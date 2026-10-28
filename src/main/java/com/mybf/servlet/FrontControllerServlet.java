package com.mybf.servlet;

import com.mybf.annotation.Controller;
import com.mybf.annotation.UrlMapping;
import com.mybf.utils.RouteMapping;
import com.mybf.utils.UrlMethod;
import com.mybf.utils.Utilitaire;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.mybf.view.ViewResolver;
import com.mybf.utils.Mapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class FrontControllerServlet extends HttpServlet {

    private Utilitaire util;
    private Map<UrlMethod, RouteMapping> routes;
    private String suffix;
    private String prefix;
    private Object applicationContext;

    @Override
    public void init() {
        util = new Utilitaire();
        routes = (Map<UrlMethod, RouteMapping>) getServletContext().getAttribute("routes");
        prefix = String.valueOf(getServletContext().getAttribute("prefix"));
        suffix = String.valueOf(getServletContext().getAttribute("suffix"));
        applicationContext = getServletContext().getAttribute("applicationContext");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String projectPath = req.getContextPath();
        String uri = req.getRequestURI();
        String url = uri.substring(projectPath.length());
        processRequest(req, resp, url, req.getMethod());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String projectPath = req.getContextPath();
        String url = req.getRequestURI().substring(projectPath.length());
        processRequest(req, resp, url, req.getMethod());
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse res, String url, String method) throws IOException, ServletException {
        if (routes == null) {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Aucune route chargée");
            return;
        }

        UrlMethod um = new UrlMethod(url, method);
        RouteMapping routeMapping = util.getByUrlMethod(um, routes);
        Method methodToInvoke = routeMapping.getMethod();

        Object controllerInstance = null;
        try {
            controllerInstance = routeMapping.getController().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ServletException("Impossible d'instancier le contrôleur : " + routeMapping.getController().getName(), e);
        }

        Object[] arguments = createArguments(req, res, methodToInvoke);

        try {
            Object result = methodToInvoke.invoke(controllerInstance, arguments);

            if (result instanceof ModelAndView) {
                ModelAndView mv = (ModelAndView) result;
                for (Map.Entry<String, Object> entry : mv.getListAttributes().entrySet()) {
                    req.setAttribute(entry.getKey(), entry.getValue());
                }

                String viewPath = prefix + mv.getUrl() + suffix;
                RequestDispatcher dispatcher = req.getRequestDispatcher(viewPath);
                dispatcher.forward(req, res);
                return;
            }

            if (Utilitaire.estApiRest(methodToInvoke)) {
                res.setContentType("application/json; charset=UTF-8");
                PrintWriter out = res.getWriter();
                if (result instanceof String) {
                    out.print((String) result);
                } else {
                    out.print(Utilitaire.toJson(result));
                }
                out.flush();
                return;
            }

            res.setContentType("text/plain; charset=UTF-8");
            PrintWriter out = res.getWriter();
            out.print(String.valueOf(result));
            out.flush();

        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'invocation de la méthode : " + methodToInvoke.getName(), e);
        }
    }

    private Object[] createArguments(HttpServletRequest req, HttpServletResponse res, Method method) throws ServletException {
        Parameter[] parameters = method.getParameters();
        Object[] arguments = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String paramName = parameter.getName();
            Class<?> paramType = parameter.getType();

            if (HttpServletRequest.class.isAssignableFrom(paramType)) {
                arguments[i] = req;
            } else if (HttpServletResponse.class.isAssignableFrom(paramType)) {
                arguments[i] = res;
            } else if (applicationContext != null && paramType.isAssignableFrom(applicationContext.getClass())) {
                arguments[i] = applicationContext;
            } else {
                // Sans l'option -parameters de javac, les noms sont arg0, arg1... et ne matchent jamais la vue
                if (!parameter.isNamePresent()) {
                    throw new ServletException("Nom du parametre introuvable pour " + method.getName()
                            + " : compilez le controller avec l'option javac -parameters");
                }

                String value = req.getParameter(paramName);
                if (value == null || (value.isEmpty() && paramType != String.class)) {
                    arguments[i] = defaultValue(paramType);
                } else {
                    try {
                        arguments[i] = convertValue(value, paramType);
                    } catch (NumberFormatException e) {
                        throw new ServletException("Valeur invalide pour le parametre " + paramName + " : " + value, e);
                    }
                }
                System.out.println("[Binding] " + method.getName() + " -> " + paramName + " = " + arguments[i]);
            }
        }

        return arguments;
    }

    // null par defaut, sauf pour les types primitifs qui ne l'acceptent pas
    private Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == long.class) return 0L;
        if (type == double.class) return 0d;
        if (type == float.class) return 0f;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        return 0;
    }

    private Object convertValue(String value, Class<?> type) {
        if (type == String.class) return value;
        if (type == Integer.class || type == int.class) return Integer.parseInt(value);
        if (type == Long.class || type == long.class) return Long.parseLong(value);
        if (type == Double.class || type == double.class) return Double.parseDouble(value);
        if (type == Float.class || type == float.class) return Float.parseFloat(value);
        if (type == Short.class || type == short.class) return Short.parseShort(value);
        if (type == Byte.class || type == byte.class) return Byte.parseByte(value);
        if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(value);
        if (type == Character.class || type == char.class) return value.charAt(0);
        return value;
    }

    protected void afficher(String url, String method, HttpServletRequest request, HttpServletResponse response,
            PrintWriter out) throws ServletException, IOException {

        UrlMethod urlMethod = new UrlMethod(url, method);
        RouteMapping route = util.getByUrlMethod(urlMethod, routes);

        if (route != null) {
            try {
                Object instance = route.getController().getDeclaredConstructor().newInstance();
                Method methode = route.getMethod();
                boolean isApiRest = Utilitaire.estApiRest(methode);

                if (isApiRest) {
                    response.setContentType("application/json");
                } else {
                    response.setContentType("text/html");
                    out.println("<h1>Front Controller</h1>");
                    out.println("<p>URL recue : " + url + "</p>");
                    out.println("<p>URL: " + urlMethod.getUrl() + " avec la methode : " + urlMethod.getMethod() + "| Classe: "
                            + route.getController().getName() + " | Fonction: "
                            + route.getMethod().getName() + "</p>");
                }

                Object[] arguments = createArguments(request, response, methode);
                Object resultat = methode.invoke(instance, arguments);

                if (resultat instanceof ModelAndView) {
                    ModelAndView mv = (ModelAndView) resultat;
                    ViewResolver viewResolver = new ViewResolver();
                    viewResolver.setNom_vue(mv.getUrl());
                    viewResolver.setPrefix_vue(getServletContext().getInitParameter("prefixVue"));
                    viewResolver.setExtension_vue(getServletContext().getInitParameter("suffixVue"));

                    for (Map.Entry<String, Object> entry : mv.getAttributes().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }

                    RequestDispatcher dispatcher = request.getRequestDispatcher(viewResolver.getCheminCompletVue());
                    dispatcher.forward(request, response);
                } else if (isApiRest) {
                    if (resultat instanceof String) {
                        out.println((String) resultat);
                    } else {
                        out.println(Utilitaire.toJson(resultat));
                    }
                }

            } catch (Exception e) {
                out.println("<p>Erreur lors de l'invocation de la méthode : " + e.getMessage() + "</p>");
            }
        } else {
            out.println("Url non trouvee : " + url);
        }
    }
}
