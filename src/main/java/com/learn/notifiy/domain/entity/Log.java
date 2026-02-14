package com.learn.notifiy.domain.entity;

import com.learn.notifiy.domain.enums.LogStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "logs")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "log_seq")
    @SequenceGenerator(name = "log_seq", sequenceName = "logs_id_seq", allocationSize = 1000)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private Integer retryCount;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private LogStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> response;

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime createdAt;
}
