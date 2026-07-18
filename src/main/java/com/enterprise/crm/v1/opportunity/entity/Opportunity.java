package com.enterprise.crm.v1.opportunity.entity;

import com.enterprise.crm.v1.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "opportunities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Opportunity extends BaseEntity {
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "assigned_sales_rep_id")
    private UUID assignedSalesRepId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "expected_revenue", nullable = false, precision = 19, scale = 4)
    private BigDecimal expectedRevenue;

    @Column(nullable = false)
    private int probability; // 0 to 100

    @Column(nullable = false, length = 30)
    private String stage; // QUALIFICATION, PROPOSAL, NEGOTIATION, WON, LOST

    @Column(name = "reason_lost")
    private String reasonLost;

    @Column(name = "expected_close_date", nullable = false)
    private LocalDate expectedCloseDate;

    @Column(name = "actual_close_date")
    private LocalDate actualCloseDate;

    @Column(name = "actual_revenue", precision = 19, scale = 4)
    private BigDecimal actualRevenue;

    @Column(nullable = false, length = 3)
    private String currency = "USD";
}
