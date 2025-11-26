package ru.hipeoplea.is.lab1.models;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Поле не может быть null, строка не может быть пустой.
     */
    @NotNull
    private String name;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "coordinates_id")
    private Coordinates coordinates;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date creationDate;

    @Positive
    private Long oscarsCount;

    @Positive
    private float budget;

    @Positive
    private long totalBoxOffice;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MpaaRating mpaaRating;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "person_id")
    private Person director;

    @ManyToOne
    @JoinColumn(name = "screenwriter_id")
    private Person screenwriter;

    @ManyToOne
    @JoinColumn(name = "operator_id")
    @NotNull
    private Person operator;

    @Positive
    private int length;

    @Positive
    @NotNull
    private Long goldenPalmCount;

    @Positive
    private long usaBoxOffice;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MovieGenre genre;

    /**
     * Auto-fills creation date before persistence.
     */
    @PrePersist
    protected void onCreate() {
        if (creationDate == null) {
            creationDate = new Date();
        }
    }
}
