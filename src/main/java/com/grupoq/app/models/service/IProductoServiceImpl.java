package com.grupoq.app.models.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupoq.app.models.entity.Producto;
import com.grupoq.app.models.dao.ProductosDao;

@Service
public class IProductoServiceImpl implements IProductoService {

	@Autowired
	private ProductosDao productosDao;

	@Override
	@Transactional(readOnly = true)
	public Page<Producto> findAll(Pageable page) {
		// TODO Auto-generated method stub
		return productosDao.findAll(page);
	}

	@Override
	@Transactional
	public void save(Producto producto) {
		// TODO Auto-generated method stub
		productosDao.save(producto);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		productosDao.deleteById(id);

	}

	@Override
	@Transactional(readOnly = true)
	public Producto findOne(Long id) {
		// TODO Auto-generated method stub
		return productosDao.findById(id).orElse(null);
	}

	@Override
	public List<Producto> findAllList() {
		// TODO Auto-generated method stub
		return (List<Producto>) productosDao.findAll();
	}

	@Override
	public List<Producto> findByNombrep(String term) {
		return productosDao.findByNombrep(term, term);
	}

	@Override
	public List<Producto> findByNombrepAndProveedorId(String term, Long term2) {
		// TODO Auto-generated method stub
		return productosDao.findByNombrepAndProveedorId(term, term2);
	}

	@Override
	public Producto fetchProductoWithInventario(Long id) {
		// TODO Auto-generated method stub
		return productosDao.fetchByIdWithInventario(id);
	}

	@Override
	public Producto fetchByIdWithInventarioByCodigoP(String id) {
		// TODO Auto-generated method stub
		return productosDao.fetchByIdWithInventarioByCodigoP(id);
	}

	@Override
	public Page<Producto> findAllJoin(Pageable page) {
		// TODO Auto-generated method stub
		return productosDao.findAllJoin(page);
	}

	@Override
	public Page<Producto> findAllLike(String termn, Pageable page) {
		// TODO Auto-generated method stub
		return productosDao.findAllLike(termn, termn,termn, page);
	}

	@Override
	public Page<Producto> findAllJoinFalse(Pageable page) {
		// TODO Auto-generated method stub
		return productosDao.findAllJoinFalse(page);
	}

	@Override
	public Page<Producto> findByCodigo(String codigo, Pageable page) {
		// TODO Auto-generated method stub
		return productosDao.findByCodigo(codigo, page);
	}

	@Override
	public Page<Producto> findByProveedor(String codigo, Pageable page) {
		// TODO Auto-generated method stub
		return productosDao.findByProveedor(codigo, page);
	}

	@Override
	public Page<Producto> findByMarca(String codigo, Pageable page) {
		// TODO Auto-generated method stub
		return productosDao.findByMarca(codigo, page);
	}

	@Override
	public Page<Producto> findByCategoria(String codigo, Pageable page) {
		// TODO Auto-generated method stub
		return productosDao.findByCategoria(codigo, page);
	}

	@Override
	public Producto findByCodigo(String codigo) {
		// TODO Auto-generated method stub
		return productosDao.findByCodigo(codigo);
	}

	@Override
	public List<Producto> findByNombrepYMarca(String term, String term2) {
		// TODO Auto-generated method stub
		return productosDao.findByNombrepYMarca(term, term2);
	}

	@Override
	public Page<Producto> findByNombrepYMarcaPage(String term, String term2, Pageable page) {
		// TODO Auto-generated method stub
		return productosDao.findByNombrepYMarcaPage(term, term2, page);
	}

	@Override
	public Page<Producto> findByBodega(String term, Pageable page) {
		// TODO Auto-generated method stub
		return productosDao.findByBodega(term, page);
	}

	@Override
	public Producto findOneByCodigoNot(Long id, Long id2) {
		// TODO Auto-generated method stub
		return productosDao.findOneByCodigoNot(id, id2);
	}

	@Override
	public Page<Producto> findAllFechas(Pageable pageable, Date date1, Date date2) {
		// TODO Auto-generated method stub
		return productosDao.findAllFechas(pageable, date1, date2);
	}

	@Override
	public List<Object[]> findAllminimo() {
		// TODO Auto-generated method stub
		return productosDao.findByMinimo();
		
	}

	@Override
	public List<Producto> findByNombrepNoID(String term, Long idno) {
		// TODO Auto-generated method stub
		return productosDao.findByNombrepNoID(term, term,idno);
	}

	@Override
	public List<Producto> rellenarstock() {
		// TODO Auto-generated method stub
		return productosDao.rellanarstock();
	}

	@Override
	public Page<Producto> findByStatus(String term, Pageable page) {
		// TODO Auto-generated method stub
		return productosDao.findByStatus(term, page);
	}





}
