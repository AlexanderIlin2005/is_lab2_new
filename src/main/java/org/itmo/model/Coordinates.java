package org.itmo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "coordinates")
public class Coordinates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Max(931)
    private Float x;

    @NotNull
    private Integer y;

    @AssertTrue(message = "x must be less than or equal to 931")
    @JsonIgnore
    public boolean isXValid() {
        return x != null && x <= 931;
    }
}
