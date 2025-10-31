package org.itmo;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.servlet.DispatcherServlet;
import org.eclipse.jetty.server.Server;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.itmo.config.WebConfig;

import jakarta.servlet.MultipartConfigElement; // <-- НОВЫЙ ИМПОРТ ДЛЯ MULTIPART
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer; // <-- НОВЫЙ ИМПОРТ ДЛЯ WEBSOCKET

import java.io.IOException;
import java.net.ServerSocket;

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
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // 1. РЕШЕНИЕ ПРОБЛЕМЫ WEBSOCKET (JSR-356)
        // Инициализация WebSocket-контейнера для Jetty 11 с Jakarta API.
        JakartaWebSocketServletContainerInitializer.configure(context, (servletContext, container) -> {
            // Конфигурация по умолчанию для ServerContainer
        });

        server.setHandler(context);


        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        ctx.register(WebConfig.class);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(ctx);
        ServletHolder springServletHolder = new ServletHolder(dispatcherServlet); // <-- Используем ServletHolder

        // 2. РЕШЕНИЕ ПРОБЛЕМЫ MULTIPART (ЗАГРУЗКА ФАЙЛОВ)
        // Для обработки MultipartFile (MultipartException: Failed to parse multipart)
        // Устанавливаем конфигурацию MultipartConfigElement для DispatcherServlet.
        // 'null' означает использование временной директории по умолчанию.
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