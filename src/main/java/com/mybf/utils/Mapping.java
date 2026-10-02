package com.mybf.utils;

import java.lang.reflect.Method;

public class Mapping {
    private Class<?> classe;
    private Method methode;

    public Mapping(Class<?> classe, Method methode) {
        this.classe = classe;
        this.methode = methode;
    }

    public Class<?> getClasse() {
        return this.classe;
    }
    public void setClasse(Class<?> classe) {
        this.classe = classe;
    }

    public Method getMethode() {
        return this.methode;
    }
    public void setMethoe() {
        this.methode = methode;
    }
}
