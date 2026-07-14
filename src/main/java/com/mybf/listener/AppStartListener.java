package com.mybf.listener;

import com.mybf.annotation.Controller;
import com.mybf.utils.Utilitaire;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.List;

@WebListener
public class AppStartListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        Utilitaire util = new Utilitaire();
        String packageName = context.getInitParameter("packageController");
        try {
            //List<String> controllers = util.getListController(packageName, Controller.class);
            List<Class<?>> controllers = util.getListControllerClass(packageName, Controller.class);
            context.setAttribute("controllers", controllers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}
