package com.enterprise.crm.v1.lead.entity;

import com.enterprise.crm.v1.common.entity.BaseEntity;
import com.enterprise.crm.v1.tag.entity.Tag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "leads")
@SQLDelete(sql = "UPDATE leads SET deleted_at = NOW() WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lead extends BaseEntity {
    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, length = 50)
    private String source; // WEB, REFERRAL, COLD_CALL, PARTNER, CONFERENCE, OTHER

    @Column(nullable = false, length = 20)
    private String priority; // LOW, MEDIUM, HIGH

    @Column(nullable = false, length = 30)
    private String status; // NEW, CONTACTED, QUALIFIED, PROPOSAL, NEGOTIATION, WON, LOST

    @Column(name = "assigned_sales_rep_id")
    private UUID assignedSalesRepId;

    @Column(name = "converted_customer_id")
    private UUID convertedCustomerId;

    @Column(name = "expected_budget", precision = 19, scale = 4)
    private BigDecimal expectedBudget;

    @Column(name = "expected_close_date")
    private LocalDate expectedCloseDate;

    @Column(name = "last_contacted_at")
    private LocalDateTime lastContactedAt;

    @Column(name = "next_follow_up_date")
    private LocalDateTime nextFollowUpDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "lead_tags",
        joinColumns = @JoinColumn(name = "lead_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}
