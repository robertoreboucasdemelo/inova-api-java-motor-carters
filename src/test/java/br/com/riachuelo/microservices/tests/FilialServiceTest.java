/*
 * Copyright (c) 2017, Riachuelo and/or its affiliates. All rights reserved.
 * RCHLO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.com.riachuelo.microservices.tests;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.riachuelo.microservices.controller.FilialController;
import br.com.riachuelo.microservices.model.Filial;
import br.com.riachuelo.microservices.model.FilialRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:/config/application-tests.properties")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FilialServiceTest {

	private final static Logger LOG = LoggerFactory.getLogger(FilialServiceTest.class);

	private MockMvc mockMvc;

	@InjectMocks
	FilialController controller;
	
	@Mock
	FilialRepository repository;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }	
	
	@Test
	public void testSaveOk1() throws Exception {
		LOG.info("Testando save ...");
		Filial filial = new Filial();
		filial.setCodLoja("15");
		filial.setCodigoIbge(BigDecimal.ONE);
		filial.setCodigoUf(BigDecimal.ONE);
		filial.setDescricaoLoja("Loja Teste");

		when(repository.save(filial)).thenReturn(filial);

		mockMvc.perform(post("/financeiro/v1/filiais")
				.contentType(MediaType.APPLICATION_JSON).content(new ObjectMapper().writeValueAsString(filial)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.codLoja", is("15"))).andExpect(jsonPath("$.descricaoLoja", is("Loja Teste")))
				.andExpect(jsonPath("$.codigoIbge", is(1))).andExpect(jsonPath("$.codigoUf", is(1)));

		verify(repository, times(1)).save(filial);
		verifyNoMoreInteractions(repository);

	}

	@Test
	public void testSaveNOk2() throws Exception {
		LOG.info("Testando save fail ...");
		Filial filial = new Filial();

		when(repository.save(filial)).thenThrow(JpaSystemException.class);

		mockMvc.perform(post("/financeiro/v1/filiais")	
				.contentType(MediaType.APPLICATION_JSON).content(new ObjectMapper().writeValueAsString(filial)))
				.andExpect(status().is5xxServerError());

		verify(repository, times(1)).save(filial);
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void testUpdateOk3() throws Exception {
		LOG.info("Testando update ...");
		Filial filial = new Filial();
		filial.setCodLoja("15");
		filial.setCodigoIbge(BigDecimal.ONE);
		filial.setCodigoUf(BigDecimal.ONE);
		filial.setDescricaoLoja("Loja Teste Alterado");

		when(repository.save(filial)).thenReturn(filial);

		mockMvc.perform(put("/financeiro/v1/filiais")				
				.contentType(MediaType.APPLICATION_JSON).content(new ObjectMapper().writeValueAsString(filial)))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.descricaoLoja", is("Loja Teste Alterado")));

		verify(repository, times(1)).save(filial);
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void testUpdateNOk4() throws Exception {
		LOG.info("Testando update fail ...");
		Filial filial = new Filial();

		mockMvc.perform(put("/financeiro/v1/filiais")
				.contentType(MediaType.APPLICATION_JSON).content(new ObjectMapper().writeValueAsString(filial)))
				.andExpect(status().is4xxClientError());

		verifyNoMoreInteractions(repository);
	}

	@Test
	public void testDelete5() throws Exception {
		LOG.info("Testando delete ...");

		Filial filial1 = new Filial();
		filial1.setCodLoja("1");

		when(repository.findById("1")).thenReturn(java.util.Optional.of(filial1));

		mockMvc.perform(delete("/financeiro/v1/filiais/1").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());

		verify(repository, times(1)).findById("1");
		verify(repository, times(1)).delete(filial1);
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void testFindAll() throws Exception {
		LOG.info("Testando findAll ...");

		Filial filial1 = new Filial();
		filial1.setCodLoja("1");
		filial1.setCodigoIbge(BigDecimal.ONE);
		filial1.setCodigoUf(BigDecimal.ONE);
		filial1.setDescricaoLoja("Loja Teste");

		Filial filial2 = new Filial();
		filial2.setCodLoja("2");
		filial2.setCodigoIbge(BigDecimal.ONE);
		filial2.setCodigoUf(BigDecimal.ONE);
		filial2.setDescricaoLoja("Loja Teste");

		when(repository.findAll()).thenReturn(Arrays.asList(filial1, filial2));

		mockMvc.perform(get("/financeiro/v1/filiais")).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].codLoja", is("1"))).andExpect(jsonPath("$[0].descricaoLoja", is("Loja Teste")))
				.andExpect(jsonPath("$[0].codigoIbge", is(1))).andExpect(jsonPath("$[1].codLoja", is("2")))
				.andExpect(jsonPath("$[1].descricaoLoja", is("Loja Teste")))
				.andExpect(jsonPath("$[1].codigoUf", is(1)));

		verify(repository, times(1)).findAll();
		verifyNoMoreInteractions(repository);

		LOG.info("FindAll executado com sucesso!");
	}

	@Test
	public void testFindById() throws Exception {
		LOG.info("Testando testFindById ...");

		Filial filial1 = new Filial();
		filial1.setCodLoja("1");
		filial1.setCodigoIbge(BigDecimal.ONE);
		filial1.setCodigoUf(BigDecimal.ONE);
		filial1.setDescricaoLoja("Loja Teste");

		when(repository.findById("1")).thenReturn(Optional.of(filial1));

		mockMvc.perform(get("/financeiro/v1/filiais/1").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(filial1))).andExpect(status().isOk());

		verify(repository, times(1)).findById("1");
		verifyNoMoreInteractions(repository);

		LOG.info("FindById executado com sucesso!");
	}

	@Test
	public void testFindByIdNok() throws Exception {
		LOG.info("Testando testFindById ...");

		mockMvc.perform(get("/financeiro/v1/filiais/2")).andExpect(status().is4xxClientError());

		LOG.info("FindById executado com sucesso!");
	}
}
