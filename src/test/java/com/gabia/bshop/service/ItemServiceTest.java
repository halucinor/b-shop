package com.gabia.bshop.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.gabia.bshop.dto.ItemDto;
import com.gabia.bshop.entity.Category;
import com.gabia.bshop.entity.Item;
import com.gabia.bshop.entity.enumtype.ItemStatus;
import com.gabia.bshop.mapper.ItemMapper;
import com.gabia.bshop.repository.CategoryRepository;
import com.gabia.bshop.repository.ItemRepository;

import jakarta.persistence.EntityNotFoundException;

@SpringBootTest
class ItemServiceTest {

	Item item1;
	Item item2;
	List<Item> itemList;
	@Mock
	private CategoryRepository categoryRepository;
	@Mock
	private ItemRepository itemRepository;
	@InjectMocks
	private ItemService itemService;

	@BeforeEach
	void beforeEach() {
		// given
		Category category = Category.builder().id(1L).name("name").build();
		item1 =
			Item.builder()
				.id(1L)
				.category(category)
				.name("item")
				.itemStatus(ItemStatus.PUBLIC)
				.basePrice(10000)
				.description("description")
				.deleted(false)
				.openAt(LocalDateTime.now())
				.build();

		item2 =
			Item.builder()
				.id(2L)
				.category(category)
				.name("item2")
				.itemStatus(ItemStatus.PUBLIC)
				.basePrice(10000)
				.description("description")
				.deleted(false)
				.openAt(LocalDateTime.now())
				.build();

		itemList = new ArrayList<>();
		itemList.add(item1);
		itemList.add(item2);
		Pageable pageable = PageRequest.of(0, 10);

		Page<Item> itemPage = new PageImpl<>(Collections.unmodifiableList(itemList));

		when(categoryRepository.findById(1L)).thenReturn(Optional.ofNullable(category));

		// 존재하는 상품
		when(itemRepository.findById(1L)).thenReturn(Optional.ofNullable(item1));
		when(itemRepository.findById(2L)).thenReturn(Optional.ofNullable(item2));

		// PageResult
		when(itemRepository.findAll(pageable)).thenReturn(itemPage);

		// 존재하지 않는 상품
		when(itemRepository.findById(3L)).thenThrow(EntityNotFoundException.class);
		when(categoryRepository.findById(2L)).thenThrow(EntityNotFoundException.class);
	}

	@Test
	void 상품_조회() {
		// then
		assertEquals(1L, itemService.findItem(1L).id());
		assertEquals(2L, itemService.findItem(2L).id());
	}

	@Test
	void 상품_목록_조회() {
		// givn
		Pageable pageable = PageRequest.of(0, 10);
		List<ItemDto> itemDtoList = itemList.stream().map(ItemMapper.INSTANCE::itemToDto).toList();
		// then
		assertEquals(itemDtoList, itemService.findListItems(pageable));
	}

	@Test
	void 상품_정보_수정() {
		// given
		Category category = Category.builder().id(1L).name("name").build();
		Item item =
			Item.builder()
				.id(2L)
				.category(category)
				.name("item2")
				.itemStatus(ItemStatus.PUBLIC)
				.basePrice(20000)
				.description("changed")
				.deleted(false)
				.openAt(LocalDateTime.now())
				.build();

		ItemDto itemDto = ItemMapper.INSTANCE.itemToDto(item);

		// when
		when(itemRepository.save(item)).thenReturn(item);

		// then
		ItemDto changeedItem = itemService.updateItem(itemDto);

		assertAll(
			() -> assertEquals(20000, changeedItem.basePrice()),
			() -> assertEquals("changed", changeedItem.description()));
	}

	@Test
	void 상품_수정_실패_상품_없음() {
		// given
		Category category = Category.builder().id(1L).name("name").build(); // 존재하지 않는 카테고리

		Item item =
			Item.builder()
				.id(3L)
				.category(category)
				.name("item3")
				.itemStatus(ItemStatus.PUBLIC)
				.basePrice(20000)
				.description("not exist")
				.deleted(false)
				.openAt(LocalDateTime.now())
				.build();

		ItemDto itemDto = ItemMapper.INSTANCE.itemToDto(item);

		// when
		when(itemRepository.save(item)).thenReturn(item);

		// then
		Assertions.assertThrows(
			EntityNotFoundException.class,
			() -> {
				itemService.updateItem(itemDto);
			});
	}

	@Test
	void 상품_수정_실패_카테고리_없음() {
		// given
		Category category = Category.builder().id(2L).name("name").build(); // 존재하지 않는 카테고리

		Item item =
			Item.builder()
				.id(2L)
				.category(category)
				.name("item2")
				.itemStatus(ItemStatus.PUBLIC)
				.basePrice(20000)
				.description("changed")
				.deleted(false)
				.openAt(LocalDateTime.now())
				.build();

		ItemDto itemDto = ItemMapper.INSTANCE.itemToDto(item);

		// when
		when(itemRepository.save(item)).thenReturn(item);

		// then
		Assertions.assertThrows(
			EntityNotFoundException.class,
			() -> {
				itemService.updateItem(itemDto);
			});
	}

	@Test
	void 상품_제거_성공() {
		itemService.deleteItem(1L);
		itemService.deleteItem(2L);
		verify(itemRepository, times(2)).deleteById(anyLong());
	}

	@Test
	void 상품_제거_실패() {
		Assertions.assertThrows(
			EntityNotFoundException.class,
			() -> {
				itemService.deleteItem(3L);
			});
	}
}
