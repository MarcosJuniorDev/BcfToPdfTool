package com.bcf.pdf;

import com.bcf.model.BcfProject;
import com.bcf.parser.BcfReader;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class PdfReportGeneratorTest {

    @Test
    public void testGeneratePdfFromBcfFolder(@TempDir Path tempDir) throws Exception {
        File dir = new File("Example/CMFI_ANA_AP_P3_TRI_02.bcf");
        assertTrue(dir.exists());

        BcfReader reader = new BcfReader();
        BcfProject project = reader.read(dir);

        File pdfOutput = tempDir.resolve("test_output.pdf").toFile();
        PdfReportGenerator generator = new PdfReportGenerator();
        generator.generate(project, pdfOutput);

        assertTrue(pdfOutput.exists());
        assertTrue(pdfOutput.length() > 0);

        try (PDDocument doc = Loader.loadPDF(pdfOutput)) {
            assertTrue(doc.getNumberOfPages() > 0);
        }
    }

    @Test
    public void testGeneratePdfFromZip(@TempDir Path tempDir) throws Exception {
        File zip = new File("Example/CMFI_ANA_AP_P3_TRI_02.bcf.zip");
        assertTrue(zip.exists());

        BcfReader reader = new BcfReader();
        BcfProject project = reader.read(zip);

        File pdfOutput = tempDir.resolve("test_zip_output.pdf").toFile();
        PdfReportGenerator generator = new PdfReportGenerator();
        generator.generate(project, pdfOutput);

        assertTrue(pdfOutput.exists());
        assertTrue(pdfOutput.length() > 0);

        try (PDDocument doc = Loader.loadPDF(pdfOutput)) {
            assertTrue(doc.getNumberOfPages() > 0);
        }
    }

    @Test
    public void testGeneratePdfFromPresentation21(@TempDir Path tempDir) throws Exception {
        File zip = new File("Example/Presentation 2.1.bcf.zip");
        assertTrue(zip.exists());

        BcfReader reader = new BcfReader();
        BcfProject project = reader.read(zip);

        assertEquals(4, project.getTopics().size());
        for (var topic : project.getTopics()) {
            assertFalse(topic.getViewpoints().isEmpty());
            assertNotNull(topic.getViewpoints().get(0).getSnapshotData());
        }

        File pdfOutput = tempDir.resolve("test_presentation_21.pdf").toFile();
        PdfReportGenerator generator = new PdfReportGenerator();
        generator.generate(project, pdfOutput);

        assertTrue(pdfOutput.exists());
        assertTrue(pdfOutput.length() > 0);

        try (PDDocument doc = Loader.loadPDF(pdfOutput)) {
            assertTrue(doc.getNumberOfPages() > 0);
        }
    }

    @Test
    public void testGeneratePdfFromPresentation30(@TempDir Path tempDir) throws Exception {
        File zip = new File("Example/Presentation 3.0.bcf.zip");
        assertTrue(zip.exists());

        BcfReader reader = new BcfReader();
        BcfProject project = reader.read(zip);

        assertEquals(4, project.getTopics().size());
        for (var topic : project.getTopics()) {
            assertFalse(topic.getViewpoints().isEmpty());
            assertNotNull(topic.getViewpoints().get(0).getSnapshotData());
        }

        File pdfOutput = tempDir.resolve("test_presentation_30.pdf").toFile();
        PdfReportGenerator generator = new PdfReportGenerator();
        generator.generate(project, pdfOutput);

        assertTrue(pdfOutput.exists());
        assertTrue(pdfOutput.length() > 0);

        try (PDDocument doc = Loader.loadPDF(pdfOutput)) {
            assertTrue(doc.getNumberOfPages() > 0);
        }
    }
}
