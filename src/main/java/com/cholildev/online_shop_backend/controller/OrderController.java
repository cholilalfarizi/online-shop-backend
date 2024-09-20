package com.cholildev.online_shop_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cholildev.online_shop_backend.dto.request.OrderSaveRequestDTO;
import com.cholildev.online_shop_backend.dto.request.PageRequest;
import com.cholildev.online_shop_backend.dto.response.MessageResponseDTO;
import com.cholildev.online_shop_backend.dto.response.ResponseBodyDTO;
import com.cholildev.online_shop_backend.dto.response.ResponseBodyPaginationDTO;
import com.cholildev.online_shop_backend.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ResponseBodyPaginationDTO> getOrderList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1", required = false) Integer pageNumber,
            @RequestParam(defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(defaultValue = "orderId,asc", required = false) String sortBy) {
        PageRequest pageRequest = new PageRequest(sortBy, pageSize, pageNumber);

        return orderService.getOrderList(keyword, pageRequest.getPage(sortBy));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponseDTO> addOrder(
            @RequestBody OrderSaveRequestDTO request) {
        return orderService.addOrder(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseBodyDTO> getOrderDetail(@PathVariable Long id) {
        return orderService.getOrderDetail(id);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponseDTO> updateOrder(
            @RequestBody OrderSaveRequestDTO request,
            @PathVariable Long id) {
        return orderService.updateOrder(request, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> deleteOrder(@PathVariable Long id) {
        return orderService.deleteOrder(id);
    }
}
