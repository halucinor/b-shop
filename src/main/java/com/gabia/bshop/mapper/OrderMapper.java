package com.gabia.bshop.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.gabia.bshop.dto.OrderItemDto;
import com.gabia.bshop.dto.request.OrderCreateRequest;
import com.gabia.bshop.dto.response.OrderCreateResponse;
import com.gabia.bshop.dto.response.OrderInfoPageResponse;
import com.gabia.bshop.dto.response.OrderInfoResponse;
import com.gabia.bshop.dto.response.OrderUpdateStatusResponse;
import com.gabia.bshop.entity.Order;
import com.gabia.bshop.entity.OrderItem;

@Mapper(componentModel = "spring")
public abstract class OrderMapper extends MapperSupporter {

	public static final OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

	@Mappings({
		@Mapping(source = "memberId", target = "member.id"),
		@Mapping(source = "orderCreateRequest.orderItemDtoList", target = "orderItemList"),
		@Mapping(target = "status", constant = "ACCEPTED"),
		@Mapping(target = "id", ignore = true),
		@Mapping(target = "totalPrice", ignore = true),
	})
	public abstract Order orderCreateRequestToEntity(Long memberId, OrderCreateRequest orderCreateRequest);

	@Mappings({
		@Mapping(source = "member.id", target = "memberId"),
		@Mapping(source = "orderItemList", target = "orderItemDtoList")
	})
	public abstract OrderCreateResponse orderCreateResponseToDto(Order order);

	@Mappings({
		@Mapping(source = "itemId", target = "item.id"),
		@Mapping(source = "itemOptionId", target = "option.id"),
		@Mapping(target = "id", ignore = true),
		@Mapping(target = "order", ignore = true),
		@Mapping(target = "price", ignore = true),
	})
	public abstract OrderItem orderItemDtoToOrderItem(OrderItemDto orderItemDto);

	@Mapping(source = "item.id", target = "itemId")
	@Mapping(source = "option.id", target = "itemOptionId")
	public abstract OrderItemDto orderItemToOrdersDto(OrderItem orderItem);

	public abstract List<OrderItemDto> orderItemListToOrderItemDtoList(List<OrderItem> orderItemList);

	//orderStatus Update
	@Mapping(source = "id", target = "orderId")
	@Mapping(source = "member.id", target = "memberId")
	public abstract OrderUpdateStatusResponse orderToOrderUpdateStatusResponse(Order order);

	@Named("orderToOrderInfoPageResponse")
	public OrderInfoPageResponse orderToOrderInfoPageResponse(Order order) {
		return OrderInfoPageResponse.builder()
			.orderId(order.getId())
			.orderItemDtoList(order.getOrderItemList().stream().map(orderItem ->
				OrderItemDto.builder()
					.itemId(orderItem.getItem().getId())
					.itemOptionId(orderItem.getOption().getId())
					.orderCount(orderItem.getOrderCount())
					.build()
			).toList())
			.itemThumbnail(addPrefixToThumbnail(order.getOrderItemList().get(0).getItem()))
			.itemName(order.getOrderItemList().get(0).getItem().getName())
			.itemTotalCount(order.getOrderItemList().size())
			.orderStatus(order.getStatus())
			.totalPrice(order.getTotalPrice())
			.createdAt(order.getCreatedAt())
			.build();
	}

	@Named("orderItemListToOrderInfoResponse")
	public OrderInfoResponse orderItemListToOrderInfoResponse(final List<OrderItem> orderItemList) {
		if (orderItemList == null) {
			return null;
		}
		final Order order = orderItemList.get(0).getOrder();
		return OrderInfoResponse.builder()
			.orderId(order.getId())
			.totalPrice(order.getTotalPrice())
			.createdAt(order.getCreatedAt())
			.orderStatus(order.getStatus())
			.orderItemList(orderItemList.stream()
				.map(
					orderItem -> OrderInfoResponse.SingleOrder.builder()
						.orderItemId(orderItem.getId())
						.itemId(orderItem.getItem().getId())
						.itemOptionId(orderItem.getOption().getId())
						.itemName(orderItem.getItem().getName())
						.itemOptionDescription(orderItem.getOption().getDescription())
						.orderCount(orderItem.getOrderCount())
						.price(orderItem.getPrice())
						.itemThumbnail(addPrefixToThumbnail(orderItem.getItem()))
						.build())
				.toList())
			.build();
	}
}
