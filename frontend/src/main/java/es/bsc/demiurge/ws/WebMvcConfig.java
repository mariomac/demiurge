package es.bsc.demiurge.ws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.ResourceBundleViewResolver;
import org.springframework.web.servlet.view.XmlViewResolver;

import java.io.IOException;

/**
 * @author Mario Macías (http://github.com/mariomac)
 */
@Configuration
@ComponentScan(basePackages = "es.bsc.demiurge.ws")
@EnableWebMvc
public class WebMvcConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("views/**").addResourceLocations("classpath:/views/");
		registry.addResourceHandler("app/**").addResourceLocations("classpath:/static/app/");
		registry.addResourceHandler("css/**").addResourceLocations("classpath:/static/css/");
		registry.addResourceHandler("images/**").addResourceLocations("classpath:/static/images/");
		registry.addResourceHandler("scripts/**").addResourceLocations("classpath:/static/scripts/");
    }

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		super.configureViewResolvers(registry);
		registry.jsp("views/",".jsp");
	}


}
