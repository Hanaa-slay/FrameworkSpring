package com.mnemos.servlet;

import com.mnemos.annotation.Controller;
import com.mnemos.annotation.UrlMapping;
import com.mnemos.utils.Utilitaire;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class FrontControllerServlet extends HttpServlet {

    List<String> listController;

    public void init(){
        /* Raha tsy hampiasa listener sinon any ambany */
//        Utilitaire util = new Utilitaire();
//        String packageName = getInitParameter("packageController");
//        try {
//            listController = util.getListController(packageName, Controller.class);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        // D ny init-param anaty web.xml ovaina context-param

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String projectPath = req.getContextPath();
        String URL = req.getRequestURI();
        URL = URL.substring(projectPath.length());
        processRequest(resp, URL);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getContextPath();
        processRequest(resp, URL);
    }

    private void processRequest(HttpServletResponse res, String url) throws IOException {
        String[] validUrl = new String[]{"","/","/salut", "/hello", "/emp/list", "/emp/new"};

        if(!Utilitaire.isUrlValid(url, Arrays.stream(validUrl).toList())){
            StringJoiner sj = new StringJoiner(", ");
            for(String s: validUrl){
                sj.add(s);
            }
            throw new RuntimeException("L'url que vours avez entré: "+url+" n'est pas valide\n"+"Les url valident sont : "+ sj);
        }

//        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        List<Class<?>> controllers = (List<Class<?>>) getServletContext().getAttribute("controllers");
        Map<String, List<Method>> methodAssocieUrl = (new Utilitaire()).getMethodWithUrl(url, UrlMapping.class, controllers);

        out.println("URL: "+url);

        for(Map.Entry<String, List<Method>> entry: methodAssocieUrl.entrySet()){
            String clazz = entry.getKey();
            List<Method> methods = entry.getValue();
            out.println(clazz+":");
            for(Method m: methods){
                out.println("\t"+m.getName());
            }
        }
    }
}
