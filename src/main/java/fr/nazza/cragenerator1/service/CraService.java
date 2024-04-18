package fr.nazza.cragenerator1.service;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import fr.nazza.cragenerator1.form.CraForm;
import org.springframework.stereotype.Service;
import com.lowagie.text.*;
import com.lowagie.text.Phrase;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.stream.Stream;

@Service
public class CraService {
    public byte[] genererCraPdf(CraForm craForm) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();
            //add text to pdf
            com.lowagie.text.Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.ORANGE);
            Paragraph para = new Paragraph("Compte rendu d'activités", font);
            para.setAlignment(Element.ALIGN_CENTER);
            document.add(para);
            document.add(Chunk.NEWLINE);
            Font fontParagraph = FontFactory.getFont(FontFactory.HELVETICA);
            fontParagraph.setSize(14);
            Paragraph paragraph = new Paragraph("Ci-dessous les jours travaillés pour le client XX sur le projet XX pour le mois : "+String.valueOf(craForm.getMois()), fontParagraph);
            paragraph.setAlignment(Paragraph.ALIGN_LEFT);
            document.add(paragraph);
            document.add(Chunk.NEWLINE);
            PdfPTable table = new PdfPTable(3);
            //make column titles
            Stream.of("Date", "Heures travaillées","Taux journalier moyen").forEach(headerDate -> {
                PdfPCell header = new PdfPCell();
                com.lowagie.text.Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
                header.setBackgroundColor(Color.LIGHT_GRAY);
                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                header.setBorderWidth(2);
                header.setPhrase(new Phrase(headerDate, headFont));
                table.addCell(header);
            });

            //add data
            PdfPCell dateCell = new PdfPCell(new Phrase(String.valueOf(craForm.getDate())));
            dateCell.setPaddingLeft(2);
            dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            dateCell.setBorderWidth(2);
            table.addCell(dateCell);

            PdfPCell hTCell = new PdfPCell(new Phrase(String.valueOf(craForm.getHeuresTravail())));
            hTCell.setPaddingLeft(2);
            hTCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            hTCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            hTCell.setBorderWidth(2);
            table.addCell(hTCell);

            PdfPCell tjmCell = new PdfPCell(new Phrase(String.valueOf(craForm.getTjm())));
            tjmCell.setPaddingLeft(2);
            tjmCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tjmCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tjmCell.setBorderWidth(2);
            table.addCell(tjmCell);

            document.add(table);
            document.add(Chunk.NEWLINE);
            Paragraph paragraph1 = new Paragraph("Description: ", fontParagraph);
            paragraph1.setAlignment(Paragraph.ALIGN_LEFT);
            //Paragraph paragraph2 = new Paragraph("Taux journalier moyen: ", fontParagraph);
            //paragraph2.setAlignment(Paragraph.ALIGN_LEFT);
            document.add(paragraph1);
            document.add(Chunk.NEWLINE);

            document.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
