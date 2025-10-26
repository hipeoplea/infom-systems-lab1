package ru.hipeoplea.infomsystemslab1.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически

    @NotNull
    private String name; //Поле не может быть null, Строка не может быть пустой

    @ManyToOne
    @NotNull
    @JoinColumn(name = "coordinates_id")
    private Coordinates coordinates; //Поле не может быть null

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически

    @Positive
    private Long oscarsCount; //Значение поля должно быть больше 0, Поле может быть null

    @Positive
    private float budget; //Значение поля должно быть больше 0

    @Positive
    private long totalBoxOffice; //Значение поля должно быть больше 0

    @NotNull
    @Enumerated(EnumType.STRING)
    private MpaaRating mpaaRating; //Поле не может быть null

    @ManyToOne
    @NotNull
    @JoinColumn(name = "person_id")
    private Person director; //Поле не может быть null

    @ManyToOne
    @JoinColumn(name = "screenwriter_id")
    private Person screenwriter;

    @ManyToOne
    @JoinColumn(name = "operator_id")
    @NotNull
    private Person operator; //Поле не может быть null

    @Positive
    private int length; //Значение поля должно быть больше 0

    @Positive
    @NotNull
    private Long goldenPalmCount; //Значение поля должно быть больше 0, Поле не может быть null

    @Positive
    private long usaBoxOffice; //Значение поля должно быть больше 0

    @NotNull
    @Enumerated(EnumType.STRING)
    private MovieGenre genre; //Поле не может быть null

    @PrePersist
    protected void onCreate() {
        if (creationDate == null) creationDate = new Date();
    }
}
