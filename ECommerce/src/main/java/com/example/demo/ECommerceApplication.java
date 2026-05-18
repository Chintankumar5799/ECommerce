package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.mariadb.autoconfigure.MariaDbStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// MariaDbStoreAutoConfiguration is excluded because we manually create the
// VectorStore bean in aiConfig.java, wired explicitly to the MariaDB DataSource.
// Without this exclude, Spring would try to use the @Primary PostgreSQL DataSource
// for the vector store, which fails because Postgres doesn't support MariaDB SQL syntax.
@SpringBootApplication(exclude = {MariaDbStoreAutoConfiguration.class})
@EnableJpaAuditing
@EnableMethodSecurity
// @MapperScan("com.example.demo.category.dao")
public class ECommerceApplication {

	private static final Logger logger = LoggerFactory.getLogger(ECommerceApplication.class);

	public static void main(String[] args) {

		SpringApplication.run(ECommerceApplication.class, args);
		logger.info("start of logger");

	}

}
