// Copyright 2018-2020 Elimu Informatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.elimu.cdshookapi.api.config;

import java.util.Collections;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaSessionFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;


@Configuration
public class CDSHookConfig {
    
    @Autowired private Environment env;
    
    @Value("${spring.datasource.password}")
    private String dataSourcePassword;
    
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getRequiredProperty("spring.datasource.driverClassName"));
        dataSource.setUrl(env.getRequiredProperty("spring.datasource.url"));
        dataSource.setUsername(env.getRequiredProperty("spring.datasource.username"));
        dataSource.setPassword(dataSourcePassword);
        return dataSource;
    } 
    
    @Bean @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

        HibernateJpaVendorAdapter hibernateJpa = new HibernateJpaVendorAdapter();
	hibernateJpa.setDatabasePlatform(env.getRequiredProperty("spring.jpa.properties.hibernate.dialect"));
        hibernateJpa.setShowSql(true);
        
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource());
        emf.setJpaProperties(hibernateProperties());
        emf.setJpaVendorAdapter(hibernateJpa);
        emf.setPackagesToScan("io.elimu.service.models");
        emf.setJpaPropertyMap(Collections.singletonMap("javax.persistence.validation.mode", "none"));
        return emf;
    }
    
    private Properties hibernateProperties() {
        Properties properties = new Properties();
	properties.put("hibernate.dialect", env.getRequiredProperty("spring.jpa.properties.hibernate.dialect"));
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", "true");
//        properties.put("hibernate.hbm2ddl.auto", env.getRequiredProperty("spring.jpa.hibernate.ddl-auto"));
//        properties.put("hibernate.show_sql", env.getRequiredProperty("spring.jpa.hibernate.show_sql"));
      
        return properties;
    }

    
    @Bean
    public JpaTransactionManager transactionManager() {
        JpaTransactionManager txnMgr = new JpaTransactionManager();
        txnMgr.setEntityManagerFactory(entityManagerFactory().getObject());
        return txnMgr;
    }
    
    
   @Bean
   public HibernateJpaSessionFactoryBean sessionFactory(EntityManagerFactory emf) {
       HibernateJpaSessionFactoryBean fact = new HibernateJpaSessionFactoryBean();
       fact.setEntityManagerFactory(emf);
       return fact;
    }
   
   @Bean
   public Session getSession(SessionFactory sessionFactory) {
	   Session session=sessionFactory.openSession();
	   return session;
	   
   }
  
}
