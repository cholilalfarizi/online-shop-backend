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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cholildev.online_shop_backend.dto.request.ItemListRequestDTO;
import com.cholildev.online_shop_backend.dto.request.ItemSaveRequestDTO;
import com.cholildev.online_shop_backend.dto.request.PageRequest;
import com.cholildev.online_shop_backend.dto.response.CustomerDTO;
import com.cholildev.online_shop_backend.dto.response.MessageResponseDTO;
import com.cholildev.online_shop_backend.dto.response.ResponseBodyDTO;
import com.cholildev.online_shop_backend.dto.response.ResponseBodyPaginationDTO;
import com.cholildev.online_shop_backend.model.Items;
import com.cholildev.online_shop_backend.service.ItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/item")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<ResponseBodyPaginationDTO> getItemList(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1", required = false) Integer pageNumber,
            @RequestParam(defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(defaultValue = "itemId", required = false) String sortBy) {
        ItemListRequestDTO requestDTO = new ItemListRequestDTO(name);
        PageRequest pageRequest = new PageRequest(sortBy, pageSize, pageNumber);

        return itemService.getItemList(requestDTO, pageRequest.getPage(sortBy));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponseDTO> addItem(
            @RequestBody ItemSaveRequestDTO request) {
        return itemService.addItem(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseBodyDTO> getItemDetail(@PathVariable Long id) {
        return itemService.getItemDetail(id);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponseDTO> updateItem(
            @RequestBody ItemSaveRequestDTO request,
            @PathVariable Long id) {
        return itemService.updateItem(request, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> deleteItem(@PathVariable Long id) {
        return itemService.deleteItem(id);
    }

    @GetMapping("/master")
    public ResponseEntity<ResponseBodyDTO> getEducationOption() {
        List<Items> items = itemService.getMasterItems();

        List<CustomerDTO> masterResponseDTOs = items.stream()
                .map(item -> new CustomerDTO(item.getItemId(), item.getItemName()))
                .collect(Collectors.toList());

        String message = "Get master item success";
        return ResponseEntity
                .ok()
                .body(new ResponseBodyDTO(HttpStatus.OK.getReasonPhrase(),
                        HttpStatus.OK.value(), message,
                        masterResponseDTOs));
    }
}
