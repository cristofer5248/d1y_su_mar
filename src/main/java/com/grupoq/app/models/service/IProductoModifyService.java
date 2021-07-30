package com.grupoq.app.models.service;

import java.util.List;

import com.grupoq.app.models.entity.Producto;
import com.grupoq.app.models.entity.ProductosModify;

public interface IProductoModifyService {
	public void save(ProductosModify productosmodify);
	public List<ProductosModify>  findAllByProductomodi(Producto id);
	public void delete(Long id);
	public ProductosModify findById(Long id);

}
