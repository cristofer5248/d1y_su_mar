package com.grupoq.app.controllers;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
//import org.springframework.security.access.annotation.Secured;
//import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.grupoq.app.models.entity.CarritoItems;
//import com.grupoq.app.models.entity.CarritoItems;
import org.springframework.ui.Model;
import com.grupoq.app.models.entity.Facturacion;
import com.grupoq.app.models.entity.Giro;
import com.grupoq.app.models.entity.Inventario;
import com.grupoq.app.models.entity.Movimientos;
import com.grupoq.app.models.entity.Producto;
import com.grupoq.app.models.service.ICarritoItemsService;
import com.grupoq.app.models.service.IFacturaService;
import com.grupoq.app.models.service.IGiroService;
import com.grupoq.app.models.service.IInventarioService;
import com.grupoq.app.models.service.IMovimientosService;
import com.grupoq.app.models.service.IProductoService;
import com.grupoq.app.models.service.MailSenderService;

@RequestMapping("/adminzone")
@RestController
public class pruebaLandController {

	@Autowired
	private IGiroService giroService;

	@Autowired
	private IFacturaService facturaService;

	@Autowired
	private IProductoService productoService;

	@Autowired
	private IInventarioService inventarioservice;

	@Autowired
	private ICarritoItemsService carritoitems;

	@Autowired
	private MailSenderService mailservice;

	@Autowired
	private IMovimientosService movimientosService;

	@RequestMapping(value = "/saveExpress/{nombre}", method = { RequestMethod.GET }, produces = { "application/json" })
	@ResponseBody
	public String saveExpress(@PathVariable(value = "nombre", required = true) String nombre) {
		Giro giro = new Giro();
		giro.setDetalles(nombre);
		giroService.save(giro);
		return "exito";

	}

	@RequestMapping(value = "/facturasAllstatus/{opc}/{valor}", method = RequestMethod.GET)
	public String verSQLFacturaStatuses(@PathVariable(value = "opc") int opc,
			@PathVariable(value = "valor") String valor) {
		String result = "";
		if (opc == 1) {
			try {
				List<Facturacion> veamos = facturaService
						.findByCotizacionByCarritoItemsByIdByStatusWithoutProducto(Long.parseLong(valor));
				result = "AUN NO ESTAN TODOS POSITIVOS, A ESPERAR";
				if (veamos.isEmpty()) {
					result = "Eliminar porque no hay ningun falso de mas";
				}

			} catch (Exception e) {
				mailservice.sendEmailchris(e.toString(), "Error PruebalandController");
				result += "Eliminar porque no hay ningun falso de mas";
			}

		}

		return result;
	}

	@RequestMapping(value = "/stockearbien", method = RequestMethod.GET)
	public String actualizatebro() {
		String result = "";

		List<Producto> todosay = productoService.findAllList();
		for (Producto pro : todosay) {
			System.out.print("El id es el del error: " + pro.getId());
			List<Inventario> stocks = inventarioservice.findByProductoById(pro.getId());
			int totalizador = 0;
			for (Inventario stocks1by1 : stocks) {
				totalizador += stocks1by1.getStock();
			}
			for (CarritoItems carro : carritoitems.findByProductosId(pro.getId())) {
				totalizador += -1 * carro.getCantidad();
			}
			if (totalizador == 0) {

			} else {
				pro.setStock(totalizador);
				productoService.save(pro);
			}
		}
		result = "Operacion exitosa";
		return result;
	}

