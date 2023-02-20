package com.gabia.bshop.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.gabia.bshop.dto.request.ItemChangeRequest;
import com.gabia.bshop.entity.enumtype.ItemStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(exclude = {"category"})
@Getter
@SQLDelete(sql = "update item set deleted = true where id = ?")
@Where(clause = "deleted = false")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "item",
	indexes = {})
@Entity
public class Item extends BaseEntity {

	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<ItemOption> itemOptionList = new ArrayList<>();
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;
	@Column(columnDefinition = "varchar(255)", nullable = false)
	private String name;
	@Column(columnDefinition = "text", nullable = false)
	private String description;
	@Column(nullable = false)
	private int basePrice;
	@Enumerated(value = EnumType.STRING)
	@Column(columnDefinition = "char(8)", nullable = false)
	private ItemStatus itemStatus;
	@Column(nullable = false)
	private LocalDateTime openAt;
	@Column(nullable = false)
	private boolean deleted;
	@Column
	private String thumbnail;
	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ItemImage> itemImageList = new ArrayList<>();

	@Builder
	private Item(
		final Long id,
		final String name,
		final Category category,
		final String description,
		final int basePrice,
		final ItemStatus itemStatus,
		final LocalDateTime openAt) {
		this.id = id;
		this.name = name;
		this.category = category;
		this.description = description;
		this.basePrice = basePrice;
		this.itemStatus = itemStatus;
		this.openAt = openAt;
		this.deleted = false;
	}

	private void updateName(final String name) {
		if (name != null) {
			this.name = name;
		}
	}

	private void updatePrice(Integer basePrice) {
		if (basePrice != null) {
			this.basePrice = basePrice;
		}
	}

	private void updateDescription(String description) {
		if (description != null) {
			this.description = description;
		}
	}

	private void updateItemStatus(ItemStatus itemStatus) {
		if (itemStatus != null) {
			this.itemStatus = itemStatus;
		}
	}

	private void updateOpenAt(LocalDateTime openAt) {
		if (openAt != null) {
			this.openAt = openAt;
		}
	}

	private void updateCategory(Category category) {
		if (category != null) {
			this.category = category;
		}
	}

	public void update(final ItemChangeRequest itemChangeRequest, final Category category) {
		updateName(itemChangeRequest.name());
		updatePrice(itemChangeRequest.basePrice());
		updateDescription(itemChangeRequest.description());
		updateOpenAt(itemChangeRequest.openAt());
		updateItemStatus(itemChangeRequest.itemStatus());
		updateCategory(category);
	}

	public void setThumbnail(ItemImage itemImage) {
		if (itemImage != null) {
			this.thumbnail = itemImage.getUrl();
		}
	}

	public void setItemStatus(ItemStatus itemStatus) {
		if (itemStatus != null) {
			this.itemStatus = itemStatus;
		}
	}

	public void updateImage(List<ItemImage> itemImageList) {
		this.itemImageList = itemImageList;
	}

	public void addItemOption(ItemOption itemOption) {
		this.itemOptionList.add(itemOption);
	}

	public void addItemImage(ItemImage itemImage) {
		this.itemImageList.add(itemImage);
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		if (that == null || getClass() != that.getClass()) {
			return false;
		}
		Item item = (Item)that;
		return getId().equals(item.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
}
