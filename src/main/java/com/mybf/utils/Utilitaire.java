package com.mybf.utils;

import java.io.File;

import com.mybf.annotation.UrlMapping;
import com.mybf.exception.UrlNotFoundException;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Method;

import com.google.gson.Gson;

public class Utilitaire {
    private List<Class<?>> listController;

    private String nom_package;
    private String annotation;
    private ElementType niveau;


    public String getNom_package() {
        return nom_package;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public ElementType getNiveau() {
        return niveau;
    }

    public void setNiveau(ElementType niveau) {
        this.niveau = niveau;
    }

    public void setNom_package(String nom_package) {
        this.nom_package = nom_package;
    }

    public static void recupererClasses(String nomPackage, List<Class<?>> classes) throws Exception {

        String cheminDossier = nomPackage.replace('.', '/');

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL ressource = classLoader.getResource(cheminDossier);

        if (ressource == null) {
            throw new IllegalArgumentException("Le package " + nomPackage + " n'existe pas.");
        }

        File dossier = new File(ressource.toURI());

        if (dossier.exists() && dossier.isDirectory()) {
            File[] fichiers = dossier.listFiles();
            if (fichiers != null) {
                for (File fichier : fichiers) {
                    if (fichier.isFile() && fichier.getName().endsWith(".class")) {
                        String nomClasse = nomPackage + '.'
                                + fichier.getName().substring(0, fichier.getName().length() - 6);

                        classes.add(Class.forName(nomClasse));
                    }
                }
            }
        }
    }

    public static void recupererClassesAvecAnnotation(Utilitaire utilitaire, List<String> listeAvecAnnotation)
            throws Exception {
        try {
            utilitaire.recupererElements(utilitaire, listeAvecAnnotation);

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erreur lors de la récupération des classes : " + e.getMessage());
        }
    }

    public void recupererElements(Utilitaire utilitaire, List<String> resultat) throws Exception {

        List<Class<?>> classes = new ArrayList<>();
        recupererClasses(utilitaire.getNom_package(), classes);

        Class<?> annotationClass = Class.forName(utilitaire.getAnnotation());

        if (!annotationClass.isAnnotation()) {
            throw new Exception("Ce n'est pas une annotation");
        }

        Class<? extends Annotation> annotation = annotationClass.asSubclass(Annotation.class);

        switch (utilitaire.getNiveau()) {

            case TYPE:
                for (Class<?> classe : classes) {
                    if (classe.isAnnotationPresent(annotation)) {
                        resultat.add(classe.toString());
                    }
                }
                break;

            case FIELD:
                for (Class<?> classe : classes) {
                    for (Field field : classe.getDeclaredFields()) {
                        if (field.isAnnotationPresent(annotation)) {
                            resultat.add(field.toString());
                        }
                    }
                }
                break;

            case METHOD:
                for (Class<?> classe : classes) {
                    for (Method method : classe.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(annotation)) {
                            resultat.add(method.toString());
                        }
                    }
                }
                break;
        }

    }

    public static Map<UrlMethod, Mapping> recupererUrlMapping(Utilitaire utilitaire) throws Exception {
        Map<UrlMethod, Mapping> urlMapping = new HashMap<>();

        List<Class<?>> classes = new ArrayList<>();
        recupererClasses(utilitaire.getNom_package(), classes);

        Class<?> annotationClass = Class.forName(utilitaire.getAnnotation());

        if (!annotationClass.isAnnotation()) {
            throw new Exception("Ce n'est pas une annotation");
        }

        Class<? extends Annotation> annotation = annotationClass.asSubclass(Annotation.class);

        Method valueMethod = annotation.getMethod("value");
        Method methodUrl = annotation.getMethod("method");

        for (Class<?> classe : classes) {
            for (Method method : classe.getDeclaredMethods()) {

                if (method.isAnnotationPresent(annotation)) {

                    Annotation ann = method.getAnnotation(annotation);

                    String url = (String) valueMethod.invoke(ann);
                    String methodOfUrl = (String) methodUrl.invoke(ann);

                    UrlMethod urlMethod = new UrlMethod(url, methodOfUrl);

                    if (urlMapping.containsKey(urlMethod)) {
                        throw new Exception("URL Deja utilise par un autre controller : " + urlMethod.getUrl()
                                + " avec la methode : " + urlMethod.getMethod());
                    }

                    urlMapping.put(urlMethod, new Mapping(classe, method));
                }
            }
        }

        return urlMapping;
    }

