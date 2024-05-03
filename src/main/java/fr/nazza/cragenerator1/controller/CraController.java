package fr.nazza.cragenerator1.controller;

import fr.nazza.cragenerator1.form.CraForm;
import fr.nazza.cragenerator1.service.CraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/cra")
@Tag(name = "CRA", description = "Controller permettant la génération d'un pdf")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CraController {
  private final CraService craService;

  @PostMapping
  @Operation(description = "Génération du CRA au format PDF")
  public ResponseEntity<byte[]> genererCraPdf(@RequestBody CraForm craForm) {
    byte[] pdf = craService.genererCraPdf(craForm);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDisposition(ContentDisposition.parse("attachment; filename=CRA.pdf"));
    headers.setContentLength(pdf.length);
    return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
  }

  @GetMapping(value = "/jours-feries/{annee}")
  @Operation(description = "Récupérer les jours fériés de l'année en cours")
  public Map<LocalDate, String> lesJourFeries(@PathVariable int annee) {
    return new RestTemplate()
        .getForObject(
            String.format("https://calendrier.api.gouv.fr/jours-feries/metropole/%d.json", annee),
            Map.class);
  }

  /**
   * @GetMapping(value = "/jours-feries/{annee}") @Operation(description = "Récupérer les jours
   * fériés de l'année en cours") public Map<LocalDate, String> lesJourFeries(@PathVariable int
   * annee) { return apiGouvRestClient .get() .uri("/jours-feries", "metropole",
   * String.format("%d.json", annee)) .retrieve() .body(Map.class); }
   */
}
