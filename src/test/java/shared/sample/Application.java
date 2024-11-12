package shared.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.zivasd.spring.boot.data.shared.repository.config.EnableSharedRepositories;

@SpringBootApplication
@EnableSharedRepositories(basePackages = "shared.sample.primary.shareddao", entityManagerFactoryRef = "primaryEntityManagerFactory")
@EnableSharedRepositories(basePackages = "shared.sample.secondary.shareddao", entityManagerFactoryRef = "secondaryEntityManagerFactory")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

    }
}