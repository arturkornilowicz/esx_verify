package org.mizar;

import java.io.File;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class XMLApplication {
    private FileName fileName;
    private File inputFile;
    private Document document;

    public XMLApplication(String fileName) {
        this.fileName = new FileName(fileName);
        this.loadInputFile();
    }

    public void loadInputFile() {
        try {
            System.out.println("Loading file " + this.fileName.getCanonicalFileName());
            this.inputFile = new File(this.fileName.getCanonicalFileName());
            SAXReader saxBuilder = new SAXReader();
            this.document = saxBuilder.read(this.inputFile);
        } catch (DocumentException var2) {
            var2.printStackTrace();
        }
    }

    public XMLElement buildTree() { return null; };

    public FileName getFileName() {
        return this.fileName;
    }

    public File getInputFile() {
        return this.inputFile;
    }

    public Document getDocument() {
        return this.document;
    }
}
