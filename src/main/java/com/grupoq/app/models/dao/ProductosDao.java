package com.grupoq.app.models.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.grupoq.app.models.entity.Producto;

public interface ProductosDao extends PagingAndSortingRepository<Producto, Long> {
	//@Query("select p from Producto p join fetch p.proveedor prv join fetch p.marca m join fetch p.presentacion pre where p.nombrep like %?1%")
	@Query(value = "select * from productos p inner join marca m on p.marca_idm=m.idm inner join categorias c on p.categoria_id=c.id inner join proveedor prov on p.proveedor_id=prov.id inner join presentacion pre on p.presentacion_id=pre.id where p.nombrep like %?1% and p.status!=0 order by p.nombrep like ?2% desc", nativeQuery =true)
	public List<Producto> findByNombrep(String term, String term2);

	@Query(value = "select * from productos p inner join marca m on p.marca_idm=m.idm inner join categorias c on p.categoria_id=c.id inner join proveedor prov on p.proveedor_id=prov.id inner join presentacion pre on p.presentacion_id=pre.id where p.nombrep like %?1% and p.status!=0 and p.idp!=?3 order by p.nombrep like ?2% desc", nativeQuery =true)
	public List<Producto> findByNombrepNoID(String term, String term2,Long idno);

	@Query(value = "(select p.idp,p.nombrep,p.minimo,f.create_at, p.stock from facturacion f inner join carrito_items c on f.cotizacion_id=c.id inner join productos p on c.productos_idp=p.idp where p.stock<p.minimo group by p.idp)union (select p2.idp, p2.nombrep,p2.minimo, i.create_at,p2.stock from inventario i inner join productos p2 on i.producto_idp=p2.idp where p2.stock<p2.minimo group by p2.idp)", nativeQuery =true)
	public List<Object[]> findByMinimo();
	
	@Query("select p from Producto p join fetch p.proveedor prv join fetch p.marca m join fetch p.presentacion pre where p.nombrep like %?1% and p.status!=0 and  p.status=true and m.nombrem like %?2%")
	public List<Producto> findByNombrepYMarca(String term, String term2);
	
	@Query(value = "select p from Producto p join fetch p.proveedor prv join fetch p.marca m join fetch p.presentacion pre where p.nombrep like %?1% and m.nombrem like %?2%", countQuery = "select count(p) from Producto p join p.proveedor prv join p.marca m join p.presentacion pre where p.nombrep like %?1% and m.nombrem like %?2%")
	public Page<Producto> findByNombrepYMarcaPage(String term, String term2, Pageable page);
	
	@Query(value = "select p from Producto p join fetch p.proveedor prv join fetch p.marca m join fetch p.presentacion pre where p.bodega like %?1%", countQuery = "select count(p) from Producto p join p.proveedor prv join p.marca m join p.presentacion pre where p.bodega like %?1%")
	public Page<Producto> findByBodega(String term, Pageable page);
	
	
	//este es el eque lista todo en el get listar
	@Query(value = "select p from Producto p join fetch p.marca m join fetch p.categoria c join fetch p.proveedor pro join fetch p.presentacion pre where p.status=true", countQuery = "select count(p) from Producto p join p.marca m join p.categoria c join p.proveedor pro join p.presentacion pre where p.status=true")
	public Page<Producto> findAllJoin(Pageable page);

	//este es para listar por fecha
	@Query(value = "select p from Producto p join fetch p.marca m join fetch p.categoria c join fetch p.proveedor pro join fetch p.presentacion pre join fetch p.productosmodify pm where p.status=true and pm.fecha between ?1 and ?2 group by pm.productomodi", countQuery = "select count(p) from Producto p join p.marca m join p.categoria c join p.proveedor pro join p.productosmodify pm join p.presentacion pre where p.status=true and pm.fecha between ?1 and ?2 group by pm.productomodi")
	public Page<Producto> findAllFechas(Pageable pageable,Date date1, Date date2);

