package fr.nazza.cragenerator1.form;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class LigneTest {

  @Test
  void doitRetournerLePremierJourDuMois() {
    Assertions.assertThat(Ligne.getDayMonthYear(LocalDate.of(2024, 12, 15)))
        .isEqualTo(LocalDate.of(2024, 12, 01));
  }

  @Test
  void doitRetournerLeMemeDateSi1erJourDuMoisSaisi() {
    Assertions.assertThat(Ligne.getDayMonthYear(LocalDate.of(2024, 12, 01)))
        .isEqualTo(LocalDate.of(2024, 12, 01));
  }
}
