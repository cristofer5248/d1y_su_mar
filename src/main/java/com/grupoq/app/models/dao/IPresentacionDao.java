package com.grupoq.app.models.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.grupoq.app.models.entity.Presentacion;

public interface IPresentacionDao extends CrudRepository<Presentacion, Long> {
	public List<Presentacion> findByOrderByUnidadAsc();
	public Presentacion findByDetalle(String detalle);
	
	@Query("select p from Presentacion p where p.detalle like %?1%")
	public List<Presentacion> findByDetalleList(String detalle);

}
