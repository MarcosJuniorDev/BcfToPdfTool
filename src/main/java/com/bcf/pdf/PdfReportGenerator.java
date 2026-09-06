package com.bcf.pdf;

import com.bcf.model.BcfComment;
import com.bcf.model.BcfProject;
import com.bcf.model.BcfTopic;
import com.bcf.model.BcfViewpoint;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;

import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class PdfReportGenerator {

    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float MARGIN_LEFT = 50f;
    private static final float MARGIN_RIGHT = 50f;
    private static final float MARGIN_TOP = 50f;
    private static final float MARGIN_BOTTOM = 50f;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT;
    private static final float MIN_CONTENT_Y = MARGIN_BOTTOM + 20f;

    // Colors matching standard Trimble / MigraDoc export
    private static final Color COLOR_TITLE = new Color(74, 92, 121);       // #4a5c79 Slate Blue
    private static final Color COLOR_MUTED = new Color(111, 113, 115);     // #6f7173 Gray
    private static final Color COLOR_TEXT = new Color(35, 31, 32);         // #231f20 Dark Charcoal
    private static final Color COLOR_DIM = new Color(150, 150, 150);       // #969696 Dim Gray

    private PDFont fontRegular;
    private PDFont fontBold;
    private PDFont fontItalic;

    public void generate(BcfProject project, File outputFile) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            loadFonts(doc);
            renderDocument(doc, project);
            doc.save(outputFile);
        }
    }

    public void generate(BcfProject project, OutputStream outputStream) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            loadFonts(doc);
            renderDocument(doc, project);
            doc.save(outputStream);
        }
    }

    private void loadFonts(PDDocument doc) throws Exception {
        InputStream regularStream = getClass().getResourceAsStream("/fonts/NotoSans-Regular.ttf");
        InputStream boldStream = getClass().getResourceAsStream("/fonts/NotoSans-Bold.ttf");
        InputStream italicStream = getClass().getResourceAsStream("/fonts/NotoSans-Italic.ttf");

        if (regularStream != null) {
            fontRegular = PDType0Font.load(doc, regularStream);
        }
        if (boldStream != null) {
            fontBold = PDType0Font.load(doc, boldStream);
        } else {
            fontBold = fontRegular;
        }
        if (italicStream != null) {
            fontItalic = PDType0Font.load(doc, italicStream);
        } else {
            fontItalic = fontRegular;
        }
    }

    private class LayoutEngine {
        final PDDocument doc;
        final List<PDPage> pages = new ArrayList<>();
        PDPage currentPage;
        PDPageContentStream currentStream;
        float currentY;

        LayoutEngine(PDDocument doc) {
            this.doc = doc;
        }

        void newPage() throws Exception {
            if (currentStream != null) {
                currentStream.close();
            }
            currentPage = new PDPage(PDRectangle.A4);
            doc.addPage(currentPage);
            pages.add(currentPage);
            currentStream = new PDPageContentStream(doc, currentPage);
            currentY = PAGE_HEIGHT - MARGIN_TOP;
        }

        void ensureSpace(float neededHeight) throws Exception {
            if (currentY - neededHeight < MIN_CONTENT_Y) {
                newPage();
            }
        }

        void drawText(String text, float x, float y, PDFont font, float fontSize, Color color) throws Exception {
            if (text == null || text.isBlank()) return;
            currentStream.setNonStrokingColor(color);
            currentStream.beginText();
            currentStream.setFont(font, fontSize);
            currentStream.newLineAtOffset(x, y);
            currentStream.showText(text);
            currentStream.endText();
        }

        void drawWrappedText(String text, float x, float maxWidth, PDFont font, float fontSize, float lineSpacing, Color color) throws Exception {
            if (text == null || text.isBlank()) return;
            String[] paragraphs = text.split("\r?\n");
            for (String paragraph : paragraphs) {
                if (paragraph.isBlank()) {
                    currentY -= lineSpacing * 0.6f;
                    continue;
                }
                List<String> lines = wrapText(paragraph, maxWidth, font, fontSize);
                for (String line : lines) {
                    ensureSpace(lineSpacing);
                    drawText(line, x, currentY, font, fontSize, color);
                    currentY -= lineSpacing;
                }
            }
        }

        List<String> wrapText(String text, float maxWidth, PDFont font, float fontSize) throws Exception {
            List<String> result = new ArrayList<>();
            String[] words = text.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
                float width = font.getStringWidth(candidate) / 1000f * fontSize;
                if (width <= maxWidth) {
                    currentLine = new StringBuilder(candidate);
                } else {
                    if (!currentLine.isEmpty()) {
                        result.add(currentLine.toString());
                        currentLine = new StringBuilder(word);
                    } else {
                        result.add(candidate);
                        currentLine = new StringBuilder();
                    }
                }
            }
            if (!currentLine.isEmpty()) {
                result.add(currentLine.toString());
            }
            return result;
        }

        void drawImage(byte[] imgData, float x, float maxW, float maxH) throws Exception {
            if (imgData == null || imgData.length == 0) return;
            PDImageXObject img;
            try {
                img = PDImageXObject.createFromByteArray(doc, imgData, "snapshot");
            } catch (Exception e) {
                return;
            }

            float origW = img.getWidth();
            float origH = img.getHeight();
            float scale = Math.min(maxW / origW, maxH / origH);
            if (scale > 1.0f) scale = 1.0f;

            float targetW = origW * scale;
            float targetH = origH * scale;

            ensureSpace(targetH + 10f);
            float drawY = currentY - targetH;
            currentStream.drawImage(img, x, drawY, targetW, targetH);
            currentY = drawY - 12f;
        }

        void drawCoordinationField(String label, String value, float labelX, float valX, float spacing) throws Exception {
            ensureSpace(spacing);
            drawText(label, labelX, currentY, fontRegular, 9.5f, COLOR_TEXT);
            boolean isDim = value == null || "Not assigned".equalsIgnoreCase(value) 
                    || "No priority".equalsIgnoreCase(value) 
                    || "No due date".equalsIgnoreCase(value) 
                    || "None".equalsIgnoreCase(value)
                    || "-".equals(value);
            Color valColor = isDim ? COLOR_DIM : COLOR_TEXT;
            drawText(value != null && !value.isBlank() ? value : "-", valX, currentY, fontRegular, 9.5f, valColor);
            currentY -= spacing;
        }
    }

    private void renderDocument(PDDocument doc, BcfProject project) throws Exception {
        LayoutEngine layout = new LayoutEngine(doc);
        layout.newPage();

        // 1. Document Header on Page 1
        String projectName = project.getName() != null && !project.getName().isBlank() 
                ? project.getName() : "BCF Project";
        
        layout.drawText("Topics Export from " + projectName, MARGIN_LEFT, layout.currentY, fontRegular, 18f, COLOR_TITLE);
        layout.currentY -= 20f;

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String exportTime = isoFormat.format(new Date());

        String exportInfo = "Exported on " + exportTime + " by BCF Tool";
        layout.drawText(exportInfo, MARGIN_LEFT, layout.currentY, fontRegular, 9f, COLOR_MUTED);
        layout.currentY -= 35f;

        // Create PDF Outline (Bookmarks)
        PDDocumentOutline outline = new PDDocumentOutline();
        doc.getDocumentCatalog().setDocumentOutline(outline);

        // 2. Render Topics
        List<BcfTopic> topics = project.getTopics();
        for (int i = 0; i < topics.size(); i++) {
            BcfTopic topic = topics.get(i);

            // Avoid starting a new topic at the very bottom of a page
            if (i > 0) {
                if (layout.currentY < PAGE_HEIGHT * 0.35f) {
                    layout.newPage();
                } else {
                    layout.currentY -= 20f;
                }
            }

            // Outline bookmark for topic
            PDOutlineItem topicOutline = new PDOutlineItem();
            String outlineTitle = (topic.getIndex() != null ? "#" + topic.getIndex() + " - " : "") 
                    + (topic.getTitle() != null ? topic.getTitle() : "Topic");
            topicOutline.setTitle(outlineTitle);
            PDPageDestination dest = new PDPageFitWidthDestination();
            dest.setPage(layout.currentPage);
            topicOutline.setDestination(dest);
            outline.addLast(topicOutline);

            // Topic Date
            if (topic.getCreationDate() != null) {
                layout.ensureSpace(16f);
                layout.drawText(topic.getCreationDate(), MARGIN_LEFT, layout.currentY, fontRegular, 9f, COLOR_MUTED);
                layout.currentY -= 15f;
            }

            // Topic Title
            String title = topic.getTitle() != null ? topic.getTitle() : "Untitled Topic";
            layout.ensureSpace(24f);
            layout.drawWrappedText(title, MARGIN_LEFT, CONTENT_WIDTH, fontBold, 13.5f, 17f, COLOR_TEXT);
            layout.currentY -= 6f;

            // Topic Description
            if (topic.getDescription() != null && !topic.getDescription().isBlank()) {
                layout.ensureSpace(16f);
                layout.drawWrappedText(topic.getDescription(), MARGIN_LEFT, CONTENT_WIDTH, fontRegular, 9.5f, 13.5f, COLOR_TEXT);
                layout.currentY -= 10f;
            }

            // Coordination Section (ensure heading + fields stay together)
            layout.ensureSpace(70f);
            layout.drawText("Coordination", MARGIN_LEFT, layout.currentY, fontBold, 11f, COLOR_TEXT);
            layout.currentY -= 16f;

            float labelX = MARGIN_LEFT + 20f;
            float valX = MARGIN_LEFT + 130f;
            float fieldSpacing = 14f;

            layout.drawCoordinationField("Created By:", topic.getCreationAuthor(), labelX, valX, fieldSpacing);
            layout.drawCoordinationField("Assigned To:", topic.getAssignedTo() != null ? topic.getAssignedTo() : "Not assigned", labelX, valX, fieldSpacing);
            layout.drawCoordinationField("Priority:", topic.getPriority() != null ? topic.getPriority() : "No priority", labelX, valX, fieldSpacing);
            layout.drawCoordinationField("Status:", topic.getTopicStatus() != null ? topic.getTopicStatus() : "Open", labelX, valX, fieldSpacing);
            layout.drawCoordinationField("Type:", topic.getTopicType() != null ? topic.getTopicType() : "-", labelX, valX, fieldSpacing);
            layout.drawCoordinationField("Due Date:", topic.getDueDate() != null ? topic.getDueDate() : "No due date", labelX, valX, fieldSpacing);
            
            String tags = topic.getLabels().isEmpty() ? "None" : String.join(", ", topic.getLabels());
            layout.drawCoordinationField("Tags:", tags, labelX, valX, fieldSpacing);

            layout.currentY -= 10f;

            // Comments Section (ensure heading + first comment stay together)
            if (!topic.getComments().isEmpty()) {
                layout.ensureSpace(60f);
                layout.drawText("Comments", MARGIN_LEFT, layout.currentY, fontBold, 11f, COLOR_TEXT);
                layout.currentY -= 16f;

                for (BcfComment comment : topic.getComments()) {
                    layout.ensureSpace(30f);
                    String authorDate = (comment.getAuthor() != null ? comment.getAuthor() : "Anonymous")
                            + (comment.getDate() != null ? "   " + comment.getDate() : "");
                    layout.drawText(authorDate, MARGIN_LEFT, layout.currentY, fontRegular, 8.5f, COLOR_MUTED);
                    layout.currentY -= 13f;

                    if (comment.getComment() != null && !comment.getComment().isBlank()) {
                        layout.drawWrappedText(comment.getComment(), MARGIN_LEFT, CONTENT_WIDTH, fontRegular, 9.5f, 13f, COLOR_TEXT);
                    }
                    layout.currentY -= 10f;
                }
            }

            // Viewpoints Section
            boolean hasSnapshots = topic.getViewpoints().stream().anyMatch(vp -> vp.getSnapshotData() != null);
            if (hasSnapshots) {
                layout.ensureSpace(160f);
                layout.drawText("Viewpoints", MARGIN_LEFT, layout.currentY, fontBold, 11f, COLOR_TEXT);
                layout.currentY -= 16f;

                for (BcfViewpoint vp : topic.getViewpoints()) {
                    if (vp.getSnapshotData() != null) {
                        layout.drawImage(vp.getSnapshotData(), MARGIN_LEFT, 280f, 170f);
                    }
                }
            }

            // Topic Last Modified Footer
            if (topic.getModifiedDate() != null || topic.getModifiedAuthor() != null) {
                layout.ensureSpace(20f);
                String modInfo = "Last modified on " 
                        + (topic.getModifiedDate() != null ? topic.getModifiedDate() : "-")
                        + " by " 
                        + (topic.getModifiedAuthor() != null ? topic.getModifiedAuthor() : "-");
                layout.drawText(modInfo, MARGIN_LEFT, layout.currentY, fontRegular, 8.5f, COLOR_MUTED);
                layout.currentY -= 20f;
            }
        }

        // Close last content stream
        if (layout.currentStream != null) {
            layout.currentStream.close();
        }

        // 3. Render Footers on all pages
        int totalPages = layout.pages.size();
        for (int p = 0; p < totalPages; p++) {
            PDPage page = layout.pages.get(p);
            try (PDPageContentStream footerStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                footerStream.setNonStrokingColor(COLOR_TEXT);
                footerStream.beginText();
                footerStream.setFont(fontRegular, 8f);
                footerStream.newLineAtOffset(MARGIN_LEFT, 30f);
                String footerLeft = "BCF Topics Export from " + projectName + " |   " + (p + 1);
                footerStream.showText(footerLeft);
                footerStream.endText();
            }
        }
    }
}
