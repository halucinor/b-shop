package com.gabia.bshop.entity;

import com.gabia.bshop.entity.enumtype.MemberGrade;
import com.gabia.bshop.entity.enumtype.MemberRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(255)", unique = true)
    private String email;

    @Column(columnDefinition = "char(11)", unique = true)
    private String phoneNumber;

    @Column(nullable = false, columnDefinition = "varchar(15)")
    private String name;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, columnDefinition = "char(6)")
    private MemberRole role;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(8)")
    private MemberGrade grade;

    @Column(nullable = false, columnDefinition = "varchar(255)", unique = true)
    private String hiworksId;
}
