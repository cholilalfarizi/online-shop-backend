package com.cholildev.online_shop_backend.service;

import java.util.Optional;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CustomersService {
    private final CustomersRepository customersRepository;

    public ResponseEntity<ResponseBodyPaginationDTO> getCustomerList(CustomerListRequestDTO request, Pageable page){
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
                                    return CustomerListResponseDTO.builder()
                                            .id(customer.getCustomerId())
                                            .name(customer.getCustomerName())
                                            .isActive(customer.getIsActive())
                                            .pic(customer.getPic())
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
                    .body(new ResponseBodyPaginationDTO(HttpStatus.OK.getReasonPhrase(), HttpStatus.OK.value(), message, response.getContent(), pagination));

        } catch (Exception e) {
            e.printStackTrace();
            String message = "Internal Server Error";
            return ResponseEntity
                .internalServerError()
                .body(new ResponseBodyPaginationDTO(
                    HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), 
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), message, null, null));
        }
    }

    public ResponseEntity<MessageResponseDTO> addCustomer(CustomerAddRequestDTO request, MultipartFile profilePic){
        try {
            if (customersRepository.existsByCustomerPhone(request.getPhone())) {
                String message = "Nomor telepon sudah digunakan";
                return ResponseEntity
                    .badRequest()
                    .body(new MessageResponseDTO(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST.value(), message));
            }
           
            String pic = null;

            Customers customers = Customers.builder()
                            .customerName(request.getName())
                            .customerAddress(request.getAddress())
                            .customerCode(request.getCode())
                            .customerPhone(request.getPhone())
                            .isActive(true)
                            .lastOrderDate(null)
                            .pic(pic)
                            .build();
            
            
            customersRepository.save(customers);

            String message = "Berhasil menambahkan customer";
            return ResponseEntity
                .ok()
                .body(new MessageResponseDTO(HttpStatus.OK.getReasonPhrase(), HttpStatus.OK.value(), message));
        } catch (Exception e) {
            e.printStackTrace();
            String message = "Internal Server Error";
            return ResponseEntity
                .internalServerError()
                .body(new MessageResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }
    }

    public ResponseEntity<ResponseBodyDTO> getCustomerDetail(Long id){
        try {
            Optional<Customers> customersOpt = customersRepository.findById(id);

            if (!customersOpt.isPresent()) {
                String message = "Customer dengan id tersebut tidak ditemukan";
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ResponseBodyDTO(HttpStatus.NOT_FOUND.getReasonPhrase(), HttpStatus.NOT_FOUND.value(), message, null));
            }

            Customers customers = customersOpt.get();

            CustomerDetailResponseDTO response = CustomerDetailResponseDTO.builder()
                                        .id(id)
                                        .name(customers.getCustomerName())
                                        .address(customers.getCustomerAddress())
                                        .code(customers.getCustomerCode())
                                        .phone(customers.getCustomerPhone())
                                        .isActive(customers.getIsActive())
                                        .lastOrderDate(customers.getLastOrderDate())
                                        .pic(customers.getPic())
                                        .build();
            String message = "Berhasil memuat detail customer";
            return ResponseEntity
                .ok()
                .body(new ResponseBodyDTO(HttpStatus.OK.getReasonPhrase(), HttpStatus.OK.value(), message, response));
        } catch (Exception e) {
            String message = "Internal Server Error";
                return ResponseEntity
                    .internalServerError()
                    .body(new ResponseBodyDTO(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), HttpStatus.INTERNAL_SERVER_ERROR.value(), message, null));
        }
    }

    public ResponseEntity<MessageResponseDTO> editCustomer(CustomerAddRequestDTO request, MultipartFile profilePic, Long id){
        try {
            Optional<Customers> customersOpt = customersRepository.findById(id);

            if (!customersOpt.isPresent()) {
                String message = "Customer dengan id tersebut tidak ditemukan";
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(HttpStatus.NOT_FOUND.getReasonPhrase(), HttpStatus.NOT_FOUND.value(), message)); 
            }

            String imageFilename = null;

            Customers customers = customersOpt.get();

            customers.setCustomerName(request.getName());
            customers.setCustomerAddress(request.getAddress());
            customers.setCustomerCode(request.getCode());
            customers.setCustomerPhone(request.getPhone());
            customers.setPic(imageFilename);

            customersRepository.save(customers);
            String message = "Berhasil memperbarui customer";
            return ResponseEntity
                .ok()
                .body(new MessageResponseDTO(HttpStatus.OK.getReasonPhrase(), HttpStatus.OK.value(), message));
        } catch (Exception e) {
            e.printStackTrace();
            String message = "Internal Server Error";
            return ResponseEntity
                .internalServerError()
                .body(new MessageResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }
    }

    public ResponseEntity<MessageResponseDTO> deleteCustomer(Long id){
        try {
            Optional<Customers> customersOpt = customersRepository.findById(id);

            if (!customersOpt.isPresent()) {
                String message = "Customer dengan id tersebut tidak ditemukan";
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(HttpStatus.NOT_FOUND.getReasonPhrase(), HttpStatus.NOT_FOUND.value(), message)); 
            }

            Customers customers = customersOpt.get();

            customersRepository.delete(customers);
            String message = "Berhasil menghapus customer";
            return ResponseEntity
                .ok()
                .body(new MessageResponseDTO(HttpStatus.OK.getReasonPhrase(), HttpStatus.OK.value(), message));
        } catch (Exception e) {
            e.printStackTrace();
            String message = "Internal Server Error";
            return ResponseEntity
                .internalServerError()
                .body(new MessageResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }
    }
}
