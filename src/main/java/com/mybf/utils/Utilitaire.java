package com.mybf.utils;

import com.mybf.annotation.UrlMapping;
import com.mybf.exception.UrlNotFoundException;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class Utilitaire {
    private List<Class<?>> listController;
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
}
