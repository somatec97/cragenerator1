package fr.nazza.cragenerator1.form;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CraForm(
    String description, String client, String projet, BigDecimal tjm, List<Ligne> lignes) {

  public Double heuresTravaillByDate(LocalDate date) {
    return lignes.stream()
        .filter(
            ligne ->
                (date.isAfter(ligne.dateDebut()) && date.isBefore(ligne.dateFin()))
                    || date.isEqual(ligne.dateDebut())
                    || date.isEqual(ligne.dateFin()))
        .findFirst()
        .map(ligne -> ligne.heuresTravail())
        .orElse(0.00);
  }
}
