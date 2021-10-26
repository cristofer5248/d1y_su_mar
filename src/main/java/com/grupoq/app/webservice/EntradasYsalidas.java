package com.grupoq.app.webservice;

import java.util.Date;

public class EntradasYsalidas {

	public Long id;
	public String codigo;
	public int movimiento;
	public Date fecha;
	public String color;
	public String url;
	public String concepto;

	public EntradasYsalidas(Long id, String codigo, int movimiento, Date fecha, String color, String url, String concepto) {
		super();
		this.id = id;
		this.codigo = codigo;
		this.movimiento = movimiento;
		this.fecha = fecha;
		this.color = color;
		this.url = url;
		this.concepto = concepto;
	}

	public EntradasYsalidas() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public int getMovimiento() {
		return movimiento;
	}

	public void setMovimiento(int movimiento) {
		this.movimiento = movimiento;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setConcepto(String concepto){
		this.concepto = concepto;
	}
}
