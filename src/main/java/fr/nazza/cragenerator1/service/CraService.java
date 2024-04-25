package fr.nazza.cragenerator1.service;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import fr.nazza.cragenerator1.form.CraForm;
import org.springframework.stereotype.Service;
import com.lowagie.text.*;
import com.lowagie.text.Phrase;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;






@Service
public class CraService {

    public byte[] genererCraPdf(CraForm craForm) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();
            //add text to pdf
            com.lowagie.text.Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
            Paragraph para = new Paragraph("Compte rendu d'activités", font);
            para.setAlignment(Element.ALIGN_CENTER);
            document.add(para);
            document.add(Chunk.NEWLINE);
            Font fontParagraph = FontFactory.getFont(FontFactory.HELVETICA);
            fontParagraph.setSize(14);

            PdfPTable table = new PdfPTable(2);
            //make column titles
            Stream.of("Date", "Heures travaillées").forEach(headerDate -> {
                PdfPCell header = new PdfPCell();
                com.lowagie.text.Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
                header.setBackgroundColor(Color.LIGHT_GRAY);
                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                header.setBorderWidth(2);
                header.setPhrase(new Phrase(headerDate, headFont));
                table.addCell(header);
            });
            //add data
            Paragraph descriptionParagraph = new Paragraph("Description: " + craForm.getDescription(), fontParagraph);
            descriptionParagraph.setAlignment(Paragraph.ALIGN_LEFT);
            document.add(descriptionParagraph);
            document.add(Chunk.NEWLINE);

            Paragraph tjmParagraph = new Paragraph("Taux journalier moyen: " + craForm.getTjm(), fontParagraph);
            tjmParagraph.setAlignment(Paragraph.ALIGN_LEFT);
            document.add(tjmParagraph);
            document.add(Chunk.NEWLINE);

            for (CraForm.Ligne ligne : craForm.getLignes()) {
                for (LocalDate date = ligne.getDateDebut(); date.isBefore(ligne.getDateFin()) || date.isEqual(ligne.getDateFin()); date = date.plusDays(1)) {
                    while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY ){
                        date = date.plusDays(1);
                    }
                    if (date.getDayOfWeek() == DayOfWeek.FRIDAY ) {
                        date = date.plusDays(2);
                    }
                    if (date.equals(ligne.getJourRepos())){
                        date = date.plusDays(1);
                    }
                    LocalDate datePdf = date.plusDays(1);
                    PdfPCell dateCell = new PdfPCell(new Phrase(String.valueOf(datePdf)));
                    dateCell.setPaddingLeft(2);
                    dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    dateCell.setBorderWidth(2);
                    table.addCell(dateCell);

                    PdfPCell hTCell = new PdfPCell(new Phrase(String.valueOf(ligne.getHeuresTravail())));
                    hTCell.setPaddingLeft(2);
                    hTCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    hTCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    hTCell.setBorderWidth(2);
                    table.addCell(hTCell);
                }
            }
            document.add(table);
            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
