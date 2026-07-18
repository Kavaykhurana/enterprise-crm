package com.enterprise.crm.v1.tag.entity;

import com.enterprise.crm.v1.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tag extends BaseEntity {
    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 7)
    private String color; // hex validation pattern e.g., #FFFFFF

    private String description;
}
