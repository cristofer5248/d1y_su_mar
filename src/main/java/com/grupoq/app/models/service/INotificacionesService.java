package com.grupoq.app.models.service;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.grupoq.app.models.entity.Notificaciones;

public interface INotificacionesService {

	public List<Notificaciones> findTop15ByOrderByIdDesc();
	public Page<Notificaciones> findAll(Pageable page);
	public Page<Notificaciones> findByNombreContaining(Pageable page, String nombre);
	public void save (Notificaciones notificaciones);
	public List<Notificaciones> therenew(Date time, Date time2); 
	
}
