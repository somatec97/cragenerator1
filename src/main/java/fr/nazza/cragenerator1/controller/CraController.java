package fr.nazza.cragenerator1.controller;
import fr.nazza.cragenerator1.form.CraForm;
import fr.nazza.cragenerator1.service.CraService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping

public class CraController {
    private CraService craService;

    public CraController(CraService craService) {
        this.craService = craService;
    }

    //@GetMapping("/generer-pdf")
    @PostMapping("/generer-pdf")
    @CrossOrigin (origins = "http://localhost:4200")
    public ResponseEntity<byte[]> genererCraPdf(@RequestBody CraForm craForm) {
        byte[] pdf = craService.genererCraPdf(craForm);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set("Content-Disposition", "attachment; filename=CRA.pdf");
        headers.setContentLength(pdf.length);
        return new ResponseEntity<byte[]>(pdf, headers, HttpStatus.OK);
    }
}
