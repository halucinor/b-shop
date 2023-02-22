package com.gabia.bshop.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gabia.bshop.dto.request.ItemChangeRequest;
import com.gabia.bshop.dto.request.ItemRequest;
import com.gabia.bshop.dto.response.ItemResponse;
import com.gabia.bshop.security.Login;
import com.gabia.bshop.service.ItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ItemController {
	private final ItemService itemService;

	@GetMapping("/items/{id}")
	public ResponseEntity<ItemResponse> findItem(@PathVariable final Long id) {
		return ResponseEntity.ok().body(itemService.findItem(id));
	}

	@GetMapping("/items")
	public ResponseEntity<Page<ItemResponse>> findItemList(
		final Pageable pageable,
		@RequestParam("categoryId") final Long categoryId) {
		return ResponseEntity.ok().body(itemService.findItemList(pageable, categoryId));
	}
	//년도, 상의, 하의
	// &category=상의&year=2022
	//

	@Login(admin = true)
	@PostMapping("/items")
	public ResponseEntity<ItemResponse> createItem(@RequestBody @Valid final ItemRequest itemRequest) {
		return ResponseEntity.ok().body(itemService.createItem(itemRequest));
	}

	@Login(admin = true)
	@PatchMapping("/items")
	public ResponseEntity<ItemResponse> updateItem(@RequestBody @Valid final ItemChangeRequest itemChangeRequest) {
		return ResponseEntity.ok().body(itemService.updateItem(itemChangeRequest));
	}

	@Login(admin = true)
	@DeleteMapping("/items/{id}")
	public ResponseEntity<Void> deleteItem(@PathVariable final Long id) {
		itemService.deleteItem(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
