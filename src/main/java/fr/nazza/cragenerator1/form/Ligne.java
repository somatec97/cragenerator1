package fr.nazza.cragenerator1.form;

import java.time.LocalDate;
import java.time.Month;

public record Ligne(LocalDate dateDebut, LocalDate dateFin, double heuresTravail) {

  public static LocalDate getDayMonthYear(LocalDate date) {
    Month mois = date.getMonth();
    int annee = date.getYear();
    return LocalDate.of(annee, mois, 1);
  }
}
