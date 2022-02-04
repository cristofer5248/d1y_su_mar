package com.grupoq.app.models.service;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.grupoq.app.models.entity.Movimientoventa;

public interface IMovimientoventaService {
	public void save(Movimientoventa movimientoventa);
	public Page<Movimientoventa> findAll(Pageable page);
    public List<Movimientoventa> findByIdp(int term);
}
