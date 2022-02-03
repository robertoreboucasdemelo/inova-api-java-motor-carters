/*
 * Copyright (c) 2017, Riachuelo and/or its affiliates. All rights reserved.
 * RCHLO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.com.riachuelo.microservices.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The persistent class for the POC_FILIAIS database table.
 * 
 */
@Entity
@Table(name="POC_FILIAIS")
@NamedQuery(name="Filial.findAll", query="SELECT f FROM Filial f")
@JsonIgnoreProperties(ignoreUnknown=true)
public class Filial implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="COD_LOJA")
	private String codLoja;

	@Column(name="CODIGO_IBGE")
	private BigDecimal codigoIbge;

	@Column(name="CODIGO_UF")
	private BigDecimal codigoUf;

	@Column(name="DESCRICAO_LOJA")
	private String descricaoLoja;

	public Filial() {
	}

	public String getCodLoja() {
		return this.codLoja;
	}

	public void setCodLoja(String codLoja) {
		this.codLoja = codLoja;
	}

	public BigDecimal getCodigoIbge() {
		return this.codigoIbge;
	}

	public void setCodigoIbge(BigDecimal codigoIbge) {
		this.codigoIbge = codigoIbge;
	}

	public BigDecimal getCodigoUf() {
		return this.codigoUf;
	}

	public void setCodigoUf(BigDecimal codigoUf) {
		this.codigoUf = codigoUf;
	}

	public String getDescricaoLoja() {
		return this.descricaoLoja;
	}

	public void setDescricaoLoja(String descricaoLoja) {
		this.descricaoLoja = descricaoLoja;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codLoja == null) ? 0 : codLoja.hashCode());
		result = prime * result + ((codigoIbge == null) ? 0 : codigoIbge.hashCode());
		result = prime * result + ((codigoUf == null) ? 0 : codigoUf.hashCode());
		result = prime * result + ((descricaoLoja == null) ? 0 : descricaoLoja.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Filial other = (Filial) obj;
		if (codLoja == null) {
			if (other.codLoja != null)
				return false;
		} else if (!codLoja.equals(other.codLoja))
			return false;
		if (codigoIbge == null) {
			if (other.codigoIbge != null)
				return false;
		} else if (!codigoIbge.equals(other.codigoIbge))
			return false;
		if (codigoUf == null) {
			if (other.codigoUf != null)
				return false;
		} else if (!codigoUf.equals(other.codigoUf))
			return false;
		if (descricaoLoja == null) {
			if (other.descricaoLoja != null)
				return false;
		} else if (!descricaoLoja.equals(other.descricaoLoja))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Filial [codLoja=" + codLoja + ", codigoIbge=" + codigoIbge + ", codigoUf=" + codigoUf
				+ ", descricaoLoja=" + descricaoLoja + "]";
	}

}