package ru.practicum.model.compilation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.practicum.model.event.Event;

import java.util.List;

@Entity
@Table(name = "compilations")
@Getter
@Setter
@RequiredArgsConstructor
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "compilations_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private List<Event> events;

    @Column(name = "title")
    private String title;

    @Column(name = "pinned")
    private Boolean pinned;
}
