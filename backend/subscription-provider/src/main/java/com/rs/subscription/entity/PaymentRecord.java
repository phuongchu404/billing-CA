package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_plan_assignment_id")
    private GroupPlanAssignment groupPlanAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_statement_id")
    private SettlementStatement settlementStatement;

    @Column(unique = true, nullable = false, length = 200)
    private String externalReference;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, columnDefinition = "CHAR(3)")
    private String currency;

    @Column(nullable = false, length = 30)
    private String paymentStatus;

    @Column(nullable = false, length = 30)
    private String paymentScope;

    @Column(nullable = false, length = 100)
    private String paymentMethod;

    @Column(nullable = false)
    private LocalDateTime paidAt;

    @Column(columnDefinition = "JSON")
    private String rawPayload;

}
