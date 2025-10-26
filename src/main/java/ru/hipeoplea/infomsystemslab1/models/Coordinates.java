package ru.hipeoplea.infomsystemslab1.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "coordinates")
public class Coordinates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Min(value = -533)
    private Float x; //Значение поля должно быть больше -553, Поле не может быть null

    @NotNull
    private Double y; //Поле не может быть null
}
