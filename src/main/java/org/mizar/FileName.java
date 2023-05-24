package org.mizar;

public class FileName {
    private String canonicalFileName;
    private String simpleFileName;
    private String simpleFileNameWithoutExtension;
    private String dirName;
    private String extension;
    private String dirSeparator = "/";

    public FileName(String canonicalFileName) {
        this.canonicalFileName = canonicalFileName;
        this.normalizeDirSeparator();
        this.simpleFileName = this.getSimpleFileName();
        this.simpleFileNameWithoutExtension = this.getSimpleFileNameWithoutExtension();
        this.dirName = this.getDirName();
        this.extension = this.getExtension();
    }

    private void normalizeDirSeparator() {
        this.canonicalFileName = this.canonicalFileName.replace("\\", this.dirSeparator);
    }

    public String getSimpleFileName() {
        if (this.canonicalFileName.contains(this.dirSeparator)) {
            String[] tab = this.canonicalFileName.split(this.dirSeparator);
            return tab[tab.length - 1];
        } else {
            return this.canonicalFileName;
        }
    }

    public String getCanonicalFileNameWithoutExtension() {
        return this.canonicalFileName.lastIndexOf(".") != -1 ? this.canonicalFileName.substring(0, this.canonicalFileName.lastIndexOf(".")) : this.canonicalFileName;
    }

    public String getSimpleFileNameWithoutExtension() {
        return this.simpleFileName.lastIndexOf(".") != -1 ? this.simpleFileName.substring(0, this.simpleFileName.lastIndexOf(".")) : this.simpleFileName;
    }

    public String getDirName() {
        if (!this.canonicalFileName.contains(this.dirSeparator)) {
            return this.canonicalFileName;
        } else {
            String[] tab = this.canonicalFileName.split(this.dirSeparator);
            String result = "";

            for(int i = 0; i < tab.length - 1; ++i) {
                result = result + tab[i] + this.dirSeparator;
            }

            return result;
        }
    }

    public String getExtension() {
        return this.canonicalFileName.lastIndexOf(".") != -1 ? this.canonicalFileName.substring(this.canonicalFileName.lastIndexOf(".")) : "";
    }

    public String toString() {
        return this.canonicalFileName;
    }

    public String getCanonicalFileName() {
        return this.canonicalFileName;
    }

    public String getDirSeparator() {
        return this.dirSeparator;
    }
}
