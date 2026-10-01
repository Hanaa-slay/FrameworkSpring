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
import com.mybf.view.ViewResolver;
import com.mybf.utils.Mapping;

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


    List<String> listeControllers;

    Map<UrlMethod, Mapping> urlMapping;

    Object applicationContext;


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


    protected void afficher(String url, String method, HttpServletRequest request, HttpServletResponse response,
            PrintWriter out)
            throws ServletException, IOException {

        UrlMethod urlMethod = new UrlMethod(url, method);
        Mapping mapping = urlMapping.get(urlMethod);

        if (mapping != null) {
            
            try {
                Object instance = mapping.getClasse().getDeclaredConstructor().newInstance();
                Method methode = mapping.getMethode();
                boolean is_apiRest = Utilitaire.estApiRest(methode);

                if(is_apiRest){
                    response.setContentType("application/json");
                }
                else
                {
                    response.setContentType("text/html");

                    out.println("<h1>Front Controller</h1>");
                    out.println("<p>URL recue : " + url + "</p>");
                    out.println("<p>URL: " + urlMethod.getUrl() + " avec la methode : " + urlMethod.getMethod() + "| Classe: "
                    + mapping.getClasse().getName() + " | Fonction: "
                    + mapping.getMethode().getName() + "</p>");
                }

                Object[] arguments = new Object[methode.getParameters().length];

                // MIla jerena oe inona daholo ny parametres an'ilay methode
                if(applicationContext != null){
                    Utilitaire.creerArguments(methode, arguments, applicationContext);
                }else{
                    Utilitaire.creerArguments(methode, arguments);
                }

                Object resultat = methode.invoke(instance, arguments);

                // tokony mbola misy condition hoe raha apiRest dia json no averina fa raha tsy apiRest dia ModelAndView no averina
                if (resultat instanceof ModelAndView) {
                    ModelAndView mv = (ModelAndView) resultat;
                    ViewResolver viewResolver = new ViewResolver();
                    viewResolver.setNom_vue(mv.getUrl());
                    viewResolver.setPrefix_vue(getServletContext().getInitParameter("prefixVue"));
                    viewResolver.setExtension_vue(getServletContext().getInitParameter("suffixVue"));

                    for (Map.Entry<String, Object> entry : mv.getAttribute().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }

                    RequestDispatcher dispatcher = request.getRequestDispatcher(viewResolver.getCheminCompletVue());
                    dispatcher.forward(request, response);
                } else if(is_apiRest){
                    // out.println("<p>Le résultat de la méthode n'est pas de type ModelAndView.</p>");
                    if(resultat instanceof String){
                        out.println((String) resultat);
                    }else{
                        out.println(Utilitaire.toJson(resultat));
                    }
                }

            } catch (Exception e) {
                out.println("<p>Erreur lors de l'invocation de la méthode : " + e.getMessage() + "</p>");
            }
        } else {
            out.println("Url non trouvee : " + url);
            out.println("<h2>Liste des URL disponibles :</h2>");
            for (UrlMethod urlMethodDisponible : urlMapping.keySet()) {
                Mapping mappingDisponible = urlMapping.get(urlMethodDisponible);
                out.println("<p>URL: " + urlMethodDisponible.getUrl() + " avec la methode : "
                        + urlMethodDisponible.getMethod() + "| Classe: " + mappingDisponible.getClasse().getName()
                        + " | Fonction: "
                        + mappingDisponible.getMethode().getName() + "</p>");
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
