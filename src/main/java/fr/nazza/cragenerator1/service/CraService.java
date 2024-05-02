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
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CraService {

  public static final String COMPTE_RENDU_D_ACTIVITÉS = "COMPTE RENDU D’ACTIVITÉ (CRA)";
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
      com.lowagie.text.Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
      Paragraph para = new Paragraph(COMPTE_RENDU_D_ACTIVITÉS, font);
      para.setAlignment(Element.ALIGN_LEFT);
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
                header.setBackgroundColor(Color.ORANGE);
                header.setHorizontalAlignment(Element.ALIGN_LEFT);
                header.setVerticalAlignment(Element.ALIGN_MIDDLE);
                header.setBorderWidth(1);
                header.setBorderColor(Color.GRAY);
                header.setFixedHeight(30);
                header.setPhrase(new Phrase(headerDate, headFont));
                table.addCell(header);
                table.setWidthPercentage(100);
              });
      // add data
      Paragraph descriptionParagraph =
          new Paragraph(String.format(DESCRIPTION_S, craForm.description()), fontParagraph);
      descriptionParagraph.setAlignment(Paragraph.ALIGN_LEFT);
      document.add(descriptionParagraph);
      document.add(Chunk.NEWLINE);

      var startMonth = Ligne.getDayMonthYear(craForm.lignes().get(0).dateDebut());
      startMonth
          .datesUntil(startMonth.plusMonths(1))
          .forEach(
              (localDate -> {
                DateTimeFormatter pattern =
                    DateTimeFormatter.ofPattern("EEEE dd MMMM", Locale.FRANCE);
                String formattedDate = localDate.format(pattern);
                var heureTravaille = craForm.heuresTravaillByDate(localDate);
                PdfPCell dateCell = createPdfCell(String.valueOf(formattedDate));
                PdfPCell htCell = createPdfCell("8h");
                htCell = createPdfCell("  (8h)  " + String.valueOf(heureTravaille));

                if (isWeekEnd(localDate)) {
                  htCell = createPdfCell("");
                  dateCell.setBackgroundColor(Color.LIGHT_GRAY);
                  htCell.setBackgroundColor(Color.LIGHT_GRAY);
                }
                if (isJourFerie(localDate)) {
                  htCell = createPdfCell("");
                  dateCell.setBackgroundColor(Color.PINK);
                  htCell.setBackgroundColor(Color.PINK);
                }
                table.addCell(dateCell);
                table.addCell(htCell);
              }));
      document.add(table);
      Paragraph tjmParagraph =
          new Paragraph(
              String.format(TAUX_JOURNALIER_MOYEN_D, craForm.tjm().toString()), fontParagraph);
      tjmParagraph.setAlignment(Paragraph.ALIGN_LEFT);
      document.add(tjmParagraph);
      document.add(Chunk.NEWLINE);
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
    com.lowagie.text.Font font = new com.lowagie.text.Font();
    font.setColor(Color.GRAY);
    font.setStyle(Font.BOLD);
    PdfPCell dateCell = new PdfPCell(new Phrase(datePdf, font));
    dateCell.setPaddingLeft(1);
    dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    dateCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    dateCell.setBorderWidth(1);
    dateCell.setBorderColor(Color.GRAY);
    dateCell.setFixedHeight(30);
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
