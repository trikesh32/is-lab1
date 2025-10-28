package com.trikesh.islab1.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Coordinates {
    @Min(value = -859, message = "X must be greater than -860")
    private double x;

    @NotNull(message = "Y cannot be null")
    @Max(value = 396, message = "Y cannot be greater than 396")
    private Float y;
    public Coordinates() {}
    public Coordinates(double x, Float y) {
        this.x = x;
        this.y = y;
    }
}