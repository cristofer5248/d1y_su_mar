package com.grupoq.app.controllers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;

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

		// try {
		// System.out.print(getClientIPAddress());
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
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

		Movimientos movimiento = new Movimientos(); // marcamos un movimiento nuevo
		if (integrar == 0 && idfacturaRepeted(codigo))
			for (int i = 0; i < itemId.length; i++) {// no se pretende anexar el ingreso a una factura existente ni
														// tampoco esta repetido esa factura
				inventarioService.save(productoToInventario(cantidad[i], itemId[i], date1, comentario, codigo,
						movimiento, authentication.getName()));
				// guardamos inventario pero no afectamos el stock todavia
				operacionStock(itemId[i], true, cantidad[i])? flash.addFlashAttribute("success", "Nuevo inventario realizado con exito"): flash.addFlashAttribute("error", "Error, revise sus datos. El inventario se establecion como oculto");
				
				return "redirect:/inventario/listar";
			}
		else {
			flash.addFlashAttribute("success", "Else");
		}

		return "redirect:/inventario/nuevo";
	}

	public Inventario productoToInventario(Integer c, Long id, Date fecha, String comentario, String codigo,
			Movimientos moviento, String zaNombrede) {
		Inventario inventario = new Inventario();
		inventario.setCodigoProveedor(codigo);
		inventario.setComentario(comentario);
		inventario.setStock(c);
		inventario.setProducto(productoService.findOne(id));
		inventario.setFecha(fecha);
		inventario.setEstado(true);
		inventario.setMovimientos(moviento);
		inventario.setZaNombrede(zaNombrede);
		return inventario;
	}

	public Boolean operacionStock(Long id, Boolean operacion, Integer stock) {
		Producto producto = productoService.findOne(id);
		Integer stockfirst = producto.getStock();
		producto.setStock(operacion ? producto.getStock() - stock : producto.getStock() - stock);
		productoService.save(producto);
		return productoService.findOne(id).getStock() != stockfirst;

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
				System.out.print("Codigo para eliminar de inventario /n " + stockDeInventario);
				// Producto productoTemp = inventarioTemp.getProducto();
				// int stockTemp = productoTemp.getStock() - stockDeInventario;
				// productoTemp.setStock(stockTemp);
				// productoService.save(productoTemp);
				inventarioService.delete(id);
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
}