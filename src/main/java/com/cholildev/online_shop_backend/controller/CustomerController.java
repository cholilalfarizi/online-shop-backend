package com.cholildev.online_shop_backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cholildev.online_shop_backend.dto.request.CustomerAddRequestDTO;
import com.cholildev.online_shop_backend.dto.request.CustomerListRequestDTO;
import com.cholildev.online_shop_backend.dto.request.PageRequest;
import com.cholildev.online_shop_backend.dto.response.CustomerDTO;
import com.cholildev.online_shop_backend.dto.response.MessageResponseDTO;
import com.cholildev.online_shop_backend.dto.response.ResponseBodyDTO;
import com.cholildev.online_shop_backend.dto.response.ResponseBodyPaginationDTO;
import com.cholildev.online_shop_backend.model.Customers;
import com.cholildev.online_shop_backend.service.CustomersService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/customer")
public class CustomerController {

    private final CustomersService customersService;

    @GetMapping
    public ResponseEntity<ResponseBodyPaginationDTO> getCustomerList(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1", required = false) Integer pageNumber,
            @RequestParam(defaultValue = "5", required = false) Integer pageSize,
            @RequestParam(defaultValue = "customerId", required = false) String sortBy) {
        CustomerListRequestDTO requestDTO = new CustomerListRequestDTO(name);
        PageRequest pageRequest = new PageRequest(sortBy, pageSize, pageNumber);

        return customersService.getCustomerList(requestDTO, pageRequest.getPage(sortBy));
    }

    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE }, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponseDTO> addCustomer(
            @RequestPart(value = "request") CustomerAddRequestDTO request,
            @RequestPart(value = "file", required = false) MultipartFile proFilePic) {

        return customersService.addCustomer(request, proFilePic);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseBodyDTO> getCustomerDetail(@PathVariable Long id) {
        return customersService.getCustomerDetail(id);
    }

    @PutMapping(path = "/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE }, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponseDTO> editCustomer(
            @RequestPart(value = "request") CustomerAddRequestDTO request,
            @RequestPart(value = "file", required = false) MultipartFile proFilePic,
            @PathVariable Long id) {
        return customersService.editCustomer(request, proFilePic, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> deleteCustomer(@PathVariable Long id) {
        return customersService.deleteCustomer(id);
    }

    @GetMapping("/master")
    public ResponseEntity<ResponseBodyDTO> getEducationOption() {
        List<Customers> customers = customersService.getMasterCustomers();

        List<CustomerDTO> masterResponseDTOs = customers.stream()
                .map(customer -> new CustomerDTO(customer.getCustomerId(), customer.getCustomerName()))
                .collect(Collectors.toList());

        String message = "Get master customer success";
        return ResponseEntity
                .ok()
                .body(new ResponseBodyDTO(HttpStatus.OK.getReasonPhrase(),
                        HttpStatus.OK.value(), message,
                        masterResponseDTOs));
    }
}
