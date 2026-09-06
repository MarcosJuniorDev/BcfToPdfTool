package com.bcf.parser;

import com.bcf.model.BcfComment;
import com.bcf.model.BcfProject;
import com.bcf.model.BcfTopic;
import com.bcf.model.BcfViewpoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BcfReader {

    public BcfProject read(File fileOrDir) throws Exception {
        if (!fileOrDir.exists()) {
            throw new FileNotFoundException("Arquivo ou diretório não encontrado: " + fileOrDir.getAbsolutePath());
        }

        BcfProject project;
        if (fileOrDir.isDirectory()) {
            project = readFromDirectory(fileOrDir.toPath());
        } else {
            project = readFromZip(new FileInputStream(fileOrDir));
        }

        // If project name was not found in project.bcfp, use file or directory name
        if (project.getName() == null || project.getName().isBlank() || "BCF Project".equalsIgnoreCase(project.getName())) {
            String fallbackName = fileOrDir.getName();
            if (fallbackName.endsWith(".bcf.zip")) {
                fallbackName = fallbackName.substring(0, fallbackName.length() - 8);
            } else if (fallbackName.endsWith(".bcfzip")) {
                fallbackName = fallbackName.substring(0, fallbackName.length() - 7);
            } else if (fallbackName.endsWith(".bcf") || fallbackName.endsWith(".zip")) {
                fallbackName = fallbackName.substring(0, fallbackName.lastIndexOf('.'));
            }
            if (!fallbackName.isBlank()) {
                project.setName(fallbackName);
            }
        }

        return project;
    }

    public BcfProject read(InputStream inputStream) throws Exception {
        return readFromZip(inputStream);
    }

    public BcfProject readFromDirectory(Path dir) throws Exception {
        Map<String, byte[]> fileMap = new HashMap<>();
        try (var stream = Files.walk(dir)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                String relPath = dir.relativize(path).toString().replace('\\', '/');
                try {
                    fileMap.put(relPath, Files.readAllBytes(path));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
        return parseFromMap(fileMap);
    }

    public BcfProject readFromZip(InputStream inputStream) throws Exception {
        Map<String, byte[]> fileMap = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String name = entry.getName().replace('\\', '/');
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    zis.transferTo(baos);
                    fileMap.put(name, baos.toByteArray());
                }
                zis.closeEntry();
            }
        }
        return parseFromMap(fileMap);
    }

    private BcfProject parseFromMap(Map<String, byte[]> fileMap) throws Exception {
        BcfProject project = new BcfProject();

        // 1. Read bcf.version if present
        for (Map.Entry<String, byte[]> entry : fileMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("bcf.version") || entry.getKey().endsWith("/bcf.version")) {
                try {
                    Document doc = parseXml(entry.getValue());
                    Element root = doc.getDocumentElement();
                    if (root.hasAttribute("VersionId")) {
                        project.setVersion(root.getAttribute("VersionId"));
                    } else {
                        String val = findFirstTextByTagName(root, "DetailedVersion", "VersionId");
                        if (val != null) project.setVersion(val);
                    }
                } catch (Exception ignored) {}
            }
        }

        // 2. Read project.bcfp if present
        for (Map.Entry<String, byte[]> entry : fileMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("project.bcfp") || entry.getKey().endsWith("/project.bcfp")) {
                try {
                    Document doc = parseXml(entry.getValue());
                    Element root = doc.getDocumentElement();
                    NodeList projectNodes = root.getElementsByTagName("Project");
                    if (projectNodes.getLength() > 0) {
                        Element pElem = (Element) projectNodes.item(0);
                        if (pElem.hasAttribute("ProjectId")) {
                            project.setProjectId(pElem.getAttribute("ProjectId"));
                        }
                        String name = findFirstTextByTagName(pElem, "Name", "ProjectName");
                        if (name != null && !name.isBlank()) project.setName(name);
                    }
                } catch (Exception ignored) {}
            }
        }

        // 3. Find all topic folders (containing markup.bcf or markup.xml)
        Map<String, Map<String, byte[]>> topicEntries = new HashMap<>();
        for (Map.Entry<String, byte[]> entry : fileMap.entrySet()) {
            String path = entry.getKey();
            int idx = path.lastIndexOf('/');
            if (idx > 0) {
                String topicFolder = path.substring(0, idx);
                String fileName = path.substring(idx + 1);
                topicEntries.computeIfAbsent(topicFolder, k -> new HashMap<>()).put(fileName, entry.getValue());
            }
        }

        List<BcfTopic> topics = new ArrayList<>();
        for (Map.Entry<String, Map<String, byte[]>> topicEntry : topicEntries.entrySet()) {
            Map<String, byte[]> files = topicEntry.getValue();
            byte[] markupData = null;
            for (Map.Entry<String, byte[]> f : files.entrySet()) {
                if (f.getKey().equalsIgnoreCase("markup.bcf") || f.getKey().equalsIgnoreCase("markup.xml")) {
                    markupData = f.getValue();
                    break;
                }
            }
            if (markupData != null) {
                BcfTopic topic = parseMarkup(markupData, files);
                topics.add(topic);
            }
        }

        // Sort topics by index or creation date
        topics.sort((t1, t2) -> {
            if (t1.getIndex() != null && t2.getIndex() != null) {
                return Integer.compare(t1.getIndex(), t2.getIndex());
            }
            if (t1.getIndex() != null) return -1;
            if (t2.getIndex() != null) return 1;
            if (t1.getCreationDate() != null && t2.getCreationDate() != null) {
                return t1.getCreationDate().compareTo(t2.getCreationDate());
            }
            return 0;
        });

        project.setTopics(topics);
        return project;
    }

    private BcfTopic parseMarkup(byte[] xmlData, Map<String, byte[]> folderFiles) throws Exception {
        Document doc = parseXml(xmlData);
        Element root = doc.getDocumentElement();

        BcfTopic topic = new BcfTopic();

        // 1. Parse Header Files
        NodeList fileNodes = root.getElementsByTagName("File");
        for (int i = 0; i < fileNodes.getLength(); i++) {
            Element fileElem = (Element) fileNodes.item(i);
            String filename = findFirstTextByTagName(fileElem, "Filename", "FileName");
            if (filename != null && !filename.isBlank()) {
                topic.getHeaderFiles().add(filename.trim());
            }
        }

        // 2. Parse Topic Element
        NodeList topicNodes = root.getElementsByTagName("Topic");
        Element topicElem = topicNodes.getLength() > 0 ? (Element) topicNodes.item(0) : root;

        if (topicElem.hasAttribute("Guid")) topic.setGuid(topicElem.getAttribute("Guid"));
        if (topicElem.hasAttribute("TopicType")) topic.setTopicType(topicElem.getAttribute("TopicType"));
        if (topicElem.hasAttribute("TopicStatus")) topic.setTopicStatus(topicElem.getAttribute("TopicStatus"));
        if (topicElem.hasAttribute("Priority")) topic.setPriority(topicElem.getAttribute("Priority"));
        if (topicElem.hasAttribute("Stage")) topic.setStage(topicElem.getAttribute("Stage"));

        // Fallback or override from child elements if attributes weren't set
        if (topic.getTopicType() == null || topic.getTopicType().isBlank()) {
            topic.setTopicType(findFirstTextByTagName(topicElem, "TopicType", "Type"));
        }
        if (topic.getTopicStatus() == null || topic.getTopicStatus().isBlank()) {
            topic.setTopicStatus(findFirstTextByTagName(topicElem, "TopicStatus", "Status"));
        }
        if (topic.getPriority() == null || topic.getPriority().isBlank()) {
            topic.setPriority(findFirstTextByTagName(topicElem, "Priority"));
        }
        if (topic.getStage() == null || topic.getStage().isBlank()) {
            topic.setStage(findFirstTextByTagName(topicElem, "Stage"));
        }

        topic.setTitle(findFirstTextByTagName(topicElem, "Title"));
        topic.setDescription(findFirstTextByTagName(topicElem, "Description"));
        topic.setCreationDate(findFirstTextByTagName(topicElem, "CreationDate"));
        topic.setCreationAuthor(findFirstTextByTagName(topicElem, "CreationAuthor"));
        topic.setModifiedDate(findFirstTextByTagName(topicElem, "ModifiedDate"));
        topic.setModifiedAuthor(findFirstTextByTagName(topicElem, "ModifiedAuthor"));
        topic.setAssignedTo(findFirstTextByTagName(topicElem, "AssignedTo"));
        topic.setDueDate(findFirstTextByTagName(topicElem, "DueDate"));

        String indexStr = findFirstTextByTagName(topicElem, "Index");
        if (indexStr != null && !indexStr.isBlank()) {
            try {
                topic.setIndex(Integer.parseInt(indexStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        // Labels / TopicLabels
        NodeList labelNodes = topicElem.getElementsByTagName("Labels");
        for (int i = 0; i < labelNodes.getLength(); i++) {
            Element lElem = (Element) labelNodes.item(i);
            NodeList subLabels = lElem.getElementsByTagName("TopicLabel");
            if (subLabels.getLength() == 0) {
                subLabels = lElem.getElementsByTagName("Label");
            }
            if (subLabels.getLength() > 0) {
                for (int j = 0; j < subLabels.getLength(); j++) {
                    String val = subLabels.item(j).getTextContent();
                    if (val != null && !val.isBlank() && !topic.getLabels().contains(val.trim())) {
                        topic.getLabels().add(val.trim());
                    }
                }
            } else {
                String text = lElem.getTextContent();
                if (text != null && !text.isBlank() && !topic.getLabels().contains(text.trim())) {
                    topic.getLabels().add(text.trim());
                }
            }
        }
        NodeList topicLabelNodes = topicElem.getElementsByTagName("TopicLabel");
        for (int i = 0; i < topicLabelNodes.getLength(); i++) {
            String val = topicLabelNodes.item(i).getTextContent();
            if (val != null && !val.isBlank() && !topic.getLabels().contains(val.trim())) {
                topic.getLabels().add(val.trim());
            }
        }

        // 3. Parse Comments (search everywhere in document)
        NodeList commentNodes = root.getElementsByTagName("Comment");
        for (int i = 0; i < commentNodes.getLength(); i++) {
            Element cElem = (Element) commentNodes.item(i);
            BcfComment comment = new BcfComment();
            if (cElem.hasAttribute("Guid")) comment.setGuid(cElem.getAttribute("Guid"));
            comment.setDate(findFirstTextByTagName(cElem, "Date", "CreationDate"));
            comment.setAuthor(findFirstTextByTagName(cElem, "Author", "CreationAuthor"));
            comment.setComment(findFirstTextByTagName(cElem, "Comment", "Text"));
            comment.setModifiedDate(findFirstTextByTagName(cElem, "ModifiedDate"));
            comment.setModifiedAuthor(findFirstTextByTagName(cElem, "ModifiedAuthor"));
            comment.setStatus(findFirstTextByTagName(cElem, "Status"));
            comment.setPriority(findFirstTextByTagName(cElem, "Priority"));

            NodeList vpNodes = cElem.getElementsByTagName("Viewpoint");
            if (vpNodes.getLength() > 0) {
                Element vpElem = (Element) vpNodes.item(0);
                if (vpElem.hasAttribute("Guid")) {
                    comment.setViewpointGuid(vpElem.getAttribute("Guid"));
                } else if (!vpElem.getTextContent().isBlank()) {
                    comment.setViewpointGuid(vpElem.getTextContent().trim());
                }
            }
            topic.getComments().add(comment);
        }

        // Sort comments chronologically
        topic.getComments().sort((c1, c2) -> {
            if (c1.getDate() != null && c2.getDate() != null) {
                return c1.getDate().compareTo(c2.getDate());
            }
            return 0;
        });

        // 4. Parse Viewpoints
        List<Element> viewpointElements = new ArrayList<>();
        NodeList allNodes = root.getElementsByTagName("*");
        for (int i = 0; i < allNodes.getLength(); i++) {
            Element elem = (Element) allNodes.item(i);
            String name = elem.getTagName();
            if (name.equalsIgnoreCase("ViewPoint") || name.equalsIgnoreCase("Viewpoints") || name.equalsIgnoreCase("Viewpoint")) {
                // Check if this element contains child viewpoint elements (i.e. is a container)
                boolean isContainer = false;
                Node child = elem.getFirstChild();
                while (child != null) {
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        String childName = ((Element) child).getTagName();
                        if (childName.equalsIgnoreCase("ViewPoint") || childName.equalsIgnoreCase("Viewpoints")) {
                            isContainer = true;
                            break;
                        }
                    }
                    child = child.getNextSibling();
                }

                // Check if this is just a leaf tag <Viewpoint>file.bcfv</Viewpoint> inside a viewpoint
                boolean isLeafFileTag = false;
                Node parent = elem.getParentNode();
                if (parent instanceof Element parentElem) {
                    String pName = parentElem.getTagName();
                    if (pName.equalsIgnoreCase("ViewPoint") || pName.equalsIgnoreCase("Viewpoints")) {
                        // If elem only contains text (.bcfv), it is a leaf tag
                        if (findDirectChildElement(elem, "Snapshot") == null && !elem.hasAttribute("Guid")) {
                            isLeafFileTag = true;
                        }
                    }
                }

                if (!isContainer && !isLeafFileTag) {
                    if (findDirectChildElement(elem, "Snapshot") != null 
                            || findDirectChildElement(elem, "Viewpoint") != null 
                            || elem.hasAttribute("Guid")) {
                        if (!viewpointElements.contains(elem)) {
                            viewpointElements.add(elem);
                        }
                    }
                }
            }
        }

        for (Element vpElem : viewpointElements) {
            BcfViewpoint vp = new BcfViewpoint();
            if (vpElem.hasAttribute("Guid")) vp.setGuid(vpElem.getAttribute("Guid"));
            vp.setViewpointFile(findFirstTextByTagName(vpElem, "Viewpoint", "ViewPoint"));
            vp.setSnapshotFile(findFirstTextByTagName(vpElem, "Snapshot", "SnapshotFileName"));

            String idx = findFirstTextByTagName(vpElem, "Index");
            if (idx != null && !idx.isBlank()) {
                try {
                    vp.setIndex(Integer.parseInt(idx.trim()));
                } catch (NumberFormatException ignored) {}
            }

            // Resolve snapshot image binary data from folderFiles
            byte[] imgBytes = findImageData(folderFiles, vp.getSnapshotFile(), vp.getGuid());
            if (imgBytes != null) {
                vp.setSnapshotData(imgBytes);
            }

            topic.getViewpoints().add(vp);
        }

        // Fallback: If no viewpoints defined in XML, check folderFiles for any images (.png / .jpg / .jpeg)
        if (topic.getViewpoints().isEmpty()) {
            for (Map.Entry<String, byte[]> f : folderFiles.entrySet()) {
                String fname = f.getKey().toLowerCase();
                if (fname.endsWith(".png") || fname.endsWith(".jpg") || fname.endsWith(".jpeg")) {
                    BcfViewpoint vp = new BcfViewpoint();
                    vp.setSnapshotFile(f.getKey());
                    vp.setSnapshotData(f.getValue());
                    topic.getViewpoints().add(vp);
                }
            }
        } else {
            // Also ensure each viewpoint without snapshotData tries to grab an unmatched image
            for (BcfViewpoint vp : topic.getViewpoints()) {
                if (vp.getSnapshotData() == null) {
                    byte[] imgBytes = findImageData(folderFiles, vp.getSnapshotFile(), vp.getGuid());
                    if (imgBytes != null) {
                        vp.setSnapshotData(imgBytes);
                    }
                }
            }
        }

        return topic;
    }

    private byte[] findImageData(Map<String, byte[]> folderFiles, String snapshotFile, String guid) {
        if (snapshotFile != null && !snapshotFile.isBlank()) {
            String cleanName = snapshotFile.replace('\\', '/');
            int idx = cleanName.lastIndexOf('/');
            if (idx >= 0) cleanName = cleanName.substring(idx + 1);

            for (Map.Entry<String, byte[]> entry : folderFiles.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(cleanName) || entry.getKey().endsWith("/" + cleanName)) {
                    return entry.getValue();
                }
            }
        }

        if (guid != null && !guid.isBlank()) {
            for (Map.Entry<String, byte[]> entry : folderFiles.entrySet()) {
                String key = entry.getKey().toLowerCase();
                if (key.contains(guid.toLowerCase()) && (key.endsWith(".png") || key.endsWith(".jpg") || key.endsWith(".jpeg"))) {
                    return entry.getValue();
                }
            }
        }

        // General fallback: snapshot.png / snapshot.jpg
        for (Map.Entry<String, byte[]> entry : folderFiles.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (key.endsWith("snapshot.png") || key.endsWith("snapshot.jpg") || key.endsWith("snapshot.jpeg")) {
                return entry.getValue();
            }
        }

        return null;
    }

    private Document parseXml(byte[] xmlBytes) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false); // Ignore namespaces for robust cross-version tag matching
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlBytes));
    }

    private Element findDirectChildElement(Element parent, String tagName) {
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) child;
                if (elem.getTagName().equalsIgnoreCase(tagName)) {
                    return elem;
                }
            }
            child = child.getNextSibling();
        }
        return null;
    }

    private String findFirstTextByTagName(Element parent, String... tagNames) {
        for (String tag : tagNames) {
            NodeList list = parent.getElementsByTagName(tag);
            if (list.getLength() > 0) {
                String text = list.item(0).getTextContent();
                if (text != null && !text.isBlank()) {
                    return text.trim();
                }
            }
        }
        return null;
    }
}
