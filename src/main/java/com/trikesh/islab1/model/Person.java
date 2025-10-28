package com.trikesh.islab1.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "persons")
@AttributeOverrides({
        @AttributeOverride(name = "coordinates.x", column = @Column(name = "coord_x")),
        @AttributeOverride(name = "coordinates.y", column = @Column(name = "coord_y")),
        @AttributeOverride(name = "location.x", column = @Column(name = "loc_x")),
        @AttributeOverride(name = "location.y", column = @Column(name = "loc_y")),
        @AttributeOverride(name = "location.name", column = @Column(name = "loc_name"))
})
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Coordinates cannot be null")
    @Embedded
    private Coordinates coordinates;

    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDate;

    @NotNull(message = "Eye color cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "eye_color", nullable = false)
    private Color eyeColor;

    @NotNull(message = "Hair color cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "hair_color", nullable = false)
    private Color hairColor;

    @NotNull(message = "Location cannot be null")
    @Embedded
    private Location location;

    @Min(value = 1, message = "Height must be greater than 0")
    @Column(name = "height")
    private Long height;

    @NotNull(message = "Birthday cannot be null")
    @Column(name = "birthday", nullable = false)
    private ZonedDateTime birthday;

    @NotNull(message = "Weight cannot be null")
    @Min(value = 1, message = "Weight must be greater than 0")
    @Column(name = "weight", nullable = false)
    private Integer weight;

    @NotNull(message = "Nationality cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "nationality", nullable = false)
    private Country nationality;

    @PrePersist
    protected void onCreate() {
        creationDate = LocalDateTime.now();
    }

    public Person() {}
}