    public static void creerArguments(Method methode, Object[] arguments, Object applicationContext) {
        for (int i = 0; i < methode.getParameters().length; i++) {
            Parameter p = methode.getParameters()[i];
            if (applicationContext != null && p.getType().isAssignableFrom(applicationContext.getClass())) {
                arguments[i] = applicationContext;
            }

        }
    }




    public void scanPackage(String packageName) throws IOException, ClassNotFoundException {
        listController = new ArrayList<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/'); // Convertit "com.package" en "com/package"
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        for (File directory : dirs) {
            listController.addAll(findClasses(directory, packageName));
        }
    }

    private List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    classes.add(Class.forName(className));
                }
            }
        }
        return classes;
    }

    public void scanControllersInPackage(String packageName, Map<UrlMethod, RouteMapping> routes, Class<? extends Annotation> annotationController, Class<? extends Annotation> annotationMethod) throws IOException, ClassNotFoundException {
        scanPackage(packageName);

        for(Class<?> c: listController){
            if(c.isAnnotationPresent(annotationController)){
                scanMethod(routes, c, annotationMethod);
            }
        }
    }

    public void scanMethod(Map<UrlMethod, RouteMapping> routes, Class<?> controller, Class<? extends Annotation> annotation){
        if(!annotation.isAssignableFrom(UrlMapping.class)){
            throw new RuntimeException("Invalid annotation type");
        }
        Method[] methods = controller.getMethods();
        for(Method method: methods){
            if(method.isAnnotationPresent(annotation)){
                UrlMapping urlMapping = (UrlMapping) method.getAnnotation(annotation);
                String link = urlMapping.url();
                String requestMethod = urlMapping.method();
                UrlMethod um = new UrlMethod(link, requestMethod);
                RouteMapping route = new RouteMapping(controller, method);

                if(routes.containsKey(um)){
                    throw new RuntimeException("L'url "+link+" est déjà utiliser dans "+routes.get(um).getMethod().getName());
                }
                routes.put(um, route);
            }
        }
    }

    public Object invoke(RouteMapping routeMapping)  {
        try{
            Class<?> controller = routeMapping.getController();
            Object o = controller.getConstructor().newInstance();
            Method method = routeMapping.getMethod();

            return method.invoke(o);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public RouteMapping getByUrlMethod(UrlMethod urlMethod, Map<UrlMethod, RouteMapping> routes) {
        RouteMapping route = routes.get(urlMethod);
        StringBuilder message = new StringBuilder("Aucune methode associer a l'url: "+urlMethod.getUrl()+"\n");
        for (Map.Entry<UrlMethod, RouteMapping> entry : routes.entrySet()) {

            String url = entry.getKey().getUrl();
            String httpMethod = entry.getKey().getMethod();
            String methodName = entry.getValue().getMethod().getName();
            String controllerName = entry.getValue().getController().getSimpleName();

            message.append("URL: ")
                    .append(url)
                    .append("\tMethode: ")
                    .append(methodName)
                    .append("\tMethode HTTP: ")
                    .append(httpMethod)
                    .append("\tClasse: ")
                    .append(controllerName)
                    .append("\n");
        }
        if(route == null){
            throw new UrlNotFoundException(message.toString());
        }
        return route;
    }



    public static void creerArguments(Method methode, Object[] arguments) {
        for (int i = 0; i < methode.getParameters().length; i++) {
            Parameter p = methode.getParameters()[i];
        }
    }

    public static boolean estApiRest(Method methode) {
        return methode.isAnnotationPresent(com.mybf.annotation.ApiRest.class);
    }

    public static String toJson(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }
}
