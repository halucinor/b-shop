package com.gabia.bshop.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.gabia.bshop.dto.OrderItemDto;
import com.gabia.bshop.dto.request.OrderCreateRequestDto;
import com.gabia.bshop.dto.response.OrderCreateResponseDto;
import com.gabia.bshop.entity.Category;
import com.gabia.bshop.entity.Item;
import com.gabia.bshop.entity.Member;
import com.gabia.bshop.entity.Options;
import com.gabia.bshop.entity.OrderItem;
import com.gabia.bshop.entity.Orders;
import com.gabia.bshop.entity.enumtype.ItemStatus;
import com.gabia.bshop.entity.enumtype.MemberGrade;
import com.gabia.bshop.entity.enumtype.MemberRole;
import com.gabia.bshop.entity.enumtype.OrderStatus;
import com.gabia.bshop.mapper.OrderMapper;
import com.gabia.bshop.repository.ItemRepository;
import com.gabia.bshop.repository.MemberRepository;
import com.gabia.bshop.repository.OptionsRepository;
import com.gabia.bshop.repository.OrderRepository;

import jakarta.persistence.EntityNotFoundException;

/*
    OrderService 기능들에 대한 단위 테스트
    1. 생성
    2. 취소
    3. (optional) 수정
 */

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private OptionsRepository optionsRepository;

	@InjectMocks
	private OrderService orderService;

	@DisplayName("존재하지_않는_회원ID로_주문목록_조회를_요청하면_오류가_발생한다")
	@Test
	void findOrderListInvalidMemberIdFail() {
		//given
		Long invalidMemberId = 999999999999L;
		when(memberRepository.findById(invalidMemberId))
			.thenThrow(EntityNotFoundException.class);
		//when & then
		Assertions.assertThatThrownBy(
				() -> orderService.findOrdersPagination(invalidMemberId, PageRequest.of(0, 10)))
			.isInstanceOf(EntityNotFoundException.class);
	}

	@DisplayName("주문을 생성한다.")
	@Test
	void createOrder() {
		//given
		Member member = Member.builder()
			.id(1L)
			.email("test@test.com")
			.grade(MemberGrade.BRONZE)
			.name("testName")
			.phoneNumber("01000001111")
			.hiworksId("hiworks")
			.role(MemberRole.NORMAL)
			.build();

		Category category = Category.builder().id(1L).name("name").build();

		Item item1 =
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

		Item item2 =
			Item.builder()
				.id(2L)
				.category(category)
				.name("item")
				.itemStatus(ItemStatus.PUBLIC)
				.basePrice(10000)
				.description("description")
				.deleted(false)
				.openAt(LocalDateTime.now())
				.build();

		Options options1 = Options.builder()
			.id(1L)
			.item(item1)
			.description("description")
			.optionLevel(1)
			.optionPrice(0)
			.stockQuantity(10)
			.build();

		Options options2 = Options.builder()
			.id(2L)
			.item(item2)
			.description("description")
			.optionLevel(1)
			.optionPrice(1000)
			.stockQuantity(5)
			.build();

		Orders orders = Orders.builder()
			.id(1L)
			.member(member)
			.status(OrderStatus.ACCEPTED)
			.build();

		OrderItem orderItem1 = OrderItem.builder()
			.id(1L)
			.item(item1)
			.order(orders)
			.option(options1)
			.orderCount(1)
			.price(item1.getBasePrice() + options1.getOptionPrice())
			.build();

		OrderItem orderItem2 = OrderItem.builder()
			.id(2L)
			.item(item2)
			.order(orders)
			.option(options2)
			.orderCount(1)
			.price(item2.getBasePrice() + options2.getOptionPrice())
			.build();

		List<OrderItem> orderItemList = List.of(orderItem1, orderItem2);

		List<OrderItemDto> orderItemDtoList =
			OrderMapper.INSTANCE.orderItemListToOrderItemDtoList(orderItemList);

		OrderCreateRequestDto orderCreateRequestDto = OrderCreateRequestDto.builder()
			.memberId(1L)
			.status(OrderStatus.ACCEPTED)
			.orderItemDtoList(orderItemDtoList)
			.build();

		when(memberRepository.findById(1L)).thenReturn(Optional.ofNullable(member));
		when(optionsRepository.findWithOptionAndItemById(1L, 1L)).thenReturn(Optional.ofNullable(options1));
		when(optionsRepository.findWithOptionAndItemById(2L, 2L)).thenReturn(Optional.ofNullable(options2));

		//when
		OrderCreateResponseDto returnDto = orderService.createOrder(orderCreateRequestDto);

		//then
		assertAll(
			() -> assertEquals(orderItemDtoList, returnDto.orderItemDtoList()),
			() -> assertEquals(orderItemList.stream().mapToLong(OrderItem::getPrice).sum(),
				returnDto.totalPrice()),
			() -> assertEquals(orderCreateRequestDto.memberId(), returnDto.memberId()),
			() -> assertEquals(orderCreateRequestDto.status(), returnDto.status()),
			() -> assertEquals(9, options1.getStockQuantity(), "주문을 하면 재고가 줄어들어야 한다.")
		);
	}

	@DisplayName("주문을 취소한다.")
	@Test
	void cancelOrder() {
		//given
		Options options1 = Options.builder()
			.id(1L)
			.description("description")
			.optionLevel(1)
			.optionPrice(0)
			.stockQuantity(10)
			.build();

		List<Options> options = List.of(options1);

		Item item1 =
			Item.builder()
				.id(1L)
				.name("item")
				.itemStatus(ItemStatus.PUBLIC)
				.basePrice(10000)
				.description("description")
				.deleted(false)
				.openAt(LocalDateTime.now())
				.optionsList(options)
				.build();

		OrderItem orderItem1 = OrderItem.builder()
			.id(1L)
			.item(item1)
			.option(options1)
			.orderCount(5)
			.build();

		List<OrderItem> orderItemList = List.of(orderItem1);

		Orders orders = Orders.builder()
			.id(1L)
			.status(OrderStatus.ACCEPTED)
			.totalPrice(20000)
			.orderItemList(orderItemList)
			.build();

		when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(orders));

		//when
		orderService.cancelOrder(1L);

		//then
		assertAll(
			() -> assertEquals(15, options1.getStockQuantity(), "주문을 취소하면 재고가 다시 추가되어야 한다."),
			() -> assertEquals(OrderStatus.CANCELLED, orders.getStatus(),
				"주문을 취소하면 주문상태가 CANCELLED로 변경되어야 한다.")
		);
	}

	@DisplayName("주문 ID가 유효하지 않으면 주문취소에 실패한다.")
	@Test
	void cancelOrderFail() {
		//given
		Long nonId = 9999L;

		when(orderRepository.findById(nonId)).thenThrow(EntityNotFoundException.class);

		//when & then
		Assertions.assertThatThrownBy(() -> orderService.cancelOrder(nonId))
			.isInstanceOf(EntityNotFoundException.class);
	}
}
