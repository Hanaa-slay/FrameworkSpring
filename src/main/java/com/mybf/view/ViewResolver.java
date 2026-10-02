package com.mybf.view;

public class ViewResolver {
    private String prefix_vue;
    private String nom_vue;
    private String extension_vue;

    public String getPrefix_vue() {
        return prefix_vue;
    }

    public void setPrefix_vue(String prefix_vue) {
        this.prefix_vue = prefix_vue;
    }

    public String getExtension_vue() {
        return extension_vue;
    }

    public void setExtension_vue(String extension_vue) {
        this.extension_vue = extension_vue;
    }

    public String getNom_vue() {
        return nom_vue;
    }

    public void setNom_vue(String nom_vue) {
        this.nom_vue = nom_vue;
    }

    public String getCheminCompletVue() {
        return prefix_vue + nom_vue + extension_vue;
    }
}
