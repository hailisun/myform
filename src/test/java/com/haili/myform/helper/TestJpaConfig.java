package com.haili.myform.helper;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@TestConfiguration
@EnableJpaAuditing
//@EnableJpaRepositories(basePackages = "com.daimler.otr")
@EnableTransactionManagement
public class TestJpaConfig implements TransactionManagementConfigurer {

    @Autowired
    private Environment env;

    @Bean
    public MariaDB4jSpringService mariaDB4jSpringService() {
        MariaDB4jSpringService mariaDB4jSpringService = new MariaDB4jSpringService();
        DBConfigurationBuilder dBConfigurationBuilder = mariaDB4jSpringService.getConfiguration();
        dBConfigurationBuilder.addArg("--character-set-server=utf8");
        dBConfigurationBuilder.addArg("--lower_case_table_names=1");
        dBConfigurationBuilder.addArg("--collation-server=utf8_general_ci");
        dBConfigurationBuilder.addArg("--user=root");
        return mariaDB4jSpringService;
    }

    @Bean
    @DependsOn("mariaDB4jSpringService")
    public DataSource customerDataSource(MariaDB4jSpringService mariaDB4jSpringService) throws ManagedProcessException {
        String databaseName = env.getProperty("mariaDB4j.databaseName");
        mariaDB4jSpringService.getDB().createDB(databaseName);

        DBConfigurationBuilder config = mariaDB4jSpringService().getConfiguration();
        return DataSourceBuilder.create()
            .url(config.getURL(databaseName))
            .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.daimler.otr");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaProperties(additionalProperties());
        return em;
    }

    @Bean
    JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    private Properties additionalProperties() {
        final Properties hibernateProperties = new Properties();

        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        hibernateProperties.setProperty("hibernate.show_sql", env.getProperty("hibernate.show_sql"));
        hibernateProperties.setProperty("hibernate.format_sql", env.getProperty("hibernate.format_sql"));
        hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", env.getProperty("hibernate.cache.use_second_level_cache"));
        hibernateProperties.setProperty("hibernate.cache.use_query_cache", env.getProperty("hibernate.cache.use_query_cache"));

        return hibernateProperties;
    }

    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return null;
    }
}
