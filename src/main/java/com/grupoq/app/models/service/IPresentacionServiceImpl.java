package com.grupoq.app.models.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grupoq.app.models.dao.IPresentacionDao;
import com.grupoq.app.models.entity.Presentacion;

@Service
public class IPresentacionServiceImpl implements IPresentacionService {
	
	@Autowired
	private IPresentacionDao presentacionDao;

	@Override
	public List<Presentacion> findAll() {
		// TODO Auto-generated method stub
		return (List<Presentacion>) presentacionDao.findAll();
	}

	@Override
	public List<Presentacion> findByOrderByUnidadDesc() {
		// TODO Auto-generated method stub
		return presentacionDao.findByOrderByUnidadAsc();
	}

	@Override
	public void save(Presentacion presentacion) {
		// TODO Auto-generated method stub
		presentacionDao.save(presentacion);
		
	}

	@Override
	public Presentacion findByDetalle(String detalle) {
		// TODO Auto-generated method stub
		return presentacionDao.findByDetalle(detalle);
	}

	@Override
	public List<Presentacion> findByDetalleList(String nombre) {
		// TODO Auto-generated method stub
		return presentacionDao.findByDetalleList(nombre);
	}
	

}
