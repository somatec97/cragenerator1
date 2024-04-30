package fr.nazza.cragenerator1;

import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.nazza.cragenerator1.service.CraService;
import java.time.LocalDate;
import jdk.jfr.Description;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Cragenerator1ApplicationTests {

  @Test
  @Description("test si un jour est férié")
  public void testIsJourFerie() {
    CraService craService = new CraService();
    LocalDate jourFerie = LocalDate.of(2024, 05, 01);
    assertTrue(craService.isJourFerie(jourFerie), "le jour est férié!");
  }
}
