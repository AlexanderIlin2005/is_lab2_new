package org.itmo;


import org.eclipse.jetty.webapp.WebAppContext; 
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import jakarta.servlet.MultipartConfigElement;
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL; 



import org.eclipse.jetty.servlet.FilterHolder;
import org.springframework.web.filter.DelegatingFilterProxy; 
import jakarta.servlet.DispatcherType;
import java.util.EnumSet;


public class Main {
    private static final int START_PORT = 8080;
    private static final int END_PORT = 10000;

    public static void main(String[] args) throws Exception {
        int port = findFreePort(START_PORT, END_PORT);
        if (port == -1) {
            throw new IllegalStateException("Нет свободных портов в диапазоне " + START_PORT + "-" + END_PORT);
        }

        System.out.println("Starting server on port: " + port);

        Server server = new Server(port);

        
        
        
        WebAppContext context = new WebAppContext();
        context.setContextPath("/");

        
        URL resourceUrl = Main.class.getClassLoader().getResource("/");
        if (resourceUrl != null) {
            context.setResourceBase(resourceUrl.toURI().toString());
        } else {
            
            context.setResourceBase("./src/main/webapp");
        }

        
        context.setParentLoaderPriority(true);

        
        
        context.setDefaultsDescriptor(null);

        
        context.setConfigurationClasses(new String[]{
                
                "org.eclipse.jetty.annotations.AnnotationConfiguration",
                
        });

        
        
        
        
        FilterHolder springSecurityFilter = new FilterHolder(DelegatingFilterProxy.class);
        
        springSecurityFilter.setInitParameter("targetBeanName", "springSecurityFilterChain");

        
        context.addFilter(springSecurityFilter, "/*", EnumSet.of(DispatcherType.REQUEST));
        

        
        
        

        server.setHandler(context);

        
        AnnotationConfigWebApplicationContext webCtx = new AnnotationConfigWebApplicationContext();
        webCtx.register(org.itmo.config.WebConfig.class);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(webCtx);
        ServletHolder springServletHolder = new ServletHolder(dispatcherServlet);

        
        MultipartConfigElement multipartConfig = new MultipartConfigElement((String) null);
        springServletHolder.getRegistration().setMultipartConfig(multipartConfig);

        
        context.addServlet(springServletHolder, "/*");

        server.start();
        server.join();
    }

    private static int findFreePort(int start, int end) {
        for (int port = start; port <= end; port++) {
            try (ServerSocket socket = new ServerSocket(port)) {
                socket.setReuseAddress(true);
                return port;
            } catch (IOException ignored) {

            }
        }
        return -1;
    }
}