package ru.hipeoplea.infomsystemslab1.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name; //Поле не может быть null, Строка не может быть пустой

    @NotNull
    @Enumerated(EnumType.STRING)
    private Color eyeColor; //Поле не может быть null

    @Enumerated(EnumType.STRING)
    private Color hairColor; //Поле может быть null

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location; //Поле может быть null

    @Column(unique = true)
    @Size(min = 10, max = 26)
    private String passportID; //Значение этого поля должно быть уникальным, Длина строки не должна быть больше 26, Длина строки должна быть не меньше 10, Поле может быть null

    @Enumerated(EnumType.STRING)
    private Country nationality; //Поле может быть null
}