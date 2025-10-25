package com.example.assets.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiquibaseDataSourceConfig {

    @Value("${spring.r2dbc.url}")
    private String r2dbcUrl;

    @Value("${spring.r2dbc.username}")
    private String username;

    @Value("${spring.r2dbc.password}")
    private String password;

    @Bean
    @LiquibaseDataSource
    public DataSource liquibaseDataSource() {
        String jdbcUrl = r2dbcUrl.replace("r2dbc:", "jdbc:");
        return DataSourceBuilder.create()
                .url(jdbcUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}
