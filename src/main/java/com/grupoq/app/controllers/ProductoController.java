package com.grupoq.app.controllers;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.grupoq.app.webservice.EntradasYsalidas;
import com.grupoq.app.webservice.HistorialDePrecios;
import com.grupoq.app.models.entity.CarritoItems;
import com.grupoq.app.models.entity.Facturacion;
import com.grupoq.app.models.entity.Inventario;
import com.grupoq.app.models.entity.Movimientos;
import com.grupoq.app.models.entity.NotadeCredito;
import com.grupoq.app.models.entity.Notificaciones;
import com.grupoq.app.models.entity.Presentacion;
import com.grupoq.app.models.entity.Producto;
import com.grupoq.app.models.entity.ProductosModify;
import com.grupoq.app.models.entity.Proveedor;
import com.grupoq.app.models.entity.Usuario;
import com.grupoq.app.models.service.IFacturaService;
import com.grupoq.app.models.service.IInventarioService;
import com.grupoq.app.models.service.IMovimientosService;
import com.grupoq.app.models.service.INotadeCreditoService;
import com.grupoq.app.models.service.INotificacionesService;
import com.grupoq.app.models.service.IPresentacionService;
import com.grupoq.app.models.service.IProductoModifyService;
import com.grupoq.app.models.service.IProductoService;
import com.grupoq.app.models.service.IUsuarioService;
import com.grupoq.app.models.service.MailSenderService;
import com.grupoq.app.util.paginator.PageRender;
import org.slf4j.LoggerFactory;
import org.apache.catalina.connector.Response;
import org.apache.poi.util.RLEDecompressingInputStream;
import org.slf4j.Logger;

@Controller
@RequestMapping("/producto")
@SessionAttributes("producto")

public class ProductoController {
	// Logger logger = LoggerFactory.getLogger(ProductoController.class);

	@Autowired
	private IProductoService productoService;

	@Autowired
	private IUsuarioService usersService;

	@Autowired
	INotificacionesService notificacionesService;

	@Autowired
	IPresentacionService presentacionservice;

	@Autowired
	IFacturaService facturaService;

	@Autowired
	IProductoModifyService productomodifyService;

	@Autowired
	INotadeCreditoService notadecreditoService;

	@Autowired
	private MailSenderService mailservice;

	@Autowired
	private IInventarioService inventarioService;

	@Autowired
	private IMovimientosService movimientosService;

