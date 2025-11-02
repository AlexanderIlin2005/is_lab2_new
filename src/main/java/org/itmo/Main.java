package org.itmo;

// ВАЖНЫЕ ИМПОРТЫ
import org.eclipse.jetty.webapp.WebAppContext; // <-- НОВЫЙ ИМПОРТ
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import jakarta.servlet.MultipartConfigElement;
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL; // <-- НОВЫЙ ИМПОРТ

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

        // **********************************************
        // !!! КРИТИЧЕСКОЕ ИЗМЕНЕНИЕ: ИСПОЛЬЗУЕМ WebAppContext !!!
        // **********************************************
        WebAppContext context = new WebAppContext();
        context.setContextPath("/");

        // Указываем, что ресурсная база — это наш JAR-файл
        URL resourceUrl = Main.class.getClassLoader().getResource("/");
        if (resourceUrl != null) {
            context.setResourceBase(resourceUrl.toURI().toString());
        } else {
            // Fallback, если запускается не из JAR
            context.setResourceBase("./src/main/webapp");
        }

        // CRITICAL: Указываем, что нужно сканировать аннотации и инициализаторы
        context.setParentLoaderPriority(true);

        // !!! КРИТИЧЕСКОЕ ИЗМЕНЕНИЕ: ОТКЛЮЧАЕМ СТАНДАРТНЫЙ WEB.XML !!!
        // Это предотвратит автоматическую загрузку IntrospectorCleaner, которого нет
        context.setDefaultsDescriptor(null);

        // ИСПОЛЬЗУЕМ ТОЛЬКО МИНИМАЛЬНЫЕ КОНФИГУРАТОРЫ
        context.setConfigurationClasses(new String[]{
                // Нужен для поиска ServletContainerInitializer (AppInitializer)
                "org.eclipse.jetty.annotations.AnnotationConfiguration",
                // Этого достаточно, если нет web.xml
        });

        // 1. РЕШЕНИЕ ПРОБЛЕМЫ WEBSOCKET (JSR-356) - теперь WebAppContext сам справится
        // JakartaWebSocketServletContainerInitializer.configure(context, ...);
        // Если вы оставляете это, убедитесь, что context.setAttribute() не перекрывает WebAppContext

        server.setHandler(context);

        // Spring Web Context (WebConfig)
        AnnotationConfigWebApplicationContext webCtx = new AnnotationConfigWebApplicationContext();
        webCtx.register(org.itmo.config.WebConfig.class);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(webCtx);
        ServletHolder springServletHolder = new ServletHolder(dispatcherServlet);

        // 2. РЕШЕНИЕ ПРОБЛЕМЫ MULTIPART (ЗАГРУЗКА ФАЙЛОВ)
        MultipartConfigElement multipartConfig = new MultipartConfigElement((String) null);
        springServletHolder.getRegistration().setMultipartConfig(multipartConfig);

        // ДОБАВЛЯЕМ DispatcherServlet в WebAppContext
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