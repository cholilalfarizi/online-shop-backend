package com.cholildev.online_shop_backend.service;

import java.util.Optional;
import java.sql.Date;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cholildev.online_shop_backend.dto.request.OrderSaveRequestDTO;
import com.cholildev.online_shop_backend.dto.response.CustomerDTO;
import com.cholildev.online_shop_backend.dto.response.ItemDTO;
import com.cholildev.online_shop_backend.dto.response.MessageResponseDTO;
import com.cholildev.online_shop_backend.dto.response.OrderResponseDTO;
import com.cholildev.online_shop_backend.dto.response.PaginationDTO;
import com.cholildev.online_shop_backend.dto.response.ResponseBodyDTO;
import com.cholildev.online_shop_backend.dto.response.ResponseBodyPaginationDTO;
import com.cholildev.online_shop_backend.model.Customers;
import com.cholildev.online_shop_backend.model.Items;
import com.cholildev.online_shop_backend.model.Orders;
import com.cholildev.online_shop_backend.repository.CustomersRepository;
import com.cholildev.online_shop_backend.repository.ItemsRepository;
import com.cholildev.online_shop_backend.repository.OrdersRepository;
import com.cholildev.online_shop_backend.specification.OrderSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderService {
    private final OrdersRepository ordersRepository;
    private final ItemsRepository itemsRepository;
    private final CustomersRepository customersRepository;

    public ResponseEntity<ResponseBodyPaginationDTO> getOrderList(String keyword, Pageable page) {
        try {
            Specification<Orders> orderSpec = OrderSpecification.orderFilter(keyword);

            Page<Orders> orderPage = ordersRepository.findAll(orderSpec, page);
            if (orderPage.isEmpty()) {
                String message = "Data not found";
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new ResponseBodyPaginationDTO(
                                HttpStatus.NOT_FOUND.getReasonPhrase(),
                                HttpStatus.NOT_FOUND.value(), message, null, null));
            }

            Page<OrderResponseDTO> response = orderPage
                    .map(order -> {
                        return OrderResponseDTO.builder()
                                .orderId(order.getOrderId())
                                .orderCode(order.getOrderCode())
                                .orderDate(order.getOrderDate())
                                .quantity(order.getQuantity())
                                .totalPrice(order.getTotalPrice())
                                .customers(new CustomerDTO(order.getCustomers().getCustomerId(),
                                        order.getCustomers().getCustomerName()))
                                .item(new ItemDTO(order.getItems().getItemId(), order.getItems().getItemName(),
                                        order.getItems().getStock(), order.getItems().getPrice()))
                                .build();
                    });
            PaginationDTO pagination = new PaginationDTO(
                    response.getTotalElements(),
                    response.getTotalPages(),
                    response.getNumber() + 1,
                    response.getNumberOfElements());
            String message = "Berhasil memuat data item";
            return ResponseEntity
                    .ok()
                    .body(new ResponseBodyPaginationDTO(HttpStatus.OK.getReasonPhrase(), HttpStatus.OK.value(), message,
                            response.getContent(), pagination));
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

    public ResponseEntity<MessageResponseDTO> addOrder(OrderSaveRequestDTO request) {
        try {
            if (ordersRepository.existsByOrderCode(request.getOrderCode())) {
                String message = "Kode order sudah tersedia";
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponseDTO(HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                HttpStatus.BAD_REQUEST.value(), message));
            }

            Optional<Customers> customersOpt = customersRepository.findById(request.getCustomerId());

            if (!customersOpt.isPresent()) {
                String message = "Customer dengan id tersebut tidak ditemukan";
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponseDTO(HttpStatus.NOT_FOUND.getReasonPhrase(),
                                HttpStatus.NOT_FOUND.value(), message));
            }

            Customers customers = customersOpt.get();

            Optional<Items> itemsOpt = itemsRepository.findById(request.getItemId());

            if (!itemsOpt.isPresent()) {
                String message = "Item dengan id tersebut tidak ditemukan";
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponseDTO(HttpStatus.NOT_FOUND.getReasonPhrase(),
                                HttpStatus.NOT_FOUND.value(), message));
            }

            Items items = itemsOpt.get();

            int totalPrice = request.getQuantity() * items.getPrice();
            int stock = items.getStock() - request.getQuantity();

            Orders orders = Orders.builder()
                    .orderCode(request.getOrderCode())
                    .orderDate(Date.valueOf(LocalDate.now()))
                    .customers(customers)
                    .items(items)
                    .quantity(request.getQuantity())
                    .totalPrice(totalPrice)
                    .build();

            ordersRepository.save(orders);

            customers.setLastOrderDate(Date.valueOf(LocalDate.now()));
            items.setStock(stock);
            items.setLastReStock(Date.valueOf(LocalDate.now()));

            itemsRepository.save(items);
            customersRepository.save(customers);
            String message = "Berhasil menambahkan order";
            return ResponseEntity
                    .ok()
                    .body(new MessageResponseDTO(HttpStatus.OK.getReasonPhrase(), HttpStatus.OK.value(), message));
        } catch (Exception e) {
            e.printStackTrace();
            String message = "Internal Server Error";
            return ResponseEntity
                    .internalServerError()
                    .body(new MessageResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }
    }

    public ResponseEntity<ResponseBodyDTO> getOrderDetail(Long id) {
        try {
            Optional<Orders> ordersOpt = ordersRepository.findById(id);
            if (!ordersOpt.isPresent()) {
                String message = "Order dengan id tersebut tidak ditemukan";
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new ResponseBodyDTO(HttpStatus.NOT_FOUND.getReasonPhrase(), HttpStatus.NOT_FOUND.value(),
                                message, null));
            }

            Orders order = ordersOpt.get();

            OrderResponseDTO response = OrderResponseDTO.builder()
                    .orderId(id)
                    .orderCode(order.getOrderCode())
                    .orderDate(order.getOrderDate())
                    .quantity(order.getQuantity())
                    .totalPrice(order.getTotalPrice())
                    .customers(new CustomerDTO(order.getCustomers().getCustomerId(),
                            order.getCustomers().getCustomerName()))
                    .item(new ItemDTO(order.getItems().getItemId(), order.getItems().getItemName(),
                            order.getItems().getStock(), order.getItems().getPrice()))
                    .build();

            String message = "Berhasil memuat detail order";
            return ResponseEntity
                    .ok()
                    .body(new ResponseBodyDTO(HttpStatus.OK.getReasonPhrase(), HttpStatus.OK.value(), message,
                            response));
        } catch (Exception e) {
            String message = "Internal Server Error";
            return ResponseEntity
                    .internalServerError()
                    .body(new ResponseBodyDTO(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), message, null));
        }
    }

    public ResponseEntity<MessageResponseDTO> updateOrder(OrderSaveRequestDTO request, Long id) {
        try {

            Optional<Orders> ordersOpt = ordersRepository.findById(id);
            if (!ordersOpt.isPresent()) {
                String message = "Order dengan id tersebut tidak ditemukan";
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponseDTO(HttpStatus.NOT_FOUND.getReasonPhrase(),
                                HttpStatus.NOT_FOUND.value(),
                                message));
            }

            Orders order = ordersOpt.get();
            if (!request.getOrderCode().equalsIgnoreCase(order.getOrderCode())) {
                if (ordersRepository.existsByOrderCode(request.getOrderCode())) {
                    String message = "Kode order sudah tersedia";
                    return ResponseEntity
                            .badRequest()
                            .body(new MessageResponseDTO(HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                    HttpStatus.BAD_REQUEST.value(), message));
                }
            }

            Optional<Customers> customersOpt = customersRepository.findById(request.getCustomerId());

            if (!customersOpt.isPresent()) {
                String message = "Customer dengan id tersebut tidak ditemukan";
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponseDTO(HttpStatus.NOT_FOUND.getReasonPhrase(),
                                HttpStatus.NOT_FOUND.value(), message));
            }

            Customers customers = customersOpt.get();

            Optional<Items> itemsOpt = itemsRepository.findById(request.getItemId());

            if (!itemsOpt.isPresent()) {
                String message = "Item dengan id tersebut tidak ditemukan";
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponseDTO(HttpStatus.NOT_FOUND.getReasonPhrase(),
                                HttpStatus.NOT_FOUND.value(), message));
            }

            Items items = itemsOpt.get();

            int totalPrice = request.getQuantity() * items.getPrice();
            int stock = items.getStock() - request.getQuantity();

            order.setOrderCode(request.getOrderCode());
            order.setCustomers(customers);
            order.setItems(items);
            order.setOrderDate(Date.valueOf(LocalDate.now()));
            order.setQuantity(request.getQuantity());
            order.setTotalPrice(totalPrice);

            ordersRepository.save(order);

            customers.setLastOrderDate(Date.valueOf(LocalDate.now()));
            items.setStock(stock);
            items.setLastReStock(Date.valueOf(LocalDate.now()));

            itemsRepository.save(items);
            customersRepository.save(customers);
            String message = "Berhasil memperbarui order";
            return ResponseEntity
                    .ok()
                    .body(new MessageResponseDTO(HttpStatus.OK.getReasonPhrase(), HttpStatus.OK.value(), message));
        } catch (Exception e) {
            e.printStackTrace();
            String message = "Internal Server Error";
            return ResponseEntity
                    .internalServerError()
                    .body(new MessageResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }
    }

    public ResponseEntity<MessageResponseDTO> deleteOrder(Long id) {
        try {
            Optional<Orders> ordersOpt = ordersRepository.findById(id);
            if (!ordersOpt.isPresent()) {
                String message = "Order dengan id tersebut tidak ditemukan";
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponseDTO(HttpStatus.NOT_FOUND.getReasonPhrase(),
                                HttpStatus.NOT_FOUND.value(),
                                message));
            }

            Orders order = ordersOpt.get();

            ordersRepository.delete(order);

            String message = "Berhasil menghapus order";
            return ResponseEntity
                    .ok()
                    .body(new MessageResponseDTO(HttpStatus.OK.getReasonPhrase(), HttpStatus.OK.value(), message));
        } catch (Exception e) {
            String message = "Internal Server Error";
            return ResponseEntity
                    .internalServerError()
                    .body(new MessageResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }
    }

}
