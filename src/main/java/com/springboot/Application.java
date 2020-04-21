package com.springboot;

import com.springboot.controller.PathTweakingRequestMappingHandlerMapping;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Configuration
  class WebMvcRegistrationsConfig implements WebMvcRegistrations {

    @Override
    public PathTweakingRequestMappingHandlerMapping getRequestMappingHandlerMapping() {
      PathTweakingRequestMappingHandlerMapping handlerMapping = new PathTweakingRequestMappingHandlerMapping();
      handlerMapping.setOrder(0);
      return handlerMapping;
    }
  }
}
