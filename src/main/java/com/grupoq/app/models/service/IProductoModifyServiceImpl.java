package com.grupoq.app.models.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.grupoq.app.models.dao.IProductosModifyDao;
import com.grupoq.app.models.entity.Producto;
import com.grupoq.app.models.entity.ProductosModify;

@Service
public class IProductoModifyServiceImpl implements IProductoModifyService {

	@Autowired
	IProductosModifyDao productomodifyservice;

	@Override
	public void save(ProductosModify productosmodify) {
		productomodifyservice.save(productosmodify);

	}

	@Override
	public List<ProductosModify> findAllByProductomodi(Producto id) {
		// TODO Auto-generated method stub
		return productomodifyservice.findAllByProductomodi(id);
	}

}
