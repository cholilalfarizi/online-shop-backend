package com.cholildev.online_shop_backend.service;

import java.util.Optional;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cholildev.online_shop_backend.dto.request.CustomerAddRequestDTO;
import com.cholildev.online_shop_backend.dto.request.CustomerListRequestDTO;
import com.cholildev.online_shop_backend.dto.response.CustomerDetailResponseDTO;
import com.cholildev.online_shop_backend.dto.response.CustomerListResponseDTO;
import com.cholildev.online_shop_backend.dto.response.MessageResponseDTO;
import com.cholildev.online_shop_backend.dto.response.PaginationDTO;
import com.cholildev.online_shop_backend.dto.response.ResponseBodyDTO;
import com.cholildev.online_shop_backend.dto.response.ResponseBodyPaginationDTO;
import com.cholildev.online_shop_backend.model.Customers;
import com.cholildev.online_shop_backend.repository.CustomersRepository;
import com.cholildev.online_shop_backend.specification.CustomerSpecification;

import jakarta.validation.ConstraintViolationException;
import lib.minio.MinioSrvc;
import lib.minio.MinioUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CustomersService {
        private final CustomersRepository customersRepository;
        private final MinioUtil minioUtil;
        private final MinioSrvc minioSrvc;
        private final ValidationService validationService;

        public ResponseEntity<ResponseBodyPaginationDTO> getCustomerList(CustomerListRequestDTO request,
                        Pageable page) {
                try {

                        Specification<Customers> customerSpec = CustomerSpecification.customerFilter(request);

                        Page<Customers> customerPage = customersRepository.findAll(customerSpec, page);
                        if (customerPage.isEmpty()) {
                                String message = "Data not found";
                                return ResponseEntity
                                                .status(HttpStatus.NOT_FOUND)
                                                .body(new ResponseBodyPaginationDTO(
                                                                HttpStatus.NOT_FOUND.getReasonPhrase(),
                                                                HttpStatus.NOT_FOUND.value(), message, null, null));
                        }

                        Page<CustomerListResponseDTO> response = customerPage
                                        .map(customer -> {
                                                String pic = customer.getPic();
                                                pic = (pic != null) ? pic : " ";
                                                String picLink = minioSrvc.getPublicLink(pic, " ",
                                                                minioSrvc.getDefaultExpiry());
                                                if (picLink.contains("%20?X-Amz")) {
                                                        picLink = null;
                                                }
                                                return CustomerListResponseDTO.builder()
                                                                .id(customer.getCustomerId())
                                                                .name(customer.getCustomerName())
                                                                .code(customer.getCustomerCode())
                                                                .isActive(customer.getIsActive())
                                                                .pic(picLink)
                                                                .address(customer.getCustomerAddress())
                                                                .phone(customer.getCustomerPhone())
                                                                .build();
                                        });

                        PaginationDTO pagination = new PaginationDTO(
                                        response.getTotalElements(),
                                        response.getTotalPages(),
                                        response.getNumber() + 1,
                                        response.getNumberOfElements());
                        String message = "Berhasil memuat data customer";
                        return ResponseEntity
                                        .ok()
                                        .body(new ResponseBodyPaginationDTO(HttpStatus.OK.getReasonPhrase(),
                                                        HttpStatus.OK.value(), message,
                                                        response.getContent(), pagination));

                } catch (Exception e) {
                        e.printStackTrace();
                        String message = "Kesalahan Server";
                        return ResponseEntity
                                        .internalServerError()
                                        .body(new ResponseBodyPaginationDTO(
                                                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(), message, null, null));
                }
        }

        public ResponseEntity<MessageResponseDTO> addCustomer(CustomerAddRequestDTO request, MultipartFile profilePic) {
                try {
                        validationService.validate(request);
                        if (customersRepository.existsByCustomerPhone(request.getPhone())) {
                                String message = "Nomor telepon sudah digunakan";
                                return ResponseEntity
                                                .badRequest()
                                                .body(new MessageResponseDTO(HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                                                HttpStatus.BAD_REQUEST.value(), message));
                        }

                        String pic = null;
                        if (profilePic != null) {
                                pic = minioUtil.uploadFileMinio(
                                                profilePic,
                                                request.getName(),
                                                request.getPhone(),
                                                "0");
                        }

                        String tempCode = UUID.randomUUID().toString();

                        Customers customers = Customers.builder()
                                        .customerName(request.getName())
                                        .customerAddress(request.getAddress())
                                        .customerPhone(request.getPhone())
                                        .customerCode(tempCode)
                                        .isActive(true)
                                        .lastOrderDate(null)
                                        .pic(pic)
                                        .build();

                        customersRepository.save(customers);
                        String customerCode = generateCustomerCode(request.getName(), customers.getCustomerId());
                        customers.setCustomerCode(customerCode);
                        customersRepository.save(customers);

                        String message = "Berhasil menambahkan customer";
                        return ResponseEntity
                                        .ok()
                                        .body(new MessageResponseDTO(HttpStatus.OK.getReasonPhrase(),
                                                        HttpStatus.OK.value(), message));
                } catch (ConstraintViolationException e) {
                        String message = "Validasi gagal: " + e.getMessage();
                        return ResponseEntity
                                        .badRequest()
                                        .body(new MessageResponseDTO(HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                                        HttpStatus.BAD_REQUEST.value(), message));
                } catch (Exception e) {
                        e.printStackTrace();
                        String message = "Kesalahan Server";
                        return ResponseEntity
                                        .internalServerError()
                                        .body(new MessageResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
                }
        }

        public ResponseEntity<ResponseBodyDTO> getCustomerDetail(Long id) {
                try {
                        Optional<Customers> customersOpt = customersRepository.findById(id);

                        if (!customersOpt.isPresent()) {
                                String message = "Customer dengan id tersebut tidak ditemukan";
                                return ResponseEntity
                                                .status(HttpStatus.NOT_FOUND)
                                                .body(new ResponseBodyDTO(HttpStatus.NOT_FOUND.getReasonPhrase(),
                                                                HttpStatus.NOT_FOUND.value(),
                                                                message, null));
                        }

                        Customers customers = customersOpt.get();

                        String pic = customers.getPic();
                        pic = (pic != null) ? pic : " ";
                        String picLink = minioSrvc.getPublicLink(pic, " ",
                                        minioSrvc.getDefaultExpiry());
                        if (picLink.contains("%20?X-Amz")) {
                                picLink = null;
                        }

                        CustomerDetailResponseDTO response = CustomerDetailResponseDTO.builder()
                                        .id(id)
                                        .name(customers.getCustomerName())
                                        .address(customers.getCustomerAddress())
                                        .code(customers.getCustomerCode())
                                        .phone(customers.getCustomerPhone())
                                        .isActive(customers.getIsActive())
                                        .lastOrderDate(customers.getLastOrderDate())
                                        .pic(picLink)
                                        .build();
                        String message = "Berhasil memuat detail customer";
                        return ResponseEntity
                                        .ok()
                                        .body(new ResponseBodyDTO(HttpStatus.OK.getReasonPhrase(),
                                                        HttpStatus.OK.value(), message,
                                                        response));
                } catch (Exception e) {
                        String message = "Kesalahan Server";
                        return ResponseEntity
                                        .internalServerError()
                                        .body(new ResponseBodyDTO(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(), message, null));
                }
        }

        public ResponseEntity<MessageResponseDTO> editCustomer(CustomerAddRequestDTO request, MultipartFile profilePic,
                        Long id) {
                try {
                        validationService.validate(request);
                        Optional<Customers> customersOpt = customersRepository.findById(id);

                        if (!customersOpt.isPresent()) {
                                String message = "Customer dengan id tersebut tidak ditemukan";
                                return ResponseEntity
                                                .status(HttpStatus.NOT_FOUND)
                                                .body(new MessageResponseDTO(HttpStatus.NOT_FOUND.getReasonPhrase(),
                                                                HttpStatus.NOT_FOUND.value(), message));
                        }

                        Customers customers = customersOpt.get();
                        String pic = customers.getPic();

                        if (profilePic != null) {
                                pic = minioUtil.uploadFileMinio(
                                                profilePic,
                                                request.getName(),
                                                request.getPhone(),
                                                "0");
                        }

                        customers.setCustomerName(request.getName());
                        customers.setCustomerAddress(request.getAddress());
                        customers.setCustomerPhone(request.getPhone());
                        customers.setPic(pic);

                        customersRepository.save(customers);
                        String message = "Berhasil memperbarui customer";
                        return ResponseEntity
                                        .ok()
                                        .body(new MessageResponseDTO(HttpStatus.OK.getReasonPhrase(),
                                                        HttpStatus.OK.value(), message));
                } catch (ConstraintViolationException e) {
                        String message = "Validasi gagal: " + e.getMessage();
                        return ResponseEntity
                                        .badRequest()
                                        .body(new MessageResponseDTO(HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                                        HttpStatus.BAD_REQUEST.value(), message));
                } catch (Exception e) {
                        e.printStackTrace();
                        String message = "Kesalahan Server";
                        return ResponseEntity
                                        .internalServerError()
                                        .body(new MessageResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
                }
        }

        public ResponseEntity<MessageResponseDTO> deleteCustomer(Long id) {
                try {
                        Optional<Customers> customersOpt = customersRepository.findById(id);

                        if (!customersOpt.isPresent()) {
                                String message = "Customer dengan id tersebut tidak ditemukan";
                                return ResponseEntity
                                                .status(HttpStatus.NOT_FOUND)
                                                .body(new MessageResponseDTO(HttpStatus.NOT_FOUND.getReasonPhrase(),
                                                                HttpStatus.NOT_FOUND.value(), message));
                        }

                        Customers customers = customersOpt.get();
                        customers.setIsActive(false);
                        customersRepository.save(customers);
                        String message = "Berhasil menghapus customer";
                        return ResponseEntity
                                        .ok()
                                        .body(new MessageResponseDTO(HttpStatus.OK.getReasonPhrase(),
                                                        HttpStatus.OK.value(), message));
                } catch (Exception e) {
                        e.printStackTrace();
                        String message = "Kesalahan Server";
                        return ResponseEntity
                                        .internalServerError()
                                        .body(new MessageResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
                }
        }

        public List<Customers> getMasterCustomers() {
                return customersRepository.findAll();
        }

        private String generateCustomerCode(String customerName, Long id) {

                String initials = Arrays.stream(customerName.split(" "))
                                .map(word -> word.substring(0, 1))
                                .collect(Collectors.joining()).toUpperCase();

                return "CUST-" + initials + "-" + id;
        }

}
