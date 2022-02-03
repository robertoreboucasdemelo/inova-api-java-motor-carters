/*
 /*
 * Copyright (c) 2017, Riachuelo and/or its affiliates. All rights reserved.
 * RCHLO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.com.riachuelo.microservices.filters;

import static net.logstash.logback.marker.Markers.appendEntries;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Provider
public class ResourceLoggingFilter  implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceLoggingFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    @Autowired
    HttpServletRequest httpServletRequest;

    static final String DATA_INICIO = "dataInicio";
    static final String TEMPO_EXECUCAO_MILLIS = "tempoExecucao";
    static final String PATH = "caminho";
    static final String METHOD ="metodo";
    static final String EVENT = "evento";
    static final String CLASSNAME = "classe";
    static final String LOGFILTER = "Mensagem de Filtro";
    static final String CODIGO_SISTEMA = "codigoSistema";
    
    public ResourceLoggingFilter() {
    	super();
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
            LOG.info(appendEntries(buildBody(containerRequestContext)), LOGFILTER);

    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        if (containerRequestContext != null) {
            containerRequestContext.getHeaders().add(DATA_INICIO, String.valueOf(System.currentTimeMillis()));
        }
    }

    private Map<String, Object> buildBody(ContainerRequestContext containerRequestContext){
        if (containerRequestContext == null){
            return null;
        }
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(PATH,containerRequestContext.getUriInfo().getRequestUri());
        map.put(METHOD,containerRequestContext.getMethod());
        map.put(EVENT,resourceInfo.getResourceMethod().getName());
        map.put(CLASSNAME,resourceInfo.getResourceClass().getCanonicalName());
        map.put(TEMPO_EXECUCAO_MILLIS, Long.valueOf(System.currentTimeMillis() - Long.parseLong(containerRequestContext.getHeaderString(DATA_INICIO))));


        if (getUserAgent(httpServletRequest) != null) {
            map.put(CODIGO_SISTEMA, getUserAgent(httpServletRequest));
        }
        return map;
    }

    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader(CODIGO_SISTEMA);
    }
}
