package com.example.balloon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Раздаёт собранный фронтенд (frontend/.output/public попадает в jar как static/).
 * Неизвестные пути SPA отдают index.html, а api/ и swagger/ — 404 JSON.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new SpaResourceResolver());
    }

    private static class SpaResourceResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            if (resourcePath.startsWith("api/") || resourcePath.startsWith("swagger/")) {
                return null;
            }
            Resource resource = super.getResource(resourcePath, location);
            return resource != null ? resource : super.getResource("index.html", location);
        }
    }
}
