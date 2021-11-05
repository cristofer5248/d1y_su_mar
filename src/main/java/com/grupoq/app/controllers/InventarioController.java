package com.grupoq.app.controllers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
//import java.util.Vector;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ch.qos.logback.core.rolling.helper.IntegerTokenConverter;

import com.grupoq.app.models.entity.CarritoItems;
import com.grupoq.app.models.entity.Facturacion;
import com.grupoq.app.models.entity.Inventario;
import com.grupoq.app.models.entity.Movimientos;
import com.grupoq.app.models.entity.Notificaciones;
import com.grupoq.app.models.entity.Producto;
import com.grupoq.app.models.entity.ProductosModify;
import com.grupoq.app.models.service.ICarritoItemsService;
import com.grupoq.app.models.service.IFacturaService;
import com.grupoq.app.models.service.IInventarioService;
import com.grupoq.app.models.service.IMovimientosService;
import com.grupoq.app.models.service.INotificacionesService;
import com.grupoq.app.models.service.IProductoModifyService;
import com.grupoq.app.models.service.IProductoService;
import com.grupoq.app.models.service.MailSenderService;
import com.grupoq.app.util.paginator.PageRender;

@Controller
@SessionAttributes("inventario")
@RequestMapping("/inventario")
@Secured({ "ROLE_ADMIN", "ROLE_INV", "ROLE_JEFEADM" })
public class InventarioController {

	@Autowired
	private IInventarioService inventarioService;
	@Autowired
	private IProductoService productoService;

	@Autowired
	private IMovimientosService movimientosService;

	@Autowired
	private IFacturaService facturaService;

	@Autowired
	private ICarritoItemsService carritoService;

	@Autowired
	private INotificacionesService notificacionesService;

	@Autowired
	private MailSenderService mailservice;

	@Autowired
	private IProductoModifyService productosmodifyService;

	private List<Inventario> invenetarioOld = null;
	// protected final Log logger = LogFactory.getLog(this.getClass());

