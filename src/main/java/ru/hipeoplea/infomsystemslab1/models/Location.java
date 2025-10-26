package ru.hipeoplea.infomsystemslab1.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "location")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Float x; //Поле не может быть null
    private int y;

    @NotNull
    private String name; //Поле может быть null
}
