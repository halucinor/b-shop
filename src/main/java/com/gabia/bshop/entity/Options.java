package com.gabia.bshop.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Options extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false, columnDefinition = "varchar(255)")
    private String description;

    @Column(nullable = false)
    private int optionLevel;

    @Column(nullable = false)
    private int optionPrice;

    @Column(nullable = false)
    private int stockQuantity;

    @Builder
    private Options(final Long id, final Item item, final String description, final int optionLevel, final int optionPrice,
            final int stockQuantity) {
        this.id = id;
        this.item = item;
        this.description = description;
        this.optionLevel = optionLevel;
        this.optionPrice = optionPrice;
        this.stockQuantity = stockQuantity;
    }
}