	@RequestMapping(value = "/lambdainventario/{idproducto}/{cantidad}", method = RequestMethod.GET)
	public String lambdaInventario(@PathVariable(value = "idproducto") Long id,
			@PathVariable(value = "cantidad") int cantidad) {
		Producto producto = productoService.findOne(id);
		String mensaje = "RAW <br>";
		String mensaje2 = "<br> LAMBDA <br>";
		List<Facturacion> fact = facturaService.findByCotizacionByCarritoItemsByIdByStatusWithoutProducto(id);
		mensaje += ((producto.getStock() + cantidad) > 0) ? "Cubierto<br>" : "No dan las cuentas<br>";
		for (Facturacion fa : fact) {
			mensaje += fa.getCotizacion().getCarrito().get(0).getProductos().getNombrep() + "<br>";

		}

		List<Facturacion> factLambda = fact.stream()
				.filter(p -> p.getCotizacion().getCarrito_objeto().getProductos().getStock() > 0)
				.collect(Collectors.toList());
		for (Facturacion fal : factLambda) {
			mensaje2 += fal.getId() + "<br>";
		}
		return mensaje + mensaje2;
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {

		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	// @RequestMapping(value = "/probador/{opc}/{valor}", method =
	// RequestMethod.GET)
	// public String probador(@PathVariable(value = "opc") int opc,
	// @PathVariable(value = "valor") String valor) {
	// String mensaje = "";
	// List<Facturacion> lista =
	// facturaService.findHistorialPrecios(Long.parseLong(valor));
	// for (int i = 0; i < lista.size(); i++) {
	// mensaje += "<H1>" + i + "</H1><br>";
	// mensaje += "ID: " + lista.get(i).getId()+"<br>";
	// for (CarritoItems carrito : lista.get(i).getCotizacion().getCarrito()) {
	// if (carrito.getProductos().getId() == Long.parseLong(valor)) {
	// mensaje += "PRECIO: "+carrito.getPrecio()+"<br>";
	// }
	// }
	// }
	// return mensaje;
	// }

	@RequestMapping(value = "/minimo", method = RequestMethod.GET)
	public String listar() {
		String text = null;
		List<Object[]> producto0_ = productoService.findAllminimo();

		for (int i = 0; i < producto0_.size(); i++) {
			for (Object[] a : producto0_) {
				text = ("Producto "
						+ a[0]);

			}

		}

		// model.addAttribute("titulo", "Listado de productos minimos");
		// model.addAttribute("productos", producto0_);
		return text;
	}

	@RequestMapping(value = "/noMoves", method = RequestMethod.GET)
	public String noMoves() {
		String text = "LOS SIGUIENTES PRODUCTOS NO PRESENTAN NINGUN MOVIMIENTO:";
		List<Producto> producto0_ = productoService.rellenarstock();

		for (Producto p : producto0_) {
			text += ("Producto <br></br>"
					+ p.getNombrep());
		}
		text += "total: " + producto0_.size();

		// model.addAttribute("titulo", "Listado de productos minimos");
		// model.addAttribute("productos", producto0_);
		return text;
	}

	@RequestMapping(value = "/ponerstocks", method = RequestMethod.GET)
	public String rellenarstock(Authentication authentication) {		
		List<Producto> producto0_ = productoService.rellenarstock();
		String text = producto0_.size()==0?"Accion no necesaria, no hubo que realizar": null;
		Movimientos newmove = new Movimientos();
		movimientosService.save(newmove);
		try {

			for (Producto p : producto0_) {
				Inventario inventario = new Inventario();
				inventario.setCodigoProveedor("AUTOMATICO" + producto0_.get(1).getId());
				inventario.setComentario(
						"Este registro fue insertado automaticamente, el producto no presentaba una entrada inicial");
				inventario.setEstado(true);
				inventario.setFecha(new Date());
				inventario.setMovimientos(newmove);
				inventario.setZaNombrede(authentication.getName());
				inventario.setProducto(p);
				inventario.setStock(p.getStock());
				inventarioservice.save(inventario);
				text="Listo, por favor revisar de nuevo";

			}
		} catch (Exception e) {
			text="hubo un error";
		}

		// model.addAttribute("titulo", "Listado de productos minimos");
		// model.addAttribute("productos", producto0_);
		return text;
	}

}