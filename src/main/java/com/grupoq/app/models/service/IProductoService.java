package com.grupoq.app.models.service;



import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;

import com.grupoq.app.models.entity.Producto;

public interface IProductoService {

	public Page<Producto> findAll(Pageable page);
	public Page<Producto> findAllJoin(Pageable page);
	public Page<Producto> findAllFechas(Pageable pageable,Date date1, Date date2);
	public Page<Producto> findAllJoinFalse(Pageable page);
	
	public List<Object[]> findAllminimo();

	public void save (Producto producto);
	public void delete(Long id);
	public Page<Producto> findAllLike(String termn,Pageable page);
	
	public Producto findOne(Long id);
	public Producto findOneByCodigoNot(Long id,Long id2);
	
	public List<Producto> findAllList();
	public List<Producto> findByNombrep(String term);	
	public List<Producto> findByNombrepNoID(String term, Long idno);	
	public List<Producto> findByNombrepYMarca(String term, String term2);
	public Page<Producto> findByNombrepYMarcaPage(String term, String term2, Pageable page);
	public Page<Producto> findByBodega(String term, Pageable page);
	public Page<Producto> findByStatus(String term, Pageable page);
	public List<Producto> findByNombrepAndProveedorId(String term, Long term2);
	
	public Producto fetchProductoWithInventario(Long id);
	public Producto fetchByIdWithInventarioByCodigoP(String id);
	
	
	//buscar para parametros en searchbar
	
	public Page<Producto> findByCodigo(String codigo, Pageable page);
	public Page<Producto> findByProveedor(String codigo, Pageable page);
	public Page<Producto> findByMarca(String codigo, Pageable page);
	public Page<Producto> findByCategoria(String codigo, Pageable page);
	public Producto findByCodigo(String codigo);
	
//queries asi old directos

public List<Producto> rellenarstock();

}
