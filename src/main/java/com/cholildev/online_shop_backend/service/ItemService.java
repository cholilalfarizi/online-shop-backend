package com.cholildev.online_shop_backend.service;

import org.springframework.stereotype.Service;

import com.cholildev.online_shop_backend.dto.request.ItemListRequestDTO;
import com.cholildev.online_shop_backend.dto.request.ItemSaveRequestDTO;
import com.cholildev.online_shop_backend.dto.response.ItemResponseDTO;
import com.cholildev.online_shop_backend.dto.response.MessageResponseDTO;
import com.cholildev.online_shop_backend.dto.response.PaginationDTO;
import com.cholildev.online_shop_backend.dto.response.ResponseBodyDTO;
import com.cholildev.online_shop_backend.dto.response.ResponseBodyPaginationDTO;
import com.cholildev.online_shop_backend.model.Items;
import com.cholildev.online_shop_backend.repository.ItemsRepository;
import com.cholildev.online_shop_backend.specification.ItemSpecification;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemService {
    private final ItemsRepository itemsRepository;

    public ResponseEntity<ResponseBodyPaginationDTO> getItemList(ItemListRequestDTO request, Pageable page){
        try {
            
            Specification<Items> itemSpec = ItemSpecification.itemFilter(request);

            Page<Items> itemPage = itemsRepository.findAll(itemSpec, page);
            if (itemPage.isEmpty()) {
                String message = "Data not found";
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ResponseBodyPaginationDTO(
                        HttpStatus.NOT_FOUND.getReasonPhrase(), 
                    HttpStatus.NOT_FOUND.value(), message, null, null));
            }

            Page<ItemResponseDTO> response = itemPage
                                .map(item -> {
                                    return ItemResponseDTO.builder()
                                            .id(item.getItemId())
                                            .name(item.getItemName())
                                            .code(item.getItemCode())
                                            .stock(item.getStock())
                                            .price(item.getPrice())
                                            .lastReStock(item.getLastReStock())
                                            .isAvailable(item.getIsAvailable())
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

    public ResponseEntity<MessageResponseDTO> addItem(ItemSaveRequestDTO request){
        try {
            if (itemsRepository.existsByItemCode(request.getCode())) {
                String message = "Kode item sudah tersedia";
                return ResponseEntity
                    .badRequest()
                    .body(new MessageResponseDTO(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST.value(), message));
            }

            Items items = Items.builder()
                            .itemName(request.getName())
                            .itemCode(request.getCode())
                            .price(request.getPrice())
                            .stock(request.getStock())
                            .isAvailable(request.getIsAvailable())
                            .lastReStock(Date.valueOf(LocalDate.now()))
                            .build();
            
            
            itemsRepository.save(items);

            String message = "Berhasil menambahkan item";
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

    public ResponseEntity<ResponseBodyDTO> getItemDetail(Long id){
        try {
            Optional<Items> itemsOpt = itemsRepository.findById(id);

            if (!itemsOpt.isPresent()) {
                String message = "Item dengan id tersebut tidak ditemukan";
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ResponseBodyDTO(HttpStatus.NOT_FOUND.getReasonPhrase(), HttpStatus.NOT_FOUND.value(), message, null));
            }

            Items items = itemsOpt.get();

            ItemResponseDTO response = ItemResponseDTO.builder()
                                        .id(id)
                                        .name(items.getItemName())
                                        .code(items.getItemCode())
                                        .stock(items.getStock())
                                        .price(items.getPrice())
                                        .isAvailable(items.getIsAvailable())
                                        .lastReStock(items.getLastReStock())
                                        .build();
            String message = "Berhasil memuat detail item";
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

    public ResponseEntity<MessageResponseDTO> updateItem(ItemSaveRequestDTO request, Long id){
        try {

            Optional<Items> itemsOpt = itemsRepository.findById(id);

            if (!itemsOpt.isPresent()) {
                String message = "Item dengan id tersebut tidak ditemukan";
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(HttpStatus.NOT_FOUND.getReasonPhrase(), HttpStatus.NOT_FOUND.value(), message));
            }

            Items items = itemsOpt.get();
            if (!request.getCode().equalsIgnoreCase(items.getItemCode())) {
                if (itemsRepository.existsByItemCode(request.getCode())) {
                    String message = "Kode item sudah tersedia";
                    return ResponseEntity
                        .badRequest()
                        .body(new MessageResponseDTO(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST.value(), message));
                }
            }
            

            items.setItemName(request.getName());
            items.setItemCode(request.getCode());
            items.setPrice(request.getPrice());
            items.setStock(request.getStock());
            items.setIsAvailable(request.getIsAvailable());
            items.setLastReStock(Date.valueOf(LocalDate.now()));
            
            itemsRepository.save(items);

            String message = "Berhasil memperbarui item";
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

    public ResponseEntity<MessageResponseDTO> deleteItem(Long id){
        try {

            Optional<Items> itemsOpt = itemsRepository.findById(id);

            if (!itemsOpt.isPresent()) {
                String message = "Item dengan id tersebut tidak ditemukan";
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(HttpStatus.NOT_FOUND.getReasonPhrase(), HttpStatus.NOT_FOUND.value(), message));
            }

            Items items = itemsOpt.get();
            
            itemsRepository.delete(items);

            String message = "Berhasil menghapus item";
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
