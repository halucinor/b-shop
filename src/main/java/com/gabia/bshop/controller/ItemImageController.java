package com.gabia.bshop.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gabia.bshop.dto.ItemImageDto;
import com.gabia.bshop.dto.request.ItemImageCreateRequest;
import com.gabia.bshop.dto.request.ItemThumbnailChangeRequest;
import com.gabia.bshop.dto.response.ItemResponse;
import com.gabia.bshop.security.Login;
import com.gabia.bshop.service.ItemImageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ItemImageController {
	private final ItemImageService itemImageService;

	@GetMapping("/items/images/{imageId}")
	public ResponseEntity<ItemImageDto> findItemImage(@PathVariable final Long imageId) {
		return ResponseEntity.ok().body(itemImageService.findItemImage(imageId));
	}

	@GetMapping("/items/{itemId}/images")
	public ResponseEntity<List<ItemImageDto>> findItemImageList(@PathVariable final Long itemId) {
		return ResponseEntity.ok().body(itemImageService.findItemImageLists(itemId));
	}

	@Login(admin = true)
	@PostMapping("/items/images")
	public ResponseEntity<List<ItemImageDto>> creatItemImages(
		@RequestBody @Valid final ItemImageCreateRequest itemImageCreateRequest) {
		return ResponseEntity.ok().body(itemImageService.createItemImage(itemImageCreateRequest));
	}

	@Login(admin = true)
	@PatchMapping("/items/images")
	public ResponseEntity<ItemImageDto> updateItemImage(
		@RequestBody @Valid final ItemImageDto itemImageDto) {
		return ResponseEntity.ok().body(itemImageService.changeItemImage(itemImageDto));
	}

	@Login(admin = true)
	@PatchMapping("/items/thumbnail")
	public ResponseEntity<ItemResponse> updateItemThumbnail(
		@RequestBody @Valid final ItemThumbnailChangeRequest itemThumbnailChangeRequest) {
		return ResponseEntity.ok().body(itemImageService.changeItemThumbnail(itemThumbnailChangeRequest));
	}

	@Login(admin = true)
	@DeleteMapping("/items/images/{imageId}")
	public ResponseEntity<Void> deleteItemImage(@PathVariable final Long imageId) {
		itemImageService.deleteItemImage(imageId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
