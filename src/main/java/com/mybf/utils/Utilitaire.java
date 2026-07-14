package com.mybf.utils;

import com.mybf.annotation.UrlMapping;
import com.mybf.exception.RouteMappingException;

import java.beans.MethodDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
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
        for(Class<?> c: controllers){
            Method[] methods = c.getMethods();
            for(Method method: methods){
                if(method.isAnnotationPresent(annotation)){
                    UrlMapping urlMapping = (UrlMapping) method.getAnnotation(annotation);
                    String link = urlMapping.url();
                    if((url.isEmpty() && link.equals("/")) || url.equals(link)){
                        RouteMapping route = new RouteMapping(c, method);
                        if(routes.containsKey(url)){
                            RouteMapping existingRoute = routes.get(url);
                            throw new RouteMappingException("L'url: "+link+" utilisé dans la méthode "+method.getName()
                                +" dans la classe: "+c.getName()+" est déjà utilisé dans la méthode "+existingRoute.getMethod().getName()
                                + " dans la classe: "+existingRoute.getController().getName());
                        }
                        routes.put(url, route);
                    }
                }
            }
        }
        return routes;
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

    public Map<UrlMethod, RouteMapping> getMethods(String url, Class<? extends Annotation> annotation, List<Class<?>> controllers){
        if(!annotation.isAssignableFrom(UrlMapping.class)){
            throw new RuntimeException("Invalid annotation type");
        }

        Map<UrlMethod, RouteMapping> routes = new HashMap<>();

        for(Class<?> controller: controllers){
            Method[] methods = controller.getMethods();
            for(Method method: methods){
                if(method.isAnnotationPresent(annotation)){
                    UrlMapping urlMapping = (UrlMapping) method.getAnnotation(annotation);
                    String link = urlMapping.url();
                    if((url.isEmpty() && link.equals("/")) || url.equals(link)){
                        String requestMethod = urlMapping.method();
                        UrlMethod um = new UrlMethod(link, requestMethod);
                        RouteMapping route = new RouteMapping(controller, method);

                        for(Map.Entry<UrlMethod, RouteMapping> entry: routes.entrySet()){
                            UrlMethod urlMethodExistant = entry.getKey();
                            RouteMapping routeExistant = entry.getValue();
                            if(urlMethodExistant.equals(um)){
                                throw new RuntimeException("La méthode HTTP "+um.getMethod()+" associé au lien "+url+" dans la classe "+controller.getName()
                                        +" est déjà utiliser dans la méthode "+route.getMethod().getName()+" de la classe "+route.getController().getName());
                            }
                        }
                        routes.put(um, route);
                    }
                }
            }
        }

        return routes;
    }

    public List<String> getExistingLink(List<Class<?>> controllers, Class<? extends Annotation> annotation){
        List<String> links = new ArrayList<>();

        for(Class<?> c: controllers){
            Method[] methods = c.getMethods();
            for(Method method: methods){
                if(method.isAnnotationPresent(annotation)){
                    UrlMapping urlMapping = (UrlMapping) method.getAnnotation(annotation);
                    String link = urlMapping.url();
                    if(!links.contains(link)){
                        links.add(link);
                    }
                }
            }
        }

        return links;
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

    public boolean isUrlValid(String url, List<String> urlValid){
        for(String link: urlValid){
            if(url.isEmpty() && link.equals("/"))return true;

            if(url.equals(link)){
                return true;
            }
        }
        return false;
    }
}
