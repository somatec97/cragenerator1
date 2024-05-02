package fr.nazza.cragenerator1.controller;

import fr.nazza.cragenerator1.form.CraForm;
import fr.nazza.cragenerator1.service.CraService;
import java.time.LocalDate;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CraController {
  private final CraService craService;

  @PostMapping("/generer-pdf")
  public ResponseEntity<byte[]> genererCraPdf(@RequestBody CraForm craForm) {
    byte[] pdf = craService.genererCraPdf(craForm);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDisposition(ContentDisposition.parse("attachment; filename=CRA.pdf"));
    headers.setContentLength(pdf.length);
    return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
  }

  @GetMapping(value = "/ApiJF")
  public Map<LocalDate, String> lesJourFeries() {
    String uri = "https://calendrier.api.gouv.fr/jours-feries/metropole/2024.json";
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<Map<LocalDate, String>> responseEntity =
        restTemplate.exchange(
            uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<LocalDate, String>>() {});
    Map<LocalDate, String> joursFeries = responseEntity.getBody();
    return joursFeries;
  }
}
