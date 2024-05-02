package fr.nazza.cragenerator1.service;

import static org.junit.jupiter.api.Assertions.*;

import fr.nazza.cragenerator1.form.CraForm;
import fr.nazza.cragenerator1.form.Ligne;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import jdk.jfr.Description;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class CraServiceTest {

  @Autowired private CraService craService;

  @Test
  void genererCraPdf() {
    // GIVEN
    var craForm =
        new CraForm(
            "Bonjour",
            BigDecimal.valueOf(400l),
            List.of(new Ligne(LocalDate.of(2024, 04, 02), LocalDate.of(2024, 04, 21), 8.0)));

    // WHEN
    craService.genererCraPdf(craForm);
  }

  @Test
  @Description("test si un jour est férié")
  public void testIsJourFerie() {
    CraService craService = new CraService();
    LocalDate jourFerie = LocalDate.of(2024, 05, 01);
    assertTrue(craService.isJourFerie(jourFerie), "le jour est férié!");
  }
}
