package com.bcf.parser;

import com.bcf.model.BcfProject;
import com.bcf.model.BcfTopic;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class BcfReaderTest {

    @Test
    public void testReadFromDirectory() throws Exception {
        File dir = new File("Example/CMFI_ANA_AP_P3_TRI_02.bcf");
        assertTrue(dir.exists());

        BcfReader reader = new BcfReader();
        BcfProject project = reader.read(dir);

        assertNotNull(project);
        assertEquals("Nova Sede da CMFI", project.getName());
        assertEquals("7_ZaVx0sjf8", project.getProjectId());
        assertEquals("2.1", project.getVersion());
        assertEquals(4, project.getTopics().size());

        BcfTopic t0 = project.getTopics().get(0);
        assertEquals("Indicação de espaços técnicos", t0.getTitle());
        assertEquals(3, t0.getViewpoints().size());
        assertNotNull(t0.getViewpoints().get(0).getSnapshotData());
    }

    @Test
    public void testReadFromZip() throws Exception {
        File zip = new File("Example/CMFI_ANA_AP_P3_TRI_02.bcf.zip");
        assertTrue(zip.exists());

        BcfReader reader = new BcfReader();
        BcfProject project = reader.read(zip);

        assertNotNull(project);
        assertEquals("Nova Sede da CMFI", project.getName());
        assertEquals(4, project.getTopics().size());
    }
}
