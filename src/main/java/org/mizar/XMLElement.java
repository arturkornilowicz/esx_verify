package org.mizar;

import org.dom4j.Element;

public class XMLElement {
    private Element element;

    public void preProcess() {
    }

    public void process() {
    }

    public void postProcess() {
    }

    public void run() {
        this.preProcess();
        this.process();
        this.postProcess();
    }

    public XMLElement(Element element) {
        this.element = element;
    }

    public String toString() {
        return "XMLElement(element=" + this.getElement() + ")";
    }

    public Element getElement() {
        return this.element;
    }

    public void setElement(Element element) {
        this.element = element;
    }
}
