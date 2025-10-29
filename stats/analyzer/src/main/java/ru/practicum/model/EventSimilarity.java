package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "similarities")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventSimilarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "event1", nullable = false)
    private Long eventA;


    @Column(name = "event2", nullable = false)
    private Long eventB;

    @Column(name = "similarity", nullable = false)
    private Double score;

    @Column(name = "ts", nullable = false)
    private Instant timestamp;
}
