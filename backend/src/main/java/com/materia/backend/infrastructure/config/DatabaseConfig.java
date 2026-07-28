package com.materia.backend.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.materia.backend", repositoryImplementationPostfix = "Impl")
public class DatabaseConfig {

    // ============================================
    // 1. PROPRIÉTÉS DE CONFIGURATION
    // ============================================

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    // JPA / Hibernate
    @Value("${spring.jpa.show-sql:false}")
    private boolean showSql;

    @Value("${spring.jpa.format-sql:false}")
    private boolean formatSql;

    @Value("${spring.jpa.generate-ddl:true}")
    private boolean generateDdl;

    @Value("${spring.jpa.hibernate.ddl-auto:update}")
    private String ddlAuto;

    @Value("${spring.jpa.properties.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}")
    private String dialect;

    @Value("${spring.jpa.properties.hibernate.default_schema:public}")
    private String defaultSchema;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size:30}")
    private int batchSize;

    @Value("${spring.jpa.properties.hibernate.order_inserts:true}")
    private boolean orderInserts;

    @Value("${spring.jpa.properties.hibernate.order_updates:true}")
    private boolean orderUpdates;

    // ============================================
    // 2. DATASOURCE (Tomcat JDBC Pool par défaut)
    // ============================================

    @Bean
    @Primary
    public DataSource dataSource() {
        createDatabaseIfNotExists();
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    /**
     * ✅ Automatic creation of target database if it does not exist on startup
     */
    private void createDatabaseIfNotExists() {
        if (url == null || !url.startsWith("jdbc:postgresql:")) {
            return;
        }

        try {
            int lastSlash = url.lastIndexOf('/');
            int queryParam = url.indexOf('?', lastSlash);
            String dbName = (queryParam > 0) ? url.substring(lastSlash + 1, queryParam) : url.substring(lastSlash + 1);

            if (dbName.isBlank() || dbName.equalsIgnoreCase("postgres")) {
                return;
            }

            String baseUrl = url.substring(0, lastSlash + 1) + "postgres" + ((queryParam > 0) ? url.substring(queryParam) : "");

            try (Connection conn = DriverManager.getConnection(baseUrl, username, password);
                 Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'");
                if (!rs.next()) {
                    stmt.executeUpdate("CREATE DATABASE \"" + dbName + "\"");
                    System.out.println("✅ Automatically created missing database: " + dbName);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not auto-create database: " + e.getMessage());
        }
    }

    // ============================================
    // 3. ENTITY MANAGER FACTORY (JPA)
    // ============================================

    /**
     * ✅ Configuration JPA / Hibernate
     */
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();

        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.materia.backend");
        emf.setPersistenceUnitName("purchasePU");

        // === Hibernate Adapter ===
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(showSql);
        vendorAdapter.setGenerateDdl(generateDdl);
        vendorAdapter.setDatabasePlatform(dialect);
        emf.setJpaVendorAdapter(vendorAdapter);

        // === Propriétés Hibernate ===
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.format_sql", formatSql);
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.default_schema", defaultSchema);
        properties.put("hibernate.jdbc.batch_size", batchSize);
        properties.put("hibernate.order_inserts", orderInserts);
        properties.put("hibernate.order_updates", orderUpdates);

        // Optimisations supplémentaires
        properties.put("hibernate.jdbc.fetch_size", 100);
        properties.put("hibernate.jdbc.lob.non_contextual_creation", true);
        properties.put("hibernate.connection.autocommit", false);
        properties.put("hibernate.connection.release_mode", "after_transaction");

        // Cache de second niveau (désactivé)
        properties.put("hibernate.cache.use_second_level_cache", false);
        properties.put("hibernate.cache.use_query_cache", false);

        // Statistiques (désactivé)
        properties.put("hibernate.generate_statistics", false);

        // Timeouts
        properties.put("jakarta.persistence.query.timeout", 30000);
        properties.put("hibernate.jdbc.timeout", 30);

        emf.setJpaPropertyMap(properties);

        return emf;
    }

    // ============================================
    // 4. TRANSACTION MANAGER
    // ============================================

    /**
     * ✅ Gestionnaire de transactions
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(
            LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        transactionManager.setRollbackOnCommitFailure(true);
        transactionManager.setDefaultTimeout(30); // 30 secondes
        return transactionManager;
    }
}