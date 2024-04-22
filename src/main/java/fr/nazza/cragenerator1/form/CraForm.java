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
    private String description;
    private BigDecimal tjm;
    private List<Ligne> lignes;

@Setter
@Getter
@ToString
 public static class Ligne {
  private LocalDate date;
  private double heuresTravail;
 }

}
