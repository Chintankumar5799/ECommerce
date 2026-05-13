//package com.example.demo.config;
//
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.beans.factory.annotation.Qualifier;
//
//import javax.sql.DataSource;
//
//@Configuration
//public class DataSourceConfig {
//
//    @Primary
//    @Bean(name = "postgresDataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.postgres")
//    public DataSource postgresDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean(name = "mariadbDataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.mariadb")
//    public DataSource mariadbDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean(name = "mariadbJdbcTemplate")
//    public JdbcTemplate mariadbJdbcTemplate(@Qualifier("mariadbDataSource") DataSource dataSource) {
//        return new JdbcTemplate(dataSource);
//    }
//}
