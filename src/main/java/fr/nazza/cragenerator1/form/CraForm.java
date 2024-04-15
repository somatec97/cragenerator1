package fr.nazza.cragenerator1.form;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
@AllArgsConstructor
@Getter
@Setter
@ToString

public class CraForm {
    private final LocalDate date;
    private final String mois;
    private final Long annee;
    private double heuresTravail;
    private final String description;
    private final BigDecimal tjm;
}
