package com.example.victorsport;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JspViewsTests {
    @Test
    void bienDichTatCaTrangVaIncludeJsp() throws Exception {
        Path webapp = Path.of("src/main/webapp").toAbsolutePath();
        var tomcat = new Tomcat();
        tomcat.setBaseDir(Path.of("target/jsp-test-tomcat").toAbsolutePath().toString());
        tomcat.setPort(0);
        tomcat.getConnector().setProperty("address", "127.0.0.1");
        var context = tomcat.addWebapp("", webapp.toString());
        context.setParentClassLoader(getClass().getClassLoader());
        Tomcat.addServlet(context, "kiemTraJsp", new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest request, HttpServletResponse response)
                    throws java.io.IOException, jakarta.servlet.ServletException {
                String page = request.getParameter("page");
                if (page == null || !page.startsWith("/WEB-INF/view/") || page.contains("..")) {
                    response.sendError(400);
                    return;
                }
                request.getRequestDispatcher(page).forward(request, response);
            }
        });
        context.addServletMappingDecoded("/kiem-tra-jsp", "kiemTraJsp");
        try {
            tomcat.start();
            var client = HttpClient.newHttpClient();
            var errors = new ArrayList<String>();
            try (var files = Files.walk(webapp.resolve("WEB-INF/view"))) {
                for (Path file : files.filter(p -> p.toString().endsWith(".jsp")).toList()) {
                    String page = "/" + webapp.relativize(file).toString().replace('\\', '/');
                    URI uri = URI.create("http://127.0.0.1:" + tomcat.getConnector().getLocalPort()
                            + "/kiem-tra-jsp?jsp_precompile=true&page=" + page);
                    var result = client.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());
                    if (result.statusCode() != 200) errors.add(page + ": " + result.statusCode());
                }
            }
            assertTrue(errors.isEmpty(), String.join("\n", errors));
        } finally {
            tomcat.stop();
            tomcat.destroy();
        }
    }
}
