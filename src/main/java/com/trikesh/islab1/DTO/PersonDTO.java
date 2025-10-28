package com.trikesh.islab1.DTO;

import com.trikesh.islab1.model.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
public class PersonDTO {
    private Long id;
    private String name;
    private Coordinates coordinates;
    private LocalDateTime creationDate;
    private Color eyeColor;
    private Color hairColor;
    private Location location;
    private Long height;
    private ZonedDateTime birthday;
    private Integer weight;
    private Country nationality;

    public PersonDTO() {}

    public PersonDTO(Person person) {
        this.id = person.getId();
        this.name = person.getName();
        this.coordinates = person.getCoordinates();
        this.creationDate = person.getCreationDate();
        this.eyeColor = person.getEyeColor();
        this.hairColor = person.getHairColor();
        this.location = person.getLocation();
        this.height = person.getHeight();
        this.birthday = person.getBirthday();
        this.weight = person.getWeight();
        this.nationality = person.getNationality();
    }
}
