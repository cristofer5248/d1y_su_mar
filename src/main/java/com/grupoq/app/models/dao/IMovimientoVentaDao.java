package com.grupoq.app.models.dao;

import java.util.List;


import org.springframework.data.repository.CrudRepository;

import com.grupoq.app.models.entity.Movimientoventa;

public interface IMovimientoVentaDao extends CrudRepository<Movimientoventa, Long> {
	
	//@Query("select g from Movimientoventa g where g.detalles like %?1%")
	public List<Movimientoventa> findByIdp(int term);
	
	//public Movimientoventa findByDetalles(String nombre);
	
}
