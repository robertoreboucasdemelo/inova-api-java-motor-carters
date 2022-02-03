/*
 * Copyright (c) 2017, Riachuelo and/or its affiliates. All rights reserved.
 * RCHLO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.com.riachuelo.microservices.model;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

/**
 * Repositório criado em runtime pelo Spring Data - DataSource.
 * 
 * @author Alexandre.Borges - RCHLO Architecture Team
 * 19/07/2017
 */
@Component
public interface FilialRepository extends CrudRepository<Filial, String> {

	List<Filial> findByCodigoUf(@Param("codigoUf") BigDecimal codigoUf);

	@Query("select f from Filial f where f.descricaoLoja like :descricaoLoja%")
	List<Filial> findByDescricaoLoja(@Param("descricaoLoja") String descricaoLoja);
}