/*
 * Copyright (c) 2017, Riachuelo and/or its affiliates. All rights reserved.
 * RCHLO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.com.riachuelo.microservices.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.riachuelo.seguranca.validadores.CriptografiaHelper;

/**
 * Pool de conexões criado pelo Spring Data - DataSource.
 * 
 * @author Alexandre.Borges - RCHLO Architecture Team
 * 19/07/2017
 */
@Configuration
public class BasicPoolDataSource {

	private final static Logger LOG = LoggerFactory.getLogger(BasicPoolDataSource.class);

	@Value(value="${spring.datasource.url}")
    private String url = null;

    @Value(value="${spring.datasource.username}")
    private String user = null;

    @Value(value="${spring.datasource.password}")
    private String password = null;

    /**
     * Return the Datasource
     * @return datasource
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.tomcat")
    public DataSource dataSource() {
    	LOG.info("Inicializando datasource...");
    	LOG.info("URL: {}", url);
    	LOG.info("User: {}", user);
    	LOG.info("Password: {}", "*****");
        return DataSourceBuilder.create()
        		.url(url)
        		.username(user)
        		.password(CriptografiaHelper.decriptaAES128(password))
        		.build();
    }
    
}
