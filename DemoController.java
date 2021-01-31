package com.sunset.controller;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.buf.UriUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

@RestController
@RequestMapping("/demo")
public class DemoController implements ServletContextAware {

    private ServletContext servletContext;


    @GetMapping("/servlet")
    public Set<String> test() {
        return servletContext.getResourcePaths("/");
    }

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        try {
            Class<? extends ServletContext> contextClass = servletContext.getClass();
            Field servletContextField = contextClass.getDeclaredField("context");
            servletContextField.setAccessible(true);

            ApplicationContext applicationContext = (ApplicationContext) servletContextField.get(servletContext);
            Field applicationContextContextField = applicationContext.getClass().getDeclaredField("context");
            applicationContextContextField.setAccessible(true);
            StandardContext standardContext = (StandardContext) applicationContextContextField.get(applicationContext);
            String classPathResource = new ApplicationHome(this.getClass()).getSource().getAbsolutePath();
            String basePath = "";
            logger.error("222222222" + classPathResource);
            if (classPathResource.startsWith("jar:")) {

                logger.error("11111111111111111" + classPathResource);
                BaseLocation baseLocation = new BaseLocation(new URL(classPathResource));
                basePath = baseLocation.getBasePath();
            } else {
                basePath = classPathResource;
            }
            String internalPath = "/META-INF/resources";
            standardContext.getResources().createWebResourceSet(WebResourceRoot.ResourceSetType.RESOURCE_JAR, "/", basePath, null, internalPath);
        } catch (NoSuchFieldException | IllegalAccessException | MalformedURLException e) {
            e.printStackTrace();
        }
        System.out.println(servletContext);


    }

    static class BaseLocation {

        private final String basePath;
        private final String archivePath;

        BaseLocation(URL url) {
            File f = null;

            if ("jar".equals(url.getProtocol()) || "war".equals(url.getProtocol())) {
                String jarUrl = url.toString();
                int endOfFileUrl = -1;
                if ("jar".equals(url.getProtocol())) {
                    endOfFileUrl = jarUrl.indexOf("!/");
                } else {
                    endOfFileUrl = jarUrl.indexOf(UriUtil.getWarSeparator());
                }
                String fileUrl = jarUrl.substring(4, endOfFileUrl);
                try {
                    f = new File(new URL(fileUrl).toURI());
                } catch (MalformedURLException | URISyntaxException e) {
                    throw new IllegalArgumentException(e);
                }
                int startOfArchivePath = endOfFileUrl + 2;
                if (jarUrl.length() > startOfArchivePath) {
                    archivePath = jarUrl.substring(startOfArchivePath);
                } else {
                    archivePath = null;
                }
            } else if ("file".equals(url.getProtocol())) {
                try {
                    f = new File(url.toURI());
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(e);
                }
                archivePath = null;
            } else {
                throw new IllegalArgumentException("standardRoot.unsupportedProtocol" + url.getProtocol());
            }

            basePath = f.getAbsolutePath();
        }


        String getBasePath() {
            return basePath;
        }


        String getArchivePath() {
            return archivePath;
        }
    }


}