	@RequestMapping(value = { "/listar", "/listar/{date1}/{date2}", "/listar/{codigo}" }, method = RequestMethod.GET)
	public String listar(@PathVariable(value = "codigo", required = false) String codigo,
			@PathVariable(value = "date1", required = false) String date1,
			@PathVariable(value = "date2", required = false) String date2,
			@RequestParam(value = "all", required = false, defaultValue = "1") int all,
			@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
		String xlsxPath = "?page=" + page;
		xlsxPath = (date1 != null) ? xlsxPath + "&all=" + all : xlsxPath;

		boolean enableallsearch = (date1 != null) ? true : false;

		String pathexcel = (enableallsearch || all > 1)
				? "inventario/listar/" + date1 + "/" + date2 + "?all=1&format=xlsx"
				: xlsxPath;

		pathexcel = (codigo != null) ? "inventario/listar/" + codigo + xlsxPath : pathexcel;
		// pathexcel = (all>1) ? "inventario/listar/" + date1 + "/" + date2 +
		// "?all=1&format=xlsx" : xlsxPath;

		Date date1_ = null;
		Date date2_ = null;
		if (date1 != null) {
			try {
				date1_ = new SimpleDateFormat("yyyy-MM-dd").parse(date1);
				date2_ = new SimpleDateFormat("yyyy-MM-dd").parse(date2);
				model.addAttribute("rangofechas", date1 + " y " + date2);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				mailservice.sendEmailchris(e.toString(), "Error InventarioController");
			}

		}

		Pageable pageRequest = !(all > 0) ? Pageable.unpaged() : PageRequest.of(page, 20);
		Page<Inventario> inventario = null;
		if (codigo != null) {
			inventario = inventarioService.findByCodigoProveedorContaining(codigo, pageRequest);
		} else {
			inventario = (date1 != null && codigo == null)
					? inventarioService.findAllFechas(pageRequest, date1_, date2_)
					: inventarioService.findAll(pageRequest);
		}

		List<Inventario> inventariolamba = inventario.stream().filter(distinctByKey(p -> p.getMovimientos()))
				.collect(Collectors.toList());
		inventariolamba.forEach(il -> System.out.print(il.getComentario()));
		model.addAttribute("titulo", "Listado de inventario");
		PageRender<Inventario> pageRender = new PageRender<>("", inventario);
		model.addAttribute("inventarios", inventario);
		model.addAttribute("page", pageRender);
		model.addAttribute("enableallsearch", enableallsearch);
		model.addAttribute("pathall", pathexcel);
		return "/inventario/listar";
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {

		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	@RequestMapping(value = "/nuevo/{id}", method = RequestMethod.GET)
	public String nuevo(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
		Inventario inventario = new Inventario();
		Producto productoTemp = new Producto();
		if (id > 0) {
			productoTemp = productoService.findOne(id);
			if (productoTemp == null) {
				flash.addFlashAttribute("error", "El ID del producto ha inventariar no existe en la BBDD!");
				return "redirect:/producto/listar";
			}
		} else {
			flash.addFlashAttribute("error", "El ID del cliente no puede ser cero!");
			return "redirect:/producto/listar";
		}
		inventario.setProducto(productoTemp);
		model.put("inventario", inventario);
		model.put("infProducto", productoTemp.getNombrep() + " con el codigo " + productoTemp.getCodigo());
		model.put("titulo", "Ingreso");
		model.put("nullchecker", 1);
		model.put("nullchecker", 0);
		return "/inventario/form";
	}

	@RequestMapping(value = "/nuevo", method = RequestMethod.GET)
	public String nuevo2(Map<String, Object> model, RedirectAttributes flash) {
		Inventario inventario = new Inventario();
		model.put("inventario", inventario);
		model.put("titulo", "Inventariar");
		return "/inventario/form2";
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String guardar(@RequestParam(name = "fecha", required = true) String fecha,
			@RequestParam(name = "comentario") String comentario,
			@RequestParam(name = "codigo", required = true) String codigo,
			@RequestParam(name = "item_id[]", required = false) Long[] itemId,
			@RequestParam(name = "integrar", required = true) int integrar,
			@RequestParam(name = "cantidad[]", required = false) Integer[] cantidad, Model model,
			RedirectAttributes flash, SessionStatus status, Authentication authentication) throws ParseException {
		System.out.print("\nfecha" + fecha);
		String pattern = "yyyy-MM-dd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		Date date1 = simpleDateFormat.parse(fecha);
		System.out.print("\nfecha" + date1);
		System.out.print("\n" + integrar);
		Boolean agregarStock = true;

		// marcamos un movimiento nuevo
		// (donde idfacturaRepeted devuelve true si esta vacio)
		if (integrar == 0 && idfacturaRepeted(codigo)) {
			Movimientos movimiento = new Movimientos();
			movimientosService.save(movimiento);
			for (int i = 0; i < itemId.length; i++) {// no se pretende anexar el ingreso a una factura existente ni
				// tampoco esta repetido esa factura
				// guardamos inventario pero no afectamos el stock todavia
				printinlog("Operacion de registro en inventario iniciada...\n");
				inventarioService.save(productoToInventario(cantidad[i], itemId[i], date1, comentario, codigo,
						movimiento, authentication.getName()));
				printinlog("Completada la accion sin errores\n");
				printinlog("Operacion de inventario que altera el stock iniciada...\n");
				agregarStock = operacionStock(itemId[i], cantidad[i] > 0 ? true : false, cantidad[i]);
				printinlog("Operacion exitosa...\n");

			}
			flash.addFlashAttribute(agregarStock ? "success" : "error",
					agregarStock ? "Nuevo inventario realizado con exito"
							: "revise sus datos. El inventario se establecion como oculto");
			return "redirect:/inventario/listar";
		} else if (integrar == 1 && !idfacturaRepeted(codigo)) {
			printinlog("Inciando la busqueda de la factura en inventario...\n");
			invenetarioOld = inventarioService.findByIdCodigoProveedor(codigo);
			Movimientos movimientoviejo = invenetarioOld.get(0).getMovimientos();
			printinlog("La busqueda arroja una factura con " + invenetarioOld.size()
					+ " productos, Internamente, el movimiento es el numero " + movimientoviejo.getId() + "'\n");

			for (int i = 0; i < itemId.length; i++) {// se pretende anexar el ingreso a
				// una factura existente guardamos inventario pero no afectamos el stock todavia
				inventarioService.save(productoToInventario(cantidad[i], itemId[i], date1, comentario, codigo,
						movimientoviejo, authentication.getName())); // Operacion stock
				agregarStock = operacionStock(itemId[i], cantidad[i] > 0 ? true : false, cantidad[i]);
			}
			flash.addFlashAttribute("success", "Integracion con exito");
			return "redirect:/inventario/listar";
		} else {
			flash.addFlashAttribute("success", "Revise sus datos porfavor");
		}

		return "redirect:/inventario/nuevo";
	}

	public Inventario addWhenItprodIsAlreadyThere(Long idps) {
		Inventario invold = null;
		if (invenetarioOld != null) {
			for (Inventario inv : invenetarioOld) {
				printinlog("Comparando id de movimiento: " + inv.getProducto().getId() + " y " + idps + "\n");
				invold = inv.getProducto().getId().equals(idps) ? inv : invold;
				printinlog(inv.getProducto().getId().equals(idps)
						? "¡Se encontro coincidencia! -> Producto " + inv.getProducto().getNombrep() + "\n"
						: "");
			}
		} else {
			printinlog("No hay comparacion...");
		}
		return invold;
	}

	public Inventario productoToInventario(Integer c, Long id, Date fecha, String comentario, String codigo,
			Movimientos moviento, String zaNombrede) {
		Inventario inventario = addWhenItprodIsAlreadyThere(id);
		if (!Objects.isNull(inventario)) {
			int stockold_temp = inventario.getStock();
			inventario.setStock(stockold_temp + c);
			inventario.setFecha(fecha);
			int toti = stockold_temp + c;
			printinlog("Producto:" + inventario.getProducto().getNombrep() + " id:" + inventario.getProducto().getId()
					+ " Stock actual:" + inventario.getProducto().getStock() + "\n");
			printinlog("Actualizando un registro de inventario...Cantidad anterior: " + stockold_temp
					+ " Cantidad ahora:" + toti+ "\n");
			nuevaNotificacion(
					"fas fa-parachute-box", "Nuevo ingreso de " + inventario.getProducto().getNombrep()
							+ " stock actual: " + toti,
					"/inventario/ver/" + moviento.getId(), "blue");
		} else {
			inventario = new Inventario();
			inventario.setStock(c);
			inventario.setFecha(fecha);
			inventario.setCodigoProveedor(codigo);
			inventario.setComentario(comentario);
			// imprimimos info del producto antes de guardar
			Producto producto_temp = productoService.findOne(id);
			inventario.setProducto(producto_temp);
			inventario.setEstado(true);
			inventario.setMovimientos(moviento);
			inventario.setZaNombrede(zaNombrede);
			int toti = producto_temp.getStock() + c;
			printinlog("Producto:" + producto_temp.getNombrep() + " id:" + producto_temp.getId() + " Stock actual:"
					+ producto_temp.getStock() + "\n");
			nuevaNotificacion("fas fa-parachute-box",
					"Nuevo ingreso de " + producto_temp.getNombrep() + " stock actual: " + toti,
					"/inventario/ver/" + moviento.getId(), "blue");
		}
		return inventario;
	}

	public Boolean operacionStock(Long id, Boolean operacion, Integer stock) {
		Producto producto = productoService.findOne(id);
		Integer stockfirst = producto.getStock();
		Integer stockoperacion = operacion ? stockfirst + stock : stockfirst - stock;
		printinlog("Producto: " + producto.getNombrep() + " id:" + producto.getId() + " stock antes:" + stockfirst
				+ " stock ahora:" + stockoperacion + "\n");
		producto.setStock(stockoperacion);
		productoService.save(producto);
		addProductomodify(producto, new Date(), producto.getPrecio(), producto.getProveedor().getNombre(),
				stockoperacion);
		return productoService.findOne(id).getStock() != stockfirst;

	}

	public void addProductomodify(Producto producto, Date fecha, Double precio, String proveedor, int stock) {
		ProductosModify pm = new ProductosModify();
		printinlog("Iniciando el ingreso al historial de productos...| PRODUCTO: " + producto.getNombrep()
				+ "|Ultimo stock: " + stock + "\n");
		pm.setFecha(fecha);
		pm.setPrecio(precio);
		pm.setProductomodi(producto);
		pm.setProveedor(proveedor);
		pm.setStock(stock);
		printinlog("Ingreso completo al historial de productos...\n");
		productosmodifyService.save(pm);
	}

	public Boolean idfacturaRepeted(String id) {
		return Objects.isNull(inventarioService.findByCodigoProveedor(id));
	}

	@RequestMapping(value = "/eliminar/{id}")
	public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash) {

		if (id > 0) {
			Inventario inventarioTemp = inventarioService.findById(id);
			if (inventarioTemp != null) {
				int stockDeInventario = inventarioTemp.getStock();
				System.out.print("Codigo para eliminar de inventario \n" + stockDeInventario);
				// Producto productoTemp = inventarioTemp.getProducto();
				// int stockTemp = productoTemp.getStock() - stockDeInventario;
				// productoTemp.setStock(stockTemp);
				// productoService.save(productoTemp);
				inventarioService.delete(id);
				Integer stockold = inventarioTemp.getStock();
				operacionStock(inventarioTemp.getProducto().getId(), false, stockold);
				addProductomodify(inventarioTemp.getProducto(), new Date(), inventarioTemp.getProducto().getPrecio(),
						inventarioTemp.getProducto().getProveedor().getNombre(),
						stockold + inventarioTemp.getProducto().getStock());
				flash.addFlashAttribute("success", "Inventariado eliminado con éxito!");
			}
		}
		return "redirect:/inventario/listar";
	}

	@GetMapping(value = "/ver/{id}")
	public String ver(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
		// para una solo entity
		// Inventario inventario;
		Movimientos movimientos;
		try {

			movimientos = movimientosService.findById(id);
		} catch (Exception e) {
			mailservice.sendEmailchris(e.toString(), "Error InventarioController");
			flash.addFlashAttribute("error", "El ingreso con ese codigo no existe en la base de datos");
			return "redirect:/inventario/listar";
		}

		// Inventario inventario = inventarioService.findByIdCodigoProveedorOb(id);

		// List<Inventario> inventario = inventarioService.findByIdCodigoProveedor(id);

		model.put("inventarios", movimientos);
		model.put("proveedor", movimientos.getInventario().get(0).getCodigoProveedor());
		model.put("proveedor1", movimientos.getInventario().get(0).getProducto().getProveedor().getNombre());
		model.put("comentario", movimientos.getInventario().get(0).getComentario());
		model.put("fecha", movimientos.getInventario().get(0).getFecha());
		model.put("codigopro", id);
		model.put("titulo", "Detalle del ingreso : " + id);

		// model.put("inventarios", inventario);
		// model.put("proveedor",
		// inventario.get(0).getProducto().getProveedor().getNombre());
		// model.put("fecha", inventario.get(0).getFecha().toString());
		// model.put("codigopro", id);
		// model.put("titulo", "Detalle del ingreso : " + id);
		return "/inventario/ver";
	}

	public void nuevaNotificacion(String icono, String nombre, String url, String color) {
		Notificaciones noti = new Notificaciones();
		noti.setFecha(new Date());
		noti.setIcono(icono);
		noti.setNombre(nombre);
		noti.setUrl(url);
		noti.setColor(color);
		notificacionesService.save(noti);
	}

	public String getClientIPAddress() throws IOException {
		InetAddress localHost = InetAddress.getLocalHost();
		NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
		byte[] hardwareAddress = ni.getHardwareAddress();
		String[] hexadecimal = new String[hardwareAddress.length];
		for (int i = 0; i < hardwareAddress.length; i++) {
			hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
		}
		String macAddress = String.join("-", hexadecimal);
		return macAddress;
	}

	public void printinlog(String texto) {
		System.out.print(texto);
		// logger.debug(texto);
	}
}