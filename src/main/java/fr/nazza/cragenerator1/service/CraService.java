package fr.nazza.cragenerator1.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.*;
import fr.nazza.cragenerator1.form.CraForm;
import fr.nazza.cragenerator1.form.Ligne;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
  public static final String TAUX_JOURNALIER_MOYEN_D =
      "Soit un total de %s jours travaillés ce mois ci. Avec un TJM de %s € HT, la facture joint avec ce CRA "
          + " est de %s € HT";
  public static final String DESCRIPTION_S =
      "Description: %s les jours travaillés pour le client final %s sur le projet %s pour le mois de %s %d";
  // formatteur de date date
  private final DateTimeFormatter pattern =
      DateTimeFormatter.ofPattern("EEEE dd MMMM", Locale.FRANCE);

  public byte[] genererCraPdf(CraForm craForm) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (Document document = new Document(PageSize.A4)) {
      // La création d'un nouveau document pdf
      PdfWriter.getInstance(document, outputStream);
      document.open();
      // Ajout d'un paragraphe pour le titre
      document.add(createTitle());
      newLine(document);
      // L'ajout du descriptionParagraph au document
      document.add(createParagraph(craForm));
      newLine(document);

      // Création d'une table pour ajouter les données
      PdfPTable table = new PdfPTable(2);
      // Créer les titres des colonnes
      table.setWidthPercentage(100);
      Stream.of(DATE, HEURES_TRAVAILLÉES)
          .forEach(headerDate -> table.addCell(createCellHeader(headerDate)));
      // Récupérer le premier jour du mois
      var startMonth = Ligne.getDayMonthYear(craForm.lignes().get(0).dateDebut());
      // Le compteur AtomicInteger pour compter le nb de jours travaillés
      AtomicInteger numJrTravaill = new AtomicInteger();
      startMonth
          .datesUntil(startMonth.plusMonths(1))
          .forEach(
              (localDate -> {
                var heureTravaille = craForm.heuresTravaillByDate(localDate);
                jourTravail(localDate, heureTravaille, numJrTravaill);
                String nbJrTravail =
                    heureTravaille > 0
                        ? String.format(" %.1f (%d) ", heureTravaille, numJrTravaill.get() / 2)
                        : "";

                PdfPCell dateCell = createPdfCell(localDate.format(pattern));
                PdfPCell htCell = createPdfCell(nbJrTravail);
                updateCellsDependingOfDay(isWeekEnd(localDate), Color.LIGHT_GRAY, dateCell, htCell);
                updateCellsDependingOfDay(isJourFerie(localDate), Color.PINK, dateCell, htCell);
                updateCellsDependingOfDay(
                    isJourConge(localDate, heureTravaille), Color.PINK, dateCell, htCell);
                // L'ajout des cellules à la table
                table.addCell(dateCell);
                table.addCell(htCell);
              }));
      // L'ajout de la table au document
      document.add(table);
      // L'ajout de tjmParagraph au document
      document.add(createParagraphTjm(craForm, numJrTravaill.get() / 2));
      newLine(document);
      document.close();
      // Le return du PDF sous forme de tableau de bytes
      return outputStream.toByteArray();
    }
  }

  private void jourTravail(
      LocalDate localDate, Double heureTravaille, AtomicInteger numJrTravaill) {
    if (heureTravaille == 8 && !isJourFerie(localDate) && !isWeekEnd(localDate)) {
      numJrTravaill.addAndGet(2);
    } else if (heureTravaille == 4 && !isJourFerie(localDate) && !isWeekEnd(localDate)) {
      numJrTravaill.addAndGet(1);
    }
  }

  //  private boolean isJourTravaille(LocalDate localDate, Double heureTravaille) {
  //    return heureTravaille > 0 && !isJourFerie(localDate) && !isWeekEnd(localDate);
  //  }

  private boolean isJourConge(LocalDate localDate, Double heureTravaille) {
    return heureTravaille == 0 && !isJourFerie(localDate) && !isWeekEnd(localDate);
  }

  private void updateCellsDependingOfDay(
      boolean condition, Color color, PdfPCell dateCell, PdfPCell htCell) {
    if (condition) {
      htCell.getColumn().setText(new Phrase("0h"));
      dateCell.setBackgroundColor(color);
      htCell.setBackgroundColor(color);
    }
  }

  private static Paragraph createParagraphTjm(CraForm craForm, int numJrTravail) {
    // Paragraphe tjmParagraph pour la petite conclusion
    Paragraph tjmParagraph =
        new Paragraph(
            String.format(
                TAUX_JOURNALIER_MOYEN_D,
                numJrTravail,
                craForm.tjm().toString(),
                BigDecimal.valueOf(numJrTravail).multiply(craForm.tjm()),
                FontFactory.getFont(FontFactory.HELVETICA, 14)));
    tjmParagraph.setAlignment(Paragraph.ALIGN_LEFT);
    return tjmParagraph;
  }

  private static PdfPCell createCellHeader(String headerDate) {
    // La cellule de header
    PdfPCell header = new PdfPCell();
    header.setBackgroundColor(Color.ORANGE);
    header.setHorizontalAlignment(Element.ALIGN_LEFT);
    header.setVerticalAlignment(Element.ALIGN_MIDDLE);
    header.setBorderWidth(1);
    header.setBorderColor(Color.GRAY);
    header.setFixedHeight(30);
    header.setPhrase(new Phrase(headerDate, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
    return header;
  }

  private static Paragraph createParagraph(CraForm craForm) {
    // paragraphe pour la description
    Paragraph descriptionParagraph =
        new Paragraph(
            String.format(
                DESCRIPTION_S,
                craForm.description(),
                craForm.client(),
                craForm.projet(),
                craForm
                    .lignes()
                    .get(0)
                    .dateDebut()
                    .getMonth()
                    .getDisplayName(TextStyle.FULL, Locale.FRANCE),
                craForm.lignes().get(0).dateDebut().getYear()),
            FontFactory.getFont(FontFactory.HELVETICA, 14));
    descriptionParagraph.setAlignment(Paragraph.ALIGN_LEFT);
    return descriptionParagraph;
  }

  private Paragraph createTitle() {
    Paragraph para =
        new Paragraph(
            COMPTE_RENDU_D_ACTIVITÉS,
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK));
    para.setAlignment(Element.ALIGN_LEFT);

    return para;
  }

  private static void newLine(Document document) {
    document.add(Chunk.NEWLINE);
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
        restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
    Map<LocalDate, String> joursFeries = responseEntity.getBody();
    return joursFeries;
  }
}
