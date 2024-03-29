package com.grupoq.app.models.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

import com.grupoq.app.models.entity.Marca;

public interface IMarcaDao extends PagingAndSortingRepository<Marca, Long> {
	public Page<Marca> findByNombremStartsWith(String marca,Pageable page);
	public Marca findByNombrem(String nombre);
	public List<Marca> findAllByOrderByNombremAsc();

}
