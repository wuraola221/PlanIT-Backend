package PlanIT.PlanIT;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableMethodSecurity(prePostEnabled = true)
@SpringBootApplication
public class PlanItApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanItApplication.class, args);
	}

}
