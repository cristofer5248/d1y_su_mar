package com.grupoq.app.models.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.grupoq.app.models.dao.INotificacionesDao;
import com.grupoq.app.models.entity.Notificaciones;

@Service
public class INotificacionesServiceImpl implements INotificacionesService {

	@Autowired
	private INotificacionesDao notificacionesDao;

	@Override
	public List<Notificaciones> findTop15ByOrderByIdDesc() {
		// TODO Auto-generated method stub
		return notificacionesDao.findTop15ByOrderByIdDesc();
	}

	@Override
	public void save(Notificaciones notificaciones) {
		notificacionesDao.save(notificaciones);
		
	}

	@Override
	public Page<Notificaciones> findAll(Pageable page) {
		// TODO Auto-generated method stub
		return notificacionesDao.findByOrderByIdDesc(page);
	}

	@Override
	public Page<Notificaciones> findByNombreContaining(Pageable page, String nombre) {		
		return notificacionesDao.findByNombreContainingAndOrderByIdDesc(page, nombre);
	}

	@Override
	public List<Notificaciones> therenew(Date time, Date time2) {
		// TODO Auto-generated method stub
		return notificacionesDao.therenew(time, time2);
	}


	
}
