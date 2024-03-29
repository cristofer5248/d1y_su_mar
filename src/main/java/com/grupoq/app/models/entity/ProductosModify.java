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

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class ProductosModify implements Serializable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	private Producto productomodi;
	
	
	private Double precio;
	private String proveedor;
	@Column(length=100)
	private String detalle;

	@Column(length = 6)
	private int stock;
//	alter table productos_modify add column proveedor varchar(200) after precio;
//	update productos_modify set proveedor="No disponible en su momento" where id>0;
	private Date fecha;
	

	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}





	public Producto getProductomodi() {
		return productomodi;
	}


	public void setProductomodi(Producto productomodi) {
		this.productomodi = productomodi;
	}


	public Double getPrecio() {
		return precio;
	}


	public void setPrecio(Double precio) {
		this.precio = precio;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}


	public Date getFecha() {
//		Date date1 = new Date();
//	    try {
//	    	String timeStamp = new SimpleDateFormat("dd/MM/yyyy").format(fecha);
//			date1 = new SimpleDateFormat("dd/MM/yyyy").parse(timeStamp);
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return fecha;
	}


	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}


	public String getProveedor() {
		return proveedor;
	}


	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}
	public int getStock() {
		return stock;
	}


	public void setStock(int stock) {
		this.stock = stock;
	}
	public void setDetalle(String detalle){
		this.detalle = detalle;
	}
	public String getDetalle(){
		return detalle;
	}

	private static final long serialVersionUID = 1L;

}
