package org.mizar;

public class UnknownAttributeException extends RuntimeException {
    String name;

    public UnknownAttributeException(String name) {
        this.name = name;
    }

    public String toString() {
        return "Unknown attribute: " + this.name;
    }
}
