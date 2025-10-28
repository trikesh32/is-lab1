package com.trikesh.islab1.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Embeddable
public class Location {
    @NotNull(message = "X cannot be null")
    private Float x;

    private int y;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    public Location() {}

    public Location(Float x, int y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
    }

}
