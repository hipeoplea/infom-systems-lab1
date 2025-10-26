package ru.hipeoplea.infomsystemslab1.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(basePackages = "ru.hipeoplea.infomsystemslab1.repository")
public class JpaConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        var vendorAdapter = new EclipseLinkJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(false);

        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("ru.hipeoplea.infomsystemslab1.models");

        Map<String, Object> props = new HashMap<>();
        props.put("eclipselink.weaving", "false");
        props.put("eclipselink.logging.level", "INFO");
        props.put("eclipselink.target-database", "PostgreSQL");
        props.put("jakarta.persistence.schema-generation.database.action", "none"); // или create/update по необходимости
        factory.setJpaPropertyMap(props);

        return factory;
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}