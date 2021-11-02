package com.grupoq.app.models.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.grupoq.app.models.entity.Notificaciones;

public interface INotificacionesDao extends PagingAndSortingRepository<Notificaciones, Long> {
	
	public List<Notificaciones> findTop15ByOrderByIdDesc();
	public Page<Notificaciones> findByOrderByIdDesc(Pageable page); 
	@Query("select n from Notificaciones n where nombre like %?1% order by n.fecha asc")
	public Page<Notificaciones> findByNombreContainingAndOrderByIdDesc(Pageable page, String nombre); 

	@Query("select n from Notificaciones n where n.fecha between ?1 and ?2 order by n.id asc")
	public List<Notificaciones> therenew(Date time, Date time2); 
}
