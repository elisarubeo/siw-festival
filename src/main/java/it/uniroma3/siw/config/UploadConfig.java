package it.uniroma3.siw.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import it.uniroma3.siw.service.ImageStorageService;

@Configuration
public class UploadConfig implements WebMvcConfigurer {

    private final ImageStorageService imageStorageService;

    public UploadConfig(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String cartella = this.imageStorageService.getDirectory().toUri().toString();
        if (!cartella.endsWith("/")) {
            cartella = cartella + "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(cartella);
    }
}
