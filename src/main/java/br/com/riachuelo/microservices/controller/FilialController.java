/*
 * Copyright (c) 2017, Riachuelo and/or its affiliates. All rights reserved.
 * RCHLO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.com.riachuelo.microservices.controller;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.riachuelo.microservices.model.Filial;
import br.com.riachuelo.microservices.model.FilialRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Rest Service para o domínio de Filiais.
 * 
 * @author Alexandre.Borges - RCHLO Architecture Team
 * 19/07/2017
 */
@RestController
@RequestMapping("/financeiro/v1")
@Api(value = "Filiais")
public class FilialController {
	
	private final static Logger LOG = LoggerFactory.getLogger(FilialController.class);
	
	@Inject
	private FilialRepository repository;
	
	@ApiOperation(value = "Lista todas as Filiais do grupo")
	@ResponseBody
	@RequestMapping(produces = MediaType.APPLICATION_JSON, method = RequestMethod.GET, path = "/filiais")
	public ResponseEntity<List<Filial>> findAll() {
		LOG.info("Chamando método FilialRepositiory.findAll()");
		final Iterable<Filial> iterable = repository.findAll();
		LOG.info("Método FilialRepositiory.findAll() executado com sucesso!");
		return new ResponseEntity<List<Filial>>((List<Filial>) iterable, HttpStatus.OK);
	}

	@ApiOperation(value = "Lista Filiais por ID")
	@ResponseBody
	@RequestMapping(produces = MediaType.APPLICATION_JSON, method = RequestMethod.GET, path = "/filiais/{codigoFilial}")
	public ResponseEntity<Filial> findById(@PathVariable("codigoFilial") String codigoLoja) {
		LOG.info("Codigo: {}", codigoLoja);
		Optional<Filial> filial = repository.findById(codigoLoja);
		
		if(filial.isPresent()){
			LOG.info("Filial encontrada: {}", filial.get());
			return new ResponseEntity<Filial>(filial.get(), HttpStatus.OK);
		} else {
			LOG.info("Filial não encontrada: {}", codigoLoja);
			return new ResponseEntity<Filial>(HttpStatus.NOT_FOUND);
		}
	}

	@ApiOperation(value = "Cria uma nova Filial")
	@ResponseBody
	@RequestMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, method = RequestMethod.POST, path = "/filiais")
	public ResponseEntity<Filial> newFilial(@RequestBody Filial filial) {
		try {
			LOG.info("Filial: {}", filial);
			filial = repository.save(filial);
		} catch(Exception e){
			LOG.error(e.getMessage(), e);
			return new ResponseEntity<Filial>(HttpStatus.SERVICE_UNAVAILABLE);
		}
		return new ResponseEntity<Filial>(filial, HttpStatus.OK);
	}

	@ApiOperation(value = "Altera uma filial existente")
	@ResponseBody
	@RequestMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, method = RequestMethod.PUT, path = "/filiais")
	public ResponseEntity<Filial> updateFilial(@RequestBody Filial filial) {
		if(filial.getCodLoja() != null){
			filial = repository.save(filial);
			return new ResponseEntity<Filial>(filial, HttpStatus.OK);
		}
		return new ResponseEntity<Filial>(filial, HttpStatus.NOT_FOUND);
	}

	@ApiOperation(value = "Remove uma filial existente")
	@ResponseBody
	@RequestMapping(produces = MediaType.APPLICATION_JSON, method = RequestMethod.DELETE, path = "/filiais/{codigoFilial}")
	public ResponseEntity<Filial> deleteFilial(
			@PathVariable("codigoFilial") String codigoLoja) {
		LOG.info("codigoFilial: {}", codigoLoja);
		Optional<Filial> filial = this.repository.findById(codigoLoja);
		if(filial.isPresent()) {
			repository.delete(filial.get());
			return new ResponseEntity<Filial>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<Filial>(HttpStatus.NOT_FOUND);
	}
}
