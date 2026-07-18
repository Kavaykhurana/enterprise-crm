package com.enterprise.crm.v1.customer.entity;

import com.enterprise.crm.v1.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "customer_contacts")
@SQLDelete(sql = "UPDATE customer_contacts SET deleted_at = NOW() WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Contact extends BaseEntity {
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "mobile_phone", length = 20)
    private String mobilePhone;

    @Column(name = "work_phone", length = 20)
    private String workPhone;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Column(length = 100)
    private String department;

    @Column(name = "linkedin_url", length = 512)
    private String linkedinUrl;

    @Column(name = "is_primary_contact", nullable = false)
    private boolean isPrimaryContact = false;
}