	@Query(value = "select p from Producto p join fetch p.marca m join fetch p.categoria c join fetch p.proveedor pro join fetch p.presentacion pre where p.status=false", countQuery = "select count(p) from Producto p join p.marca m join p.categoria c join p.proveedor pro join p.presentacion pre where p.status=false")
	public Page<Producto> findAllJoinFalse(Pageable page);
	
	
	//@Query(value = "select p from Producto p join fetch p.marca m join fetch p.categoria c join fetch p.proveedor pro join fetch p.presentacion pre where p.nombrep like %?1% order by p.nombrep like ?1% desc", countQuery = "select count(p) from Producto p join p.marca m join p.categoria c join p.proveedor pro join p.presentacion pre where p.nombrep like %?1% order by p.nombrep like ?1% desc")
	@Query(value = "select p from Producto p join fetch p.marca m join fetch p.categoria c join fetch p.proveedor pro join fetch p.presentacion pre where p.nombrep like %?1% and status=true" , countQuery = "select count(p) from Producto p join p.marca m join p.categoria c join p.proveedor pro join p.presentacion pre where p.nombrep like %?1% and status=true")
	public Page<Producto> findAllLike(String termn,String termn2, @Param("title") String title, Pageable page);
	
	@Query("select p from Producto p where p.nombrep like %?1% and p.proveedor.id=?2")
	public List<Producto> findByNombrepAndProveedorId(String term, Long term2);
//	@Query("select p from Producto p join fetch p.proveedor pro join fetch p.marca m join fetch p.categoria c join fetch p.margen mar join fetch p.inventarios i where p.id=?1")
	@Query("select p from Producto p join fetch p.proveedor pro join fetch p.marca m  left join p.productosmodify pm join fetch p.categoria c left join p.inventarios i where p.id=?1")
	public Producto fetchByIdWithInventario(Long id);
//	@Query("select p from Producto p join fetch p.proveedor pro join fetch p.marca m join fetch p.categoria c join fetch p.margen mar join fetch p.inventarios i where i.codigoProveedor=?1")
	@Query("select p from Producto p join fetch p.proveedor pro join fetch p.marca m join fetch p.categoria c join fetch p.inventarios i where i.codigoProveedor=?1")
	public Producto fetchByIdWithInventarioByCodigoP(String id);
	
	
	//filtro para searchbar
	
	@Query(value = "select p from Producto p join fetch p.marca m join fetch p.categoria c join fetch p.proveedor pro join fetch p.presentacion pre where p.codigo like %?1% and status=true", countQuery = "select count(p) from Producto p join p.marca m join p.categoria c join p.proveedor pro join p.presentacion pre where p.codigo like %?1% and status=true")
	public Page<Producto> findByCodigo(String codigo, Pageable page);
	
	@Query(value = "select p from Producto p join fetch p.marca m join fetch p.categoria c join fetch p.proveedor pro join fetch p.presentacion pre where pro.nombre like %?1%", countQuery = "select count(p) from Producto p join p.marca m join p.categoria c join p.proveedor pro join p.presentacion pre where pro.nombre like %?1%")
	public Page<Producto> findByProveedor(String codigo, Pageable page);
	
	@Query(value = "select p from Producto p join fetch p.marca m join fetch p.categoria c join fetch p.proveedor pro join fetch p.presentacion pre where m.nombrem like %?1%", countQuery = "select count(p) from Producto p join p.marca m join p.categoria c join p.proveedor pro join p.presentacion pre where m.nombrem like %?1%")
	public Page<Producto> findByMarca(String codigo, Pageable page);
	
	@Query(value = "select p from Producto p join fetch p.marca m join fetch p.categoria c join fetch p.proveedor pro join fetch p.presentacion pre where c.nombre like %?1%", countQuery = "select count(p) from Producto p join p.marca m join p.categoria c join p.proveedor pro join p.presentacion pre where c.nombre like %?1%")
	public Page<Producto> findByCategoria(String codigo, Pageable page);
	
	public Producto findByCodigo(String codigo);
	
	@Query(value = "select p from Producto p where p.id=?1 and p.codigo!=?2")
	public Producto findOneByCodigoNot(Long id,Long id2);

	
	@Query(nativeQuery =true, value= "select * from productos where idp not in (select producto_idp from inventario) and stock>0")
	public  List<Producto> rellanarstock();

	@Query(value = "select p from Producto p join fetch p.marca m join fetch p.categoria c join fetch p.proveedor pro join fetch p.presentacion pre where p.status=false and p.nombrep like %?1%", countQuery = "select count(p) from Producto p join p.marca m join p.categoria c join p.proveedor pro join p.presentacion pre where p.status=false and p.nombrep like %?1%")
	public Page<Producto> findByStatus(String term, Pageable page);

}
