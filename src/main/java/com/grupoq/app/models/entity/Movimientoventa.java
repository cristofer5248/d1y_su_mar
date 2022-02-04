package com.grupoq.app.models.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "movimientoventa")
public class Movimientoventa implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	private int idfactura;
	private int idp;
	private int stockBefore;
	private int stockAfter;
	private int stockCar;
	private String operacion;

	private static final long serialVersionUID = 1L;

	public Movimientoventa() {

	}
	
	public Long getId() {
		return this.id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public int getIdfactura() {
		return this.idfactura;
	}
	public void setIdfactura(int idfactura) {
		this.idfactura = idfactura;
	}

	public int getIdp(){
		return this.idp;
	}

	public void setIdp(int idp) {
		this.idp = idp;
	}


	public int getStockBefore() {
		return this.stockBefore;
	}
	public void setStockBefore(int stockBefore) {
		this.stockBefore = stockBefore;
	}


	public int getStockAfter() {
		return this.stockAfter;
	}
	public void setStockAfter(int stockAfter) {
		this.stockAfter = stockAfter;
	}


	public int getStockCar() {
		return this.stockCar;
	}
	public void setStockCar(int stockCar) {
		this.stockCar = stockCar;
	}


	public String getOperacion() {
		return this.operacion;
	}
	public void setOperacion(String operacion) {
		this.operacion = operacion;
	}
	

}