	@Secured({ "ROLE_ADMIN", "ROLE_INV", "ROLE_JEFEADM", "ROLE_SELLER" })
	@RequestMapping(value = { "/listar", "/listar/{op}/{nombrep}", "/listar/{op}",
			"/listar/{date1}/{date2}/fechas" }, method = RequestMethod.GET)
	public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model,
			@PathVariable(value = "nombrep", required = false) String nombrep,
			@PathVariable(value = "date1", required = false) String date1,
			@PathVariable(value = "date2", required = false) String date2,
			@PathVariable(value = "op", required = false) String op,
			@RequestParam(name = "op2", required = false, defaultValue = "0") int op2) {
		Pageable pageRequest;
		boolean enablebtnall = false;
		boolean enableallsearch = false;

		Date date1_ = null;
		Date date2_ = null;
		if (date1 != null) {
			try {
				date1_ = new SimpleDateFormat("yyyy-MM-dd").parse(date1);
				date2_ = new SimpleDateFormat("yyyy-MM-dd").parse(date2);
				model.addAttribute("rangofechas", date1 + " y " + date2);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				mailservice.sendEmailchris(e.toString(), "Error ProductoController linea 106");
			}

		}

		// mostrar todos o no 0= a todas y >0 es por paginado
		int npage = 20;
		if (op != null) {
			pageRequest = (op.equals("all")) ? Pageable.unpaged() : PageRequest.of(page, npage);
			enablebtnall = (op.equals("all") ? true : false);
			model.addAttribute("ocultos", op.equals("ocultos") ? true : false);
		} else {
			pageRequest = PageRequest.of(page, npage);
			model.addAttribute("ocultos", false);
		}

		pageRequest = (op2 != 0) ? Pageable.unpaged() : PageRequest.of(page, npage);
		enableallsearch = (op != null) ? true : false;

		Page<Producto> productos = null;
		// String xlsxPath = (page > 0) ? "?page=" + page : "";
		String xlsxPath = "?page=" + page;
		xlsxPath = (op2 != 0) ? xlsxPath + "&op2=" + op2 : xlsxPath;

		String pathall = (op != null) ? xlsxPath + "&op2=1" : "all";

		nombrep = (nombrep == null) ? nombrep : nombrep.replace("zzz", "/");
		String urlpage = "listar";

		if (nombrep != null) {
			xlsxPath = "/" + op + "/" + nombrep;
			urlpage = nombrep;
			if (op.equals("nombre")) {
				pathall = "producto/listar/nombre/" + nombrep + "/" + pathall;
				productos = productoService.findAllLike(nombrep, pageRequest);
				if (productos.isEmpty()) {
					int lastSpaceIndex = nombrep.lastIndexOf(" ");
					String term2 = nombrep.substring(lastSpaceIndex + 1, nombrep.length());
					try {
						nombrep = nombrep.substring(0, lastSpaceIndex);
					} catch (Exception e) {
						// mailservice.sendEmailchris(e.toString(), "Error ProductoController linea
						// 145");
						productos = null;
					}

					productos = productoService.findByNombrepYMarcaPage(nombrep, term2, pageRequest);
				}

			}
			if (op.equals("codigo")) {
				pathall = "producto/listar/codigo/" + nombrep + pathall;
				productos = productoService.findByCodigo(nombrep, pageRequest);
			}
			if (op.equals("proveedor")) {
				pathall = "producto/listar/proveedor/" + nombrep + pathall;
				productos = productoService.findByProveedor(nombrep, pageRequest);
			}
			if (op.equals("marca")) {
				pathall = "producto/listar/marca/" + nombrep + pathall;
				productos = productoService.findByMarca(nombrep, pageRequest);
			}
			if (op.equals("categoria")) {
				pathall = "producto/listar/categoria/" + nombrep + pathall;
				productos = productoService.findByCategoria(nombrep, pageRequest);
			}
			if (op.equals("bodega")) {
				pathall = "producto/listar/bodega/" + nombrep + pathall;
				productos = productoService.findByBodega(nombrep, pageRequest);
			}
			if (op.equals("ocultos")) {
				pathall = "producto/listar/ocultos/" + nombrep + pathall;
				productos = productoService.findByStatus(nombrep, pageRequest);

			}
			if (productos == null) {
				model.addAttribute("error", "Error, Query mal formado");
			}
			xlsxPath += (page > 0) ? "?page=" + page : "";
		} else {
			xlsxPath = (op == null) ? xlsxPath : xlsxPath + "/all";
			if (date1 != null && nombrep == null) {
				pageRequest = Pageable.unpaged();
				enableallsearch = true;
				pathall = "fechas";
			}

			productos = (date1 != null && nombrep == null) ? productoService.findAllFechas(pageRequest, date1_, date2_)
					: productoService.findAllJoin(pageRequest);

		}

		// que model pondremos si es lista o page

		model.addAttribute("nopage", false);
		PageRender<Producto> pageRender = new PageRender<>(urlpage, productos);
		model.addAttribute("page", pageRender);
		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");
		model.addAttribute("xlsxpath", xlsxPath);
		model.addAttribute("pathall", pathall);
		model.addAttribute("enableallsearch", enableallsearch);
		model.addAttribute("enablebtnall", enablebtnall);

		// We are adding the new model to get data about minimum products
		List<Object[]> productsminimum = productoService.findAllminimo();
		model.addAttribute("minimum", productsminimum);

		return "/productos/listar";
	}

	@Secured({ "ROLE_ADMIN", "ROLE_JEFEADM", "ROLE_SELLER", "ROLE_INV" })
	@RequestMapping(value = "/listartodos", method = RequestMethod.GET)
	public void listarTodos(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
		List<Producto> productos = productoService.findAllList();
		// model.addAttribute("titulo", "Listado de marcas");
		model.addAttribute("productos", productos);
		// return "/giros/listar";
	}

	@Secured({ "ROLE_ADMIN", "ROLE_INV", "ROLE_SELLER" })
	@RequestMapping(value = { "/listarS" }, method = RequestMethod.GET)
	public String listarS(@RequestParam(name = "page", defaultValue = "0") int page, Model model,
			@PathVariable(value = "nombrep", required = false) String nombrep) {
		Pageable pageRequest = PageRequest.of(page, 20);
		Page<Producto> productos;
		productos = productoService.findAllJoinFalse(pageRequest);

		PageRender<Producto> pageRender = new PageRender<>("listarS", productos);
		model.addAttribute("titulo", "Listado productos solicitados o que han sido ocultos");
		model.addAttribute("enablebtnall", true);
		model.addAttribute("productos", productos);
		model.addAttribute("page", pageRender);
		model.addAttribute("ocultos", true);
		return "/productos/listar";
	}

	@Secured({ "ROLE_ADMIN", "ROLE_INV" })
	@RequestMapping(value = "/status/{id}")
	public String cambiarStatus(@PathVariable(value = "id") Long id, Map<String, Object> model,
			RedirectAttributes flash) {
		Producto producto = productoService.findOne(id);

		String mensajeFlash = (producto != null) ? "Solicitud aceptada con éxito!"
				: "Error en la solicitud, ese ID de solicitud podria no existir!!";

		producto.setStatus(true);
		productoService.save(producto);
		flash.addFlashAttribute("success", mensajeFlash);
		return "redirect:/producto/listar";
	}

	@Secured({ "ROLE_ADMIN", "ROLE_INV" })
	@RequestMapping(value = "/asegurar/{id}")
	public String asegurarProducto(@PathVariable(value = "id") Long id, Map<String, Object> model,
			RedirectAttributes flash) {
		Producto producto = productoService.findOne(id);

		String mensajeFlash = (producto != null) ? "Cambio de estado éxitoso!"
				: "Error en la solicitud, ese ID de solicitud podria no existir!!";
		boolean estadoB;
		estadoB = producto.isAsegurar() ? false : true;
		producto.setAsegurar(estadoB);
		productoService.save(producto);
		flash.addFlashAttribute("success", mensajeFlash);
		return "redirect:/producto/listar";
	}

	@Secured({ "ROLE_ADMIN", "ROLE_INV" })
	@RequestMapping(value = "/nuevo", method = RequestMethod.GET)
	public String nuevo(Map<String, Object> model) {
		Producto productos = new Producto();
		productos.setStatus(true);
		model.put("producto", productos);
		model.put("titulo", "Crear nuevo producto");
		model.put("nullchecker", 1);
		return "/productos/productoform";
	}

	@Secured({ "ROLE_ADMIN", "ROLE_INV" })
	@RequestMapping(value = "/transformacion", method = RequestMethod.GET)
	public String transformacion(Map<String, Object> model) {
		model.put("titulo", "Tranformacion de productos");
		model.put("nullchecker", 1);
		return "/productos/transformacion";
	}

	@Secured({ "ROLE_ADMIN", "ROLE_SELLER" })
	@RequestMapping(value = "/nuevoS", method = RequestMethod.GET)
	public String nuevoS(Map<String, Object> model) {
		Producto productos = new Producto();
		productos.setStatus(false);
		model.put("producto", productos);
		model.put("titulo", "Crear nuevo producto");
		model.put("nullchecker", 1);
		return "/productos/productoform";
	}

	// @Secured({"ROLE_ADMIN","ROLE_INV"})
	// @RequestMapping(value = "/nuevo10", method = RequestMethod.GET)
	// public String nuevosdjiosjfdoi(Map<String, Object> model) {
	// Producto productos = new Producto();
	// model.put("producto", productos);
	// model.put("titulo", "Crear nuevo producto");
	// model.put("nullchecker", 1);
	// return "/templates/productos/productoform";
	// }
	@Secured({ "ROLE_ADMIN", "ROLE_INV", "ROLE_SELLER" })
	@RequestMapping(value = "/savestock", method = RequestMethod.POST)
	public String guardarStock(@Valid Producto producto, BindingResult result, Model model, RedirectAttributes flash,
			SessionStatus status) {
		Producto pro = producto;
		ProductosModify pm = new ProductosModify();
		pm.setDetalle("Stock modificado directamente");
		pm.setFecha(new Date());
		pm.setPrecio(pro.getPrecio());
		pm.setProductomodi(pro);
		pm.setProveedor(pro.getProveedor().getNombre());
		pm.setStock(pro.getStock());
		productomodifyService.save(pm);
		productoService.save(pro);
		// productomodifyService.save(nuevamodificacion(pro, pro));
		return "redirect:/producto/ver/" + pro.getId();

	}

	@Secured({ "ROLE_ADMIN", "ROLE_INV", "ROLE_SELLER" })
	@RequestMapping(value = "/productosave", method = RequestMethod.POST)
	public String guardar(@Valid Producto producto, BindingResult result, Model model, RedirectAttributes flash,
			SessionStatus status) {
		printLogger("el codigo del id :" + producto.getId() + " d");
		Producto pro = (producto.getId() != null) ? productoService.findOne(producto.getId()) : null;
		Double precioold = (producto.getId() != null) ? pro.getPrecio() : null;
		Proveedor preveedorold = (producto.getProveedor() != null && producto.getId() != null) ? pro.getProveedor()
				: null;

		if (result.hasErrors()) {
			model.addAttribute("titulo", "Formulario de Productos");
			return "/productos/productoform";
		}

		if (pro == null) {
			pro = productoService.findByCodigo(producto.getCodigo());

			if (pro != null) {
				model.addAttribute("error", "Ese codigo de producto ya esta registrado prueba otro.");
				model.addAttribute("titulo", "Enviar de nuevo");
				return "/productos/productoform";
			}

		} else {
			if (pro.getCodigo().equals(producto.getCodigo()) && !(pro.getId().equals(producto.getId()))) {
				model.addAttribute("error", "Ese codigo de producto ya esta registrado prueba otro.");
				model.addAttribute("titulo", "Enviar de nuevo");
				return "/productos/productoform";
			}
		}

		String mensajeFlash = (producto.getId() != null) ? "Producto editado con éxito!"
				: "Producto creado o solicitud enviada con éxito!";
		if (producto.getId() == null) {
			producto.setStock(0);
		}

		if (producto.getId() != null) {
			try {
				productoService.save(producto);
			} catch (Exception e) {
				// mailservice.sendEmailchris(e.toString(), "Error ProductoController linea
				// 336");
				// TODO: handle exception
			}

			if (producto.getPrecio() != precioold || producto.getProveedor() != preveedorold) {
				productomodifyService.save(nuevamodificacion(producto, pro, " no hay"));
				String detalleprod = pro.getId() + ">" + pro.getCodigo() + ">" + pro.getBodega() + ">" + pro.getFecha()
						+ ">" + pro.getMargen() + ">" + pro.getMinimo() + ">" + pro.getPrecio() + ">" + pro.getStock()
						+ ">" + pro.getCategoria().getNombre() + ">" + pro.getMarca().getNombrem() + ">"
						+ pro.getPresentacion().getDetalle() + ">" + pro.getProveedor().getNombre();
				detalleprod = (detalleprod.length() > 100) ? detalleprod.substring(0, 50) : detalleprod;
				nuevaNotificacion("fas fa-box-open",
						"Producto '" + producto.getNombrep() + "' agregado o modificado. Datos: ",
						"/producto/ver/" + producto.getId(), "blue");
				flash.addFlashAttribute("success", mensajeFlash);
			}

		} else {
			try {
				if (producto.getId() == null) {
					producto.setAsegurar(false);
				}
				productoService.save(producto);
			} catch (Exception e) {
				// mailservice.sendEmailchris(e.toString(), "Error ProductoController linea
				// 354");
			}
			productomodifyService.save(nuevamodificacion(producto, producto, "no hay"));
		}

		status.setComplete();
		try {
			String detalleprod = pro.getId() + ">" + pro.getCodigo() + ">" + pro.getBodega() + ">" + pro.getFecha()
					+ ">" + pro.getMargen() + ">" + pro.getMinimo() + ">" + pro.getPrecio() + ">" + pro.getStock() + ">"
					+ pro.getCategoria().getNombre() + ">" + pro.getMarca().getNombrem() + ">"
					+ pro.getPresentacion().getDetalle() + ">" + pro.getProveedor().getNombre();
			printLogger(detalleprod);
			detalleprod = (detalleprod.length() > 100) ? detalleprod.substring(0, 50) : detalleprod;
			printLogger("Notificacion hecha...");
			flash.addFlashAttribute("success", mensajeFlash);
		} catch (Exception e) {
			flash.addFlashAttribute("success", mensajeFlash);
			e.printStackTrace();
			printLogger("Notificacion no realizada. algo ha salido mal...null");
			return "redirect:/producto/listar";
		}
		flash.addFlashAttribute("success", mensajeFlash);
		nuevaNotificacion("fas fa-box-open", "Producto '" + producto.getNombrep() + "' agregado o modificado. ",
				"/producto/ver/" + producto.getId(), "blue");
		return "redirect:/producto/listar";
	}

	public ProductosModify nuevamodificacion(Producto producto, Producto pro, String detalle) {
		ProductosModify pro_modify = new ProductosModify();
		pro_modify.setFecha(new Date());
		pro_modify.setPrecio(producto.getPrecio());
		pro_modify.setProveedor(producto.getProveedor().getNombre());
		pro_modify.setProductomodi(productoService.findOne(pro.getId()));
		pro_modify.setStock(producto.getStock());
		pro_modify.setDetalle(detalle);
		return pro_modify;

	}
	public ProductosModify nuevamodificacionConProvee(Producto producto, Producto pro, String detalle,String proveedor) {
		ProductosModify pro_modify = new ProductosModify();
		pro_modify.setFecha(new Date());
		pro_modify.setPrecio(producto.getPrecio());
		pro_modify.setProveedor(proveedor);
		pro_modify.setProductomodi(productoService.findOne(pro.getId()));
		pro_modify.setStock(producto.getStock());
		pro_modify.setDetalle(detalle);
		return pro_modify;

	}

	@Secured({ "ROLE_ADMIN", "ROLE_INV", "ROLE_JEFEADM" })
	@RequestMapping(value = "/transformacion/{id}/{id2}/{cantidad}")
	public String transformacionStock(@PathVariable(value = "id") Long id1, @PathVariable(value = "id2") Long id2,
			@PathVariable(value = "cantidad") Long cantidad, RedirectAttributes flash) {
		System.out.print("ID1= " + id1 + " id2= " + id2 + " stock= " + cantidad);
		return "redirect:/producto/trasformacion";
	}

	@RequestMapping(value = "/savetransformacion/{producto1}/{producto2}/{stock}/{equivale}", method = {
			RequestMethod.GET }, produces = { "application/json" })
	public @ResponseBody Boolean savegettransformation(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(value = "producto1", required = true) Long producto1,
			@PathVariable(value = "producto2", required = true) Long producto2,
			@PathVariable(value = "stock", required = true) int stock,
			@PathVariable(value = "equivale", required = true) int equivale, Authentication authentication) {
		Producto producto1Obj = productoService.findOne(producto1);
		Producto producto2Obj = productoService.findOne(producto2);
		String proveedorP2 = producto1Obj.getProveedor().getNombre();
		Boolean responsef = false;
		
		//Boolean stop = true;
		
		if (producto1Obj.getStock() > 0) {
			printLogger("buscando producto producto 1... (" + producto1Obj.getNombrep() + ")\n stock de: "
					+ producto1Obj.getStock());
			int stockFirst = producto1Obj.getStock();
			int stockRevenue = stock < stockFirst ? stock : stockFirst;

			int stock2 = ((stockFirst - stock) * equivale)+producto2Obj.getStock();
			/*if (stop) {
				printLogger("VALORES: "+producto1+"\nProducto2: "+producto2+"\nStock: "+stock+"\nEquivale: "+equivale);
				printLogger("stock2: "+stock2);
				printLogger("proveedordeP2: "+producto2Obj.getProveedor().getNombre());
				return false;
			}*/
			producto1Obj.setStock(stockRevenue);
			productoService.save(producto1Obj);
			printLogger("guardando cambio de producto 1 (" + producto1Obj.getNombrep() + ")\n stock ahora de: "
					+ stockRevenue);
			printLogger("creando movimiento de modificacion de esto...");
			try {
				productomodifyService
						.save(nuevamodificacion(producto1Obj, producto1Obj, "Se sustrajo una cantidad del producto "
								+ producto1Obj.getNombrep() + " hacia " + producto2Obj.getNombrep()));
				printLogger("pasamos al producto 2");
			} catch (Exception e) {
				productomodifyService
						.save(nuevamodificacion(producto1Obj, producto1Obj,
								"Se sustrajo una cantidad del producto con ID"
										+ producto1Obj.getId() + " hacia producto con id" + producto2Obj.getId()));
				printLogger("El detalle del producto es muy largo, se tuvo que recortar \n Pasamos al producto 2");
			}

			printLogger("Stock para a sumarle: " + (stock2-producto2Obj.getStock()));
			producto2Obj.setStock(stock2);
			productoService.save(producto2Obj);
			printLogger("Se guarda el producto dos. Datos:\n Producto " + producto2Obj.getNombrep()
					+ " stock actual deberia ser " + stock2);
			Inventario inventarioForp2 = new Inventario();
			List<ProductosModify> pm_temp = productomodifyService.findAllByProductomodi(producto1Obj);
			int pm_tempSize = pm_temp.size();
			inventarioForp2.setCodigoProveedor(producto2Obj.getCodigo() + "TRAS"
					+ (pm_temp.get(pm_tempSize - 1).getId()) + 1);
			inventarioForp2.setComentario("Traslado del producto " + producto1Obj.getNombrep());
			inventarioForp2.setEstado(true);
			inventarioForp2.setFecha(new Date());
			Movimientos newmove = new Movimientos();
			movimientosService.save(newmove);
			inventarioForp2.setMovimientos(newmove);
			inventarioForp2.setProducto(producto2Obj);
			inventarioForp2.setStock(stock2);
			inventarioForp2.setZaNombrede(authentication.getName());
			inventarioService.save(inventarioForp2);
			productomodifyService.save(nuevamodificacionConProvee(producto2Obj, producto2Obj,
					"Se fue trasferido stock del producto con ID " + producto1,proveedorP2));
			printLogger("Se guarda el inventario");
			responsef = true;

		} else {
			return true;
		}
		return responsef;
	}

	@PostMapping(value = "/savetransformacion") // no sirve en post sad
	public String transformacionStock_(@Param("producto1") String producto1, @Param("producto2") String producto2,
			@Param("stock") String stock) {
		return "ok";

	}

	@Secured({ "ROLE_ADMIN", "ROLE_INV", "ROLE_JEFEADM" })
	@RequestMapping(value = "/eliminar/{id}")
	public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash) {

		if (id > 0) {
			try {
				productoService.delete(id);
				flash.addFlashAttribute("success", "Producto eliminado con éxito!");
				nuevaNotificacion("fas fa-box-open", "Producto con ID'" + id + "' eliminado!", "#", "red");
			} catch (Exception e) {
				System.out.print(e.getMessage());
				e.printStackTrace();
				// mailservice.sendEmailchris(e.toString(), "Error ProductoController");
				flash.addFlashAttribute("error",
						"El producto posiblemente tiene registros de inventariado, no se puede eliminar!");
				return "redirect:/producto/listar";
			}

		}
		return "redirect:/producto/listar";
	}

	@Secured({ "ROLE_ADMIN", "ROLE_INV", "ROLE_JEFEADM" })
	@RequestMapping(value = "/Ehistoryindv/{id}")
	public String eliminarhistoryind(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		Long idredirect = null;
		if (id > 0) {
			try {

				ProductosModify aja = productomodifyService.findById(id);
				idredirect = aja.getProductomodi().getId();
				productomodifyService.delete(id);

				flash.addFlashAttribute("success", "Historial de Producto eliminado con éxito!");
				nuevaNotificacion("fas fa-box-open", "Historial de Producto con ID'" + id + "' eliminado!", "#", "red");
			} catch (Exception e) {
				System.out.print(e.getMessage());
				e.printStackTrace();
				// mailservice.sendEmailchris(e.toString(), "Error ProductoController eliminando
				// historial");
				flash.addFlashAttribute("error",
						"El producto posiblemente tiene registros de inventariado, no se puede eliminar!");
				return "redirect:/producto/listar";
			}

		}
		return "redirect:/producto/ver/" + idredirect;
	}

	@Secured({ "ROLE_ADMIN", "ROLE_INV", "ROLE_JEFEADM" })
	@RequestMapping(value = "/updateHistory/{id}/{stock}/{comentario}")
	public String updatehistorypro(@PathVariable(value = "id") Long id, @PathVariable(value = "stock") int stock,@PathVariable(value = "comentario") String comentario,
			RedirectAttributes flash) {
		Long idpro = null;
		if (id > 0) {
			try {
				ProductosModify aja = productomodifyService.findById(id);
				idpro = aja.getProductomodi().getProductosmodify().get(0).getProductomodi().getId();
				aja.setStock(stock);
				aja.setDetalle(comentario);
				productomodifyService.save(aja);
				printLogger("datos = " + id + " stock=" + stock + " productoid" + idpro + "\n"+"comentario: "+comentario);

				flash.addFlashAttribute("success", "Historial de Producto se ha actualizado con éxito!");
				// nuevaNotificacion("fas fa-box-open", "Historial de Producto con ID'" + id +
				// "' actualizado!", "#", "red");
			} catch (Exception e) {
				// System.out.print(e.getMessage());
				// e.printStackTrace();
				// mailservice.sendEmailchris(e.toString(), "Error ProductoController eliminando
				// historial");
				flash.addFlashAttribute("error",
						"El producto posiblemente tiene registros de inventariado, no se puede actualizar!");
				return "redirect:/producto/ver/" + idpro;
			}

		}
		return "redirect:/producto/ver/" + idpro;
	}

	@Secured({ "ROLE_ADMIN", "ROLE_INV", "ROLE_JEFEADM" })
	@RequestMapping(value = { "/editar/{id}", "/clonar/{op}/{id}" })
	public String editar(@PathVariable(value = "id") Long id, @PathVariable(value = "op", required = false) Boolean op,
			Map<String, Object> model, RedirectAttributes flash) {
		op = op == null ? false : true;
		Producto producto = null;
		Producto proClon = new Producto();
		// String mensaje = "Error innesperado";

		if (id > 0) {
			producto = productoService.findOne(id);
			if (op) {
				proClon.setNombrep(producto.getNombrep());
				proClon.setPrecio(producto.getPrecio());
				proClon.setBodega(producto.getBodega());
				proClon.setFecha(producto.getFecha());
				proClon.setMinimo(producto.getMinimo());
				proClon.setMargen(producto.getMargen());

			} else
				printLogger("edicion");

			if (producto == null) {
				flash.addFlashAttribute("error", "El ID del cliente no existe en la BBDD!");
				return "redirect:/producto/listar";
			}
		} else {
			flash.addFlashAttribute("error", "El ID del cliente no puede ser cero!");
			return "redirect:/producto/listar";
		}

		model.put("producto", op ? proClon : producto);

		model.put("categoriaid", producto.getCategoria().getId());
		model.put("marcaid", producto.getMarca().getIdm());
		// model.put("margenid", producto.getMargen().getId());
		model.put("presentacionid", producto.getPresentacion().getId());
		model.put("proveedorid", producto.getProveedor().getId());
		model.put("nullchecker", 0);
		model.put("titulo", op ? "Clonar producto" : "Editar Producto");
		return "/productos/productoform";
	}

	@Secured({ "ROLE_ADMIN", "ROLE_INV" })
	@GetMapping(value = "/cargar_productos", produces = { "application/json" })
	public @ResponseBody List<Producto> marcaTodosJson() {
		List<Producto> list = productoService.findAllList();
		return list;
	}

	@Secured({ "ROLE_ADMIN", "ROLE_INV", "ROLE_JEFEADM", "ROLE_FACT" })
	@RequestMapping(value = "/fix/{id}")
	public String fixstock(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
		Producto actualprod = productoService.findOne(id);
		List<ProductosModify> lastprodm = productomodifyService.findAllByProductomodi(actualprod);
		actualprod.setStock(lastprodm.get(lastprodm.size() - 1).getStock());

		productoService.save(actualprod);
		nuevaNotificacion("fas fa-tools", "Se ha evaluado el producto " + actualprod.getNombrep(),
				"/producto/ver/" + id, "brown");

		flash.addFlashAttribute("success", "Accion ejecutada correctamente");
		return "redirect:/producto/ver/" + id;
	}

	@Secured({ "ROLE_ADMIN", "ROLE_INV", "ROLE_JEFEADM", "ROLE_FACT", "ROLE_SELLER" })
	@GetMapping(value = "/ver/{id}")
	public String ver(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
		// DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		// LocalDateTime now = LocalDateTime.now();
		// System.out.println(dtf.format(now));
		// TimeZone zone = TimeZone.getDefault();
		// System.out.println(zone.getDisplayName());
		// System.out.println(zone.getID());
		// ZoneId z = ZoneId.systemDefault();
		// String output = z.toString();
		// System.out.print(output);

		// Taller taller = clienteService.findByIdTallerWithClienteWithFactura(id);
		// List<?> taller = facturaService.probando(id);
		// Generica sin fetch no join
		// Producto producto = productoService.findOne(id);
		Producto producto = productoService.fetchProductoWithInventario(id);
		if (producto == null) {
			flash.addFlashAttribute("error", "El producto no existe en la base de datos");
			return "redirect:/producto/listar";
		}
		model.put("producto", producto);
		List<EntradasYsalidas> entra_list = new ArrayList<EntradasYsalidas>();
		for (int i = 0; i < producto.getInventarios().size(); i++) {
			EntradasYsalidas entr_salidas = new EntradasYsalidas();
			entr_salidas.setCodigo(producto.getInventarios().get(i).getCodigoProveedor());
			entr_salidas.setFecha(producto.getInventarios().get(i).getFecha());
			entr_salidas.setId(producto.getInventarios().get(i).getId());
			entr_salidas.setMovimiento(producto.getInventarios().get(i).getStock());
			entr_salidas.setColor("blue");
			entr_salidas.setUrl("/inventario/ver/" + producto.getInventarios().get(i).getMovimientos().getId());
			entr_salidas.setConcepto(producto.getInventarios().get(i).getComentario());
			entra_list.add(entr_salidas);
		}
		List<NotadeCredito> notitas = notadecreditoService.findByCodigodoc(id);
		if (!notitas.isEmpty()) {
			for (int i = 0; i < notitas.size(); i++) {
				EntradasYsalidas entr_salidas = new EntradasYsalidas();
				entr_salidas.setCodigo("NDC - " + notitas.get(i).getCodigodoc());
				entr_salidas.setFecha(notitas.get(i).getFecha());
				entr_salidas.setId(notitas.get(i).getId());
				entr_salidas.setConcepto("-");
				for (CarritoItems carritostocker : notitas.get(i).getCarrito().getCarrito()) {
					entr_salidas.setMovimiento(
							(carritostocker.getProductos().equals(producto)) ? carritostocker.getCantidad() : 0);

				}

				entr_salidas.setColor("blue");

				entra_list.add(entr_salidas);
			}
		}
		List<Facturacion> lista = facturaService.findHistorialPrecios(id);
		for (int i = 0; i < lista.size(); i++) {
			EntradasYsalidas entr_salidas = new EntradasYsalidas();
			entr_salidas.setCodigo(lista.get(i).getCodigofactura());
			entr_salidas.setFecha(lista.get(i).getFecha());
			entr_salidas.setId(lista.get(i).getId());
			entr_salidas.setColor("green");
			entr_salidas.setUrl("/factura/ver/" + lista.get(i).getId());
			for (CarritoItems carrito : lista.get(i).getCotizacion().getCarrito()) {
				if (carrito.getProductos().getId() == Long.parseLong(id.toString())) {
					entr_salidas.setMovimiento(carrito.getCantidad() * -1);
				}
			}
			entra_list.add(entr_salidas);
		}

		List<EntradasYsalidas> entra_listByDat = entra_list.stream().sorted((a, b) -> {
			try {
				return (a.getFecha().compareTo(b.getFecha()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				printLogger("Error PROD01");
				// mailservice.sendEmailchris(e.toString(), "Error ProductoController");
			}
			return 0;
		}).collect(Collectors.toList());

		model.put("movimientos", entra_listByDat);
		model.put("idp", producto.getId());
		model.put("titulo", "Detalle producto: " + producto.getNombrep());
		double precioventa = (producto.getPrecio() / ((100 - producto.getMargen()) / 100));
		model.put("precioventa", precioventa);
		return "/productos/ver";
	}

	@GetMapping(value = "/historialDePrecios/{id}", produces = { "application/json" })
	public @ResponseBody List<HistorialDePrecios> historialDePrecios(@PathVariable(value = "id") String idp,
			HttpServletRequest request, Authentication auth) {
		List<HistorialDePrecios> historyList = new ArrayList<HistorialDePrecios>();
		// ver si mostramos todos o solo los del vendedor
		Usuario user_ = usersService.findByUsername(auth.getName());

		List<Facturacion> lista = (request.isUserInRole("ROLE_ADMIN") || request.isUserInRole("ROLE_JEFEADM"))
				? facturaService.findHistorialPrecios(Long.parseLong(idp))
				: facturaService.findHistorialPreciosVendedor(Long.parseLong(idp), user_.getId());

		for (int i = 0; i < lista.size(); i++) {
			HistorialDePrecios hp = new HistorialDePrecios();
			hp.setId(lista.get(i).getId());
			hp.setFecha(lista.get(i).getFecha());
			for (CarritoItems carrito : lista.get(i).getCotizacion().getCarrito()) {
				if (carrito.getProductos().getId() == Long.parseLong(idp)) {
					hp.setIdCotizacion(carrito.getCotizacionid().getId());
					hp.setPrecio(carrito.getPrecio());
					hp.setCantidad(carrito.getCantidad());
				}
			}
			historyList.add(hp);
		}

		return historyList;
	}

	@RequestMapping(value = "/saveExpress/{nombre}", method = { RequestMethod.GET }, produces = { "application/json" })
	public @ResponseBody Long saveExpress(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(value = "nombre", required = true) String nombre) {
		Presentacion presentacion = new Presentacion();
		presentacion.setDetalle(nombre);
		presentacion.setUnidad(nombre.substring(0, 4));
		presentacionservice.save(presentacion);
		return presentacion.getId();

	}

	@Secured({ "ROLE_ADMIN", "ROLE_INV", "ROLE_JEFEADM" })
	@RequestMapping(value = "/ocultar/{id}")
	public String ocultar(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
		Producto actualprod = productoService.findOne(id);
		actualprod.setStatus(actualprod.isStatus() ? false : true);
		productoService.save(actualprod);
		nuevaNotificacion("fas fa-tools", "El producto se ha ocultado" + actualprod.getNombrep(), "/producto/ver/" + id,
				"brown");

		flash.addFlashAttribute("success", "Accion de ocultar exitosa");
		return "redirect:/producto/listarS";
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

	// @RequestMapping(value = "/minimo", method = RequestMethod.GET)
	// public String listar(Model model,@RequestParam(name = "page", defaultValue =
	// "0") int page) {
	// Pageable pageRequest = PageRequest.of(page, 30);

	// Page<Producto> producto0_ = productoService.findAllminimo(pageRequest);

	// PageRender<Producto> pageRender = new PageRender<>("", producto0_);
	// model.addAttribute("titulo", "Listado de productos minimos");
	// model.addAttribute("productos", producto0_);
	// model.addAttribute("page", pageRender);
	// return "/productos/listar";
	// }
	public void printLogger(String txt) {
		System.out.print(txt + "\n");
	}
}