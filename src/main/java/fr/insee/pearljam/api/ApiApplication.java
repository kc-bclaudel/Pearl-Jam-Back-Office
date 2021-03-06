package fr.insee.pearljam.api;

import java.util.Arrays;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import fr.insee.pearljam.api.repository.SurveyUnitRepository;

@SpringBootApplication(scanBasePackages = "fr.insee.pearljam.api")
@EnableJpaRepositories(basePackageClasses = SurveyUnitRepository.class)
public class ApiApplication extends SpringBootServletInitializer{
	private static final Logger LOGGER = LoggerFactory.getLogger(ApiApplication.class);

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ApiApplication.class);
		app.run(args);
	}
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		setProperties(); 
		return application.sources(ApiApplication.class);
	}
	
	public static void setProperties() {
		System.setProperty("spring.config.location",
				"classpath:/,"
				+ "file:///${catalina.base}/webapps/pearljam-bo.properties");
	}
	
	@EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {

        final Environment env = event.getApplicationContext().getEnvironment();
        LOGGER.info("================================ Properties =================================");
        final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();
        StreamSupport.stream(sources.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .distinct()
                .filter(prop -> !(prop.contains("credentials") || prop.contains("password")))
                .filter(prop -> prop.startsWith("fr.insee") || prop.startsWith("logging") || prop.startsWith("keycloak") || prop.startsWith("spring") || prop.startsWith("application"))
                .sorted()
                .forEach(prop -> LOGGER.info("{}: {}", prop, env.getProperty(prop)));
        LOGGER.info("============================================================================");
    }

}
