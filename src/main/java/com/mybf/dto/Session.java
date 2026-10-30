package com.mybf.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Session manipulee par le controller, independante de HttpSession :
// le FrontControllerServlet la remplit avant l'appel et recopie son contenu dans HttpSession apres
public class Session {
    private final Map<String, Object> donnees = new HashMap<>();

    public Object get(String cle) {
        return donnees.get(cle);
    }

    public void set(String cle, Object valeur) {
        donnees.put(cle, valeur);
    }

    public void remove(String cle) {
        donnees.remove(cle);
    }

    public boolean contains(String cle) {
        return donnees.containsKey(cle);
    }

    public void clear() {
        donnees.clear();
    }

    public Map<String, Object> getAll() {
        return Collections.unmodifiableMap(donnees);
    }

    @Override
    public String toString() {
        return "Session" + donnees;
    }
}
