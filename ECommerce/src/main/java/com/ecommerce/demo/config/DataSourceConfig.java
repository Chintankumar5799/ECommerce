package com.ecommerce.demo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    // ====================================================
    // PRIMARY: PostgreSQL — used by JPA for all @Entity classes
    // (User, Product, Cart, Order, Ticket, etc.)
    // ====================================================

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.postgres")
    public DataSourceProperties postgresDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "postgresDataSource")
    @Primary
    public DataSource postgresDataSource(
            @Qualifier("postgresDataSourceProperties") DataSourceProperties props) {
        return props.initializeDataSourceBuilder().build();
    }

    // ====================================================
    // SECONDARY: MariaDB — used ONLY by the AI VectorStore
    // JPA does NOT touch this database
    // ====================================================

    @Bean
    @ConfigurationProperties("spring.datasource.mariadb")
    public DataSourceProperties mariadbDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "mariadbDataSource")
    public DataSource mariadbDataSource(
            @Qualifier("mariadbDataSourceProperties") DataSourceProperties props) {
        return props.initializeDataSourceBuilder().build();
    }

    
    /**
     * JdbcTemplate wired to MariaDB — injected into MariaDBVectorStore manually.
     * Keeps the VectorStore completely isolated from the PostgreSQL DataSource.
     */
    @Bean(name = "mariadbJdbcTemplate")
    public JdbcTemplate mariadbJdbcTemplate(
            @Qualifier("mariadbDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
