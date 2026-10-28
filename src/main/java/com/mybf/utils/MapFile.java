package com.mybf.utils;

public class MapFile {
    private String nom_fichier;
    private String extension;
    private byte[] bytes;

    public MapFile() {}

    public MapFile(String nom_fichier, String extension, byte[] bytes) {
        this.nom_fichier = nom_fichier;
        this.extension = extension;
        this.bytes = bytes;
    }

    public String getNom_fichier() {
        return nom_fichier;
    }

    public void setNom_fichier(String nom_fichier) {
        this.nom_fichier = nom_fichier;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public String toString() {
        return "MapFile[" + nom_fichier + ", extension=" + extension + ", taille="
                + (bytes == null ? 0 : bytes.length) + " octets]";
    }
}
