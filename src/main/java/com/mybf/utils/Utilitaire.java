package com.mnemos.utils;

import com.mnemos.annotation.UrlMapping;

import java.beans.MethodDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class Utilitaire {
    private List<Class<?>> listClass;
    public void scanPackage(String packageName) throws IOException, ClassNotFoundException {
        listClass = new ArrayList<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/'); // Convertit "com.package" en "com/package"
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        for (File directory : dirs) {
            listClass.addAll(findClasses(directory, packageName));
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

    public List<String> getListController(String packageName, Class<? extends Annotation> annotation) throws InstantiationException, IllegalAccessException, IOException, ClassNotFoundException {
        scanPackage(packageName);
        List<String> controllerList = new ArrayList<>();

        for(Class<?> c: listClass){
            if(c.isAnnotationPresent(annotation)){
                String classFullName = c.getName();
                controllerList.add(classFullName);
            }
        }
        return controllerList;
    }

    public List<Class<?>> getListControllerClass(String packageName, Class<? extends Annotation> annotation) throws IOException, ClassNotFoundException {
        scanPackage(packageName);
        List<Class<?>> controllerList = new ArrayList<>();

        for(Class<?> c: listClass){
            if(c.isAnnotationPresent(annotation)){
                controllerList.add(c);
            }
        }
        return controllerList;
    }

    public Map<String, RouteMapping> getMethodAssocieUrl(String url, Class<? extends Annotation> annotation, List<Class<?>> controllers){
        if(!annotation.isAssignableFrom(UrlMapping.class)){
            throw new RuntimeException("Invalid annotation type");
        }

        Map<String, RouteMapping> routes = new HashMap<>();
        return null;
    }

    public Map<String, List<Method>> getMethodWithUrl(String url, Class<? extends Annotation> annotation, List<Class<?>> controllers){
        if(!annotation.isAssignableFrom(UrlMapping.class)){
            throw new RuntimeException("Invalid annotation type");
        }

        Map<String, List<Method>> methodAndClass = new HashMap<>();

        for(Class<?> c: controllers){
            String className = c.getName();
            Method[] methods = c.getMethods();
            List<Method> methodList = new ArrayList<>();
            for(Method method: methods){
                if(method.isAnnotationPresent(annotation)){
                   UrlMapping urlMapping = (UrlMapping) method.getAnnotation(annotation);
                   String link = urlMapping.url();
                   if((url.isEmpty() && link.equals("/")) || url.equals(link)){
                       methodList.add(method);
                   }
                }
            }
            if(!methodList.isEmpty()){
                methodAndClass.put(className, methodList);
            }
        }

        return methodAndClass;
    }

    public static boolean isUrlValid(String url, List<String> urlValid){
        for(String link: urlValid){
            if(url.equals(link)){
                return true;
            }
        }
        return false;
    }
}
