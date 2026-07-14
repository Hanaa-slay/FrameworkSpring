package com.mybf.servlet;

import com.mybf.annotation.Controller;
import com.mybf.annotation.UrlMapping;
import com.mybf.utils.RouteMapping;
import com.mybf.utils.UrlMethod;
import com.mybf.utils.Utilitaire;
import jakarta.servlet.ServletException;
import jakarta.servlet.RequestDispatcher;
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

    private Utilitaire util;
    private List<Class<?>> controllers;
    private Map<UrlMethod, RouteMapping> routes;
    private String suffix;
    private String prefix;



    public void init(){
        util = new Utilitaire();
        // controllers = (List<Class<?>>) getServletContext().getAttribute("controllers");
        routes = (Map<UrlMethod, RouteMapping>) getServletContext().getAttribute("routes");
        prefix = getServletContext().getAttribute("prefix").toString();
        suffix = getServletContext().getAttribute("suffix").toString();

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String projectPath = req.getContextPath();
        String URI = req.getRequestURI();
        String method = req.getMethod();
        String URL = URI.substring(projectPath.length());
        processRequest(req, resp, URL, method);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String projectPath = req.getContextPath();
        String URL = req.getRequestURI();
        String method = req.getMethod();
        URL = URL.substring(projectPath.length());
        processRequest(req, resp, URL, method);
    }


    private void processRequest(HttpServletRequest req, HttpServletResponse res, String url, String method) throws IOException, ServletException {
        UrlMethod um = new UrlMethod(url, method);
        RouteMapping routeMapping = util.getByUrlMethod(um, routes);
        if(req.getMethod().equals("GET")){
            Object objectView = null;
            if((objectView = util.invoke(routeMapping)) instanceof ModelAndView){
                ModelAndView modelAndView = (ModelAndView) objectView;
                Map<String, Object> attributes = modelAndView.getListAttributes();
                String view = prefix+modelAndView.getUrl()+suffix;  
                for(Map.Entry<String, Object> entry: attributes.entrySet()){
                    req.setAttribute(entry.getKey(), entry.getValue());
                }
                RequestDispatcher dispatcher = req.getRequestDispatcher(view);
                dispatcher.forward(req, res);
            }
        }
    }

// //        res.setContentType("text/html");
//         PrintWriter out = res.getWriter();
// //        Map<String, List<Method>> methodAssocieUrl = util.getMethodWithUrl(url, UrlMapping.class, controllers);
// //        Map<String, RouteMapping> methodUrl = util.getMethodAssocieUrl(url, UrlMapping.class, controllers);

//         Map<UrlMethod, RouteMapping> routes = util.getMethods(url, UrlMapping.class, controllers);

//         for(Map.Entry<UrlMethod, RouteMapping> entry: routes.entrySet()){
//             UrlMethod urlMethod = entry.getKey();
//             RouteMapping route = entry.getValue();

//             out.println("URL: "+urlMethod.getUrl()+" | method: "+urlMethod.getMethod()+" | Controller: "+route.getController().getName()+" | Method: "+route.getMethod().getName());

//             if(method.equals(urlMethod.getMethod())){
//                 try{
//                     Object returnValue =  util.invoke(route);
//                     out.println("Valeur retournee: " + returnValue);
//                 } catch (Exception e) {
//                     throw new RuntimeException(e);
//                 }
//             }
//         }


// //        for(Map.Entry<String, RouteMapping> entry: methodUrl.entrySet()){
// //            String urlAssocie = entry.getKey();
// //            RouteMapping route = entry.getValue();
// //            out.println("URL: "+url+" | Controller: "+route.getController()+" | Method: "+route.getMethod().getName());
// //        }

// //        out.println("URL: "+url);

// //        for(Map.Entry<String, List<Method>> entry: methodAssocieUrl.entrySet()){
// //            String clazz = entry.getKey();
// //            List<Method> methods = entry.getValue();
// //            out.println(clazz+":");
// //            for(Method m: methods){
// //                out.println("\t"+m.getName());
// //            }
// //        }
//     }
}
