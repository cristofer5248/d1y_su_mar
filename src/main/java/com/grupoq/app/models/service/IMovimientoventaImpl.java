package com.grupoq.app.models.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.grupoq.app.models.dao.IClienteDao;
import com.grupoq.app.models.dao.IClienteDirecciones;
import com.grupoq.app.models.dao.IDireccionesDao;
import com.grupoq.app.models.dao.IMovimientoVentaDao;
import com.grupoq.app.models.entity.Cliente;
import com.grupoq.app.models.entity.ClienteDirecciones;
import com.grupoq.app.models.entity.Direccion;
import com.grupoq.app.models.entity.Movimientoventa;


@Service
public class IMovimientoventaImpl implements IMovimientoventaService {

	@Autowired
private IMovimientoVentaDao movimientoDao;

	@Override
	public void save(Movimientoventa movimientoventa) {
		movimientoDao.save(movimientoventa);
		
	}

	@Override
	public Page<Movimientoventa> findAll(Pageable page) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Movimientoventa> findByIdp(int term) {
		// TODO Auto-generated method stub
		return movimientoDao.findByIdp(term);
	}
		
}
