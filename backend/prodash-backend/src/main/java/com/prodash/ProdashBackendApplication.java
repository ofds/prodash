package com.prodash;

// Import the repository class
import com.prodash.infrastructure.adapter.out.persistence.ProposalMongoRepository; 
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
// Change this line to use the type-safe 'basePackageClasses'
@EnableMongoRepositories(basePackageClasses = ProposalMongoRepository.class)
public class ProdashBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProdashBackendApplication.class, args);
    }
}