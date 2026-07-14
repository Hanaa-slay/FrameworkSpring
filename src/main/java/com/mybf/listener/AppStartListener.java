package com.mybf.listener;

import com.mybf.annotation.Controller;
import com.mybf.annotation.UrlMapping;
import com.mybf.utils.RouteMapping;
import com.mybf.utils.UrlMethod;
import com.mybf.utils.Utilitaire;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@WebListener
public class AppStartListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        Utilitaire util = new Utilitaire();
        String packageName = context.getInitParameter("packageController");
        String prefix = context.getInitParameter("prefix");
        String suffix = context.getInitParameter("suffix");

        try {
            //List<String> controllers = util.getListController(packageName, Controller.class);
            // List<Class<?>> controllers = util.getListControllerClass(packageName, Controller.class);

            Map<UrlMethod, RouteMapping> routes = new HashMap<>();

            util.scanControllersInPackage(packageName, routes, Controller.class, UrlMapping.class);

            context.setAttribute("routes", routes);
            context.setAttribute("prefix", prefix);
            context.setAttribute("suffix", suffix);

            // context.setAttribute("controllers", controllers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}
