package com.grupoq.app.models.dao;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

import com.grupoq.app.models.entity.Producto;
import com.grupoq.app.models.entity.ProductosModify;

public interface IProductosModifyDao extends CrudRepository<ProductosModify, Long>{

    public List<ProductosModify> findAllByProductomodi(Producto id);
	

}
