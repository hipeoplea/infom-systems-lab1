package ru.hipeoplea.is.lab1.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "coordinates")
public class Coordinates {
    private static final int MIN_X = -533;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Значение поля должно быть больше -533, поле не может быть null.
     */
    @NotNull
    @Min(value = MIN_X)
    private Float x;

    /**
     * Поле не может быть null.
     */
    @NotNull
    private Double y;
}
