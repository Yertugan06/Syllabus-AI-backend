package com.syllabusai.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DBConnectionManager {
    private static DBConnectionManager instance;
    private final DataSource dataSource;

    private DBConnectionManager(){
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/syllabusai");
        config.setUsername("postgres");
        config.setPassword("postgres");
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(5);
        this.dataSource = new HikariDataSource(config);
    }
    public static synchronized DBConnectionManager getInstance(){
        if(instance == null){
            instance = new DBConnectionManager();
        }
        return instance;
    }
    public DataSource getDataSource(){
        return this.dataSource;
    }
}
