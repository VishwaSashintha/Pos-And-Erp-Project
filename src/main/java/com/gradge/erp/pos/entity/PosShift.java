package com.gradge.erp.pos.entity;

import com.gradge.erp.common.entity.BaseEntity;
import com.gradge.erp.auth.entity.User;
import com.gradge.erp.pos.enums.ShiftStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pos_shifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosShift extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String posTerminalId;

    @Column(nullable = false)
    private LocalDateTime openedAt;

    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftStatus status;

    @Column(nullable = false)
    private BigDecimal startingCash;

    @Builder.Default
    private BigDecimal expectedCash = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal actualCash = BigDecimal.ZERO;

    private String closingNotes;
}
