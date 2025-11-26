package ru.hipeoplea.is.lab1.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
    private static final int PASSPORT_MIN = 10;
    private static final int PASSPORT_MAX = 26;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    /**
     * Поле не может быть null, строка не может быть пустой.
     */
    @NotBlank
    private String name;

    /**
     * Поле не может быть null.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private Color eyeColor;

    /**
     * Поле может быть null.
     */
    @Enumerated(EnumType.STRING)
    private Color hairColor;

    /**
     * Поле может быть null.
     */
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    /**
     * Длина строки 10-26, значение должно быть уникальным, может быть null.
     */
    @Column(unique = true)
    @Size(min = PASSPORT_MIN, max = PASSPORT_MAX)
    private String passportID;

    /**
     * Поле может быть null.
     */
    @Enumerated(EnumType.STRING)
    private Country nationality;
}
