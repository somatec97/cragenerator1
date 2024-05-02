package fr.nazza.cragenerator1.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.*;
import fr.nazza.cragenerator1.form.CraForm;
import fr.nazza.cragenerator1.form.Ligne;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CraService {

  public static final String COMPTE_RENDU_D_ACTIVITÉS = "Compte rendu d'activités";
  public static final String HEURES_TRAVAILLÉES = "Heures travaillées";
  public static final String DATE = "Date";
  public static final String TAUX_JOURNALIER_MOYEN_D = "Taux journalier moyen: %s";
  public static final String DESCRIPTION_S = "Description: %s";

  public byte[] genererCraPdf(CraForm craForm) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      Document document = new Document(PageSize.A4);
      PdfWriter.getInstance(document, outputStream);
      document.open();
      // add text to pdf
      com.lowagie.text.Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
      Paragraph para = new Paragraph(COMPTE_RENDU_D_ACTIVITÉS, font);
      para.setAlignment(Element.ALIGN_CENTER);
      document.add(para);
      document.add(Chunk.NEWLINE);
      Font fontParagraph = FontFactory.getFont(FontFactory.HELVETICA);
      fontParagraph.setSize(14);

      PdfPTable table = new PdfPTable(2);
      // make column titles
      Stream.of(DATE, HEURES_TRAVAILLÉES)
          .forEach(
              headerDate -> {
                PdfPCell header = new PdfPCell();
                com.lowagie.text.Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
                header.setBackgroundColor(Color.LIGHT_GRAY);
                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                header.setBorderWidth(2);
                header.setPhrase(new Phrase(headerDate, headFont));
                table.addCell(header);
              });
      // add data
      Paragraph descriptionParagraph =
          new Paragraph(String.format(DESCRIPTION_S, craForm.description()), fontParagraph);
      descriptionParagraph.setAlignment(Paragraph.ALIGN_LEFT);
      document.add(descriptionParagraph);
      document.add(Chunk.NEWLINE);

      Paragraph tjmParagraph =
          new Paragraph(
              String.format(TAUX_JOURNALIER_MOYEN_D, craForm.tjm().toString()), fontParagraph);
      tjmParagraph.setAlignment(Paragraph.ALIGN_LEFT);
      document.add(tjmParagraph);
      document.add(Chunk.NEWLINE);

      var startMonth = Ligne.getDayMonthYear(craForm.lignes().get(0).dateDebut());
      startMonth
          .datesUntil(startMonth.plusMonths(1))
          .forEach(
              (localDate -> {
                DateTimeFormatter pattern = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
                String formattedDate = localDate.format(pattern);
                if (!isWeekEnd(localDate) && !isJourFerie(localDate)) {
                  var heureTravaille = craForm.heuresTravaillByDate(localDate);
                  table.addCell(createPdfCell(String.valueOf(formattedDate)));
                  table.addCell(createPdfCell(String.valueOf(heureTravaille)));
                }
              }));
      document.add(table);
      document.close();
      return outputStream.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public boolean isJourFerie(LocalDate jourFerie) {
    return lesJourFeries().containsKey(jourFerie);
  }

  public static PdfPCell createPdfCell(String datePdf) {
    PdfPCell dateCell = new PdfPCell(new Phrase(datePdf));
    dateCell.setPaddingLeft(2);
    dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    dateCell.setBorderWidth(2);
    return dateCell;
  }

  public static boolean isWeekEnd(LocalDate date) {
    return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
  }

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
