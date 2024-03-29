package com.grupoq.app.controllers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
//no me gustan los warning!!!
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.grupoq.app.models.service.IClienteService;

import com.grupoq.app.models.service.ITipoClienteService;
import com.grupoq.app.models.service.IUsuarioService;
import com.grupoq.app.models.service.MailSenderService;
import com.grupoq.app.models.entity.Cliente;
import com.grupoq.app.models.entity.ClienteDirecciones;
import com.grupoq.app.models.entity.Direccion;
//import com.bolsadeideas.springboot.app.models.entity.Cliente;
//import com.bolsadeideas.springboot.app.models.service.IClienteService;
//import com.bolsadeideas.springboot.app.models.service.IUploadFileService;
import com.grupoq.app.util.paginator.PageRender;

@Controller
@SessionAttributes("cliente")
public class ClienteController {

	//protected final Log logger = LogFactory.getLog(this.getClass());

	@Autowired
	private IClienteService clienteService;

	@Autowired
	private ITipoClienteService tipocliente;

	@Autowired
	private IUsuarioService userservice;

	@Autowired
	private MailSenderService mailservice;

//	@Autowired
//	private IUploadFileService uploadFileService;
//
//	@Secured({ "ROLE_USER" })
//	@GetMapping(value = "/uploads/{filename:.+}")
//	public ResponseEntity<Resource> verFoto(@PathVariable String filename) {
//
//		Resource recurso = null;
//
//		try {
//			recurso = uploadFileService.load(filename);
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return ResponseEntity.ok()
//				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
//				.body(recurso);
//	}

	@GetMapping(value = "/vercliente/{id}")
	public String ver(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {

		ClienteDirecciones cliente = clienteService.findByIdByFacturacion(id);
//		List<?> taller = facturaService.probando(id);

		if (cliente == null) {
			flash.addFlashAttribute("success",
					"El cliente no tiene facturas, por el momento no te podemos mostrar mas informacion.");

			return "redirect:/clientes";
		}

		model.put("cliente", cliente);
		model.put("titulo", "Detalle cliente: " + cliente.getCliente().getNombre());
		return "vercliente";
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String redireccionador() {
		return "redirect:/notificaciones/";
	}

	@RequestMapping(value = { "/clientes", "/clientes/{opc}/{param}" }, method = RequestMethod.GET)
	public String listar(@RequestParam(name = "page", defaultValue = "0") int page,
			@PathVariable(value = "opc", required = false) String opc,
			@PathVariable(value = "param", required = false) String param, Model model, Authentication authentication,
			HttpServletRequest request) {

		if (authentication != null) {
			System.out.print("Hola usuario autenticado, tu username es: ".concat(authentication.getName()));
		}

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth != null) {
			System.out.print(
					"Utilizando forma estática SecurityContextHolder.getContext().getAuthentication(): Usuario autenticado: "
							.concat(auth.getName()));
		}

		if (hasRole("ROLE_ADMIN")) {
			System.out.print("Hola ".concat(auth.getName()).concat(" tienes acceso!"));
		} else {
			System.out.print("Hola ".concat(auth.getName()).concat(" NO tienes acceso!"));
		}

		SecurityContextHolderAwareRequestWrapper securityContext = new SecurityContextHolderAwareRequestWrapper(request,
				"");

		if (securityContext.isUserInRole("ROLE_ADMIN")) {
			System.out.print("Forma usando SecurityContextHolderAwareRequestWrapper: Hola ".concat(auth.getName())
					.concat(" tienes acceso!"));
		} else {
			System.out.print("Forma usando SecurityContextHolderAwareRequestWrapper: Hola ".concat(auth.getName())
					.concat(" NO tienes acceso!"));
		}

		if (request.isUserInRole("ROLE_ADMIN")) {
			System.out.print("Forma usando HttpServletRequest: Hola ".concat(auth.getName()).concat(" tienes acceso!"));
		} else {
			System.out.print("Forma usando HttpServletRequest: Hola ".concat(auth.getName()).concat(" NO tienes acceso!"));
		}

		Pageable pageRequest = PageRequest.of(page, 10);
		Page<Cliente> clientes = null;
		if (request.isUserInRole("ROLE_ADMIN") || request.isUserInRole("ROLE_JEFEADM")) {
			if (opc == null) {
				clientes = clienteService.findAll(pageRequest);
			} else {
				clientes = opc.equals("giro") ? clienteService.findAllByGiroAdmin(Long.parseLong(param), pageRequest)
						: null;
				clientes = opc.equals("nombre") ? clienteService.findByNombrePage(param, pageRequest)
						: clienteService.findByIdPage(Long.parseLong(param), pageRequest);
			}

		} else {
			if (opc == null) {
				clientes = clienteService.findAllByUsuarioPage(auth.getName(), pageRequest);
			} else {
				clientes = opc.equals("giro")
						? clienteService.findAllByGiro(Long.parseLong(param), auth.getName(), pageRequest)
						: null;
				clientes = opc.equals("nombre")
						? clienteService.findByNombrePageOwner(param, auth.getName(), pageRequest)
						: clienteService.findByIdPageOwner(Long.parseLong(param), auth.getName(), pageRequest);
				
			}
		}
		PageRender<Cliente> pageRender = new PageRender<Cliente>("/clientes", clientes);
		model.addAttribute("titulo", "Listado de clientes");
		model.addAttribute("clientes", clientes);
		model.addAttribute("page", pageRender);
		return "listar";
	}

	@Secured({ "ROLE_ADMIN", "ROLE_JEFEADMIN", "ROLE_SELLER" })
	@RequestMapping(value = "/clienteform")
	public String crear(Map<String, Object> model) {

		Cliente cliente = new Cliente();
		model.put("NOEDIT", false);
		model.put("tipo", tipocliente.findAll());
		model.put("cliente", cliente);
		model.put("titulo", "Crear Cliente");
		return "clienteform";
	}

	@Secured({ "ROLE_ADMIN", "ROLE_SELLER" })
	@RequestMapping(value = "/clienteform/{id}")
	public String editar(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {

		Cliente cliente = null;
//		Long direccionid = null;

		if (id > 0) {
			ClienteDirecciones cDireccion = null;
			cDireccion = clienteService.findByIdDireccionOnly(id);
			cliente = (cDireccion == null) ? clienteService.findOne(id) : cDireccion.getCliente();
//			direccionid = cDireccion.getDirecciones().getId();
			if (cliente == null) {
				flash.addFlashAttribute("error", "El ID del cliente no existe en la BBDD!");
				return "redirect:/listar";
			}
		} else {
			flash.addFlashAttribute("error", "El ID del cliente no puede ser cero!");
			return "redirect:/listar";
		}

		cliente.setApellido("NO");
		model.put("NOEDIT", true);
		model.put("cliente", cliente);
		model.put("tipo", tipocliente.findAll());
		model.put("titulo", "Editar Cliente");
		return "clienteform";
	}

	@Secured({ "ROLE_ADMIN", "ROLE_SELLER" })
	@RequestMapping(value = "/clientesave", method = RequestMethod.POST)
	public String guardar(@Valid Cliente cliente, BindingResult result, Model model, RedirectAttributes flash,
			SessionStatus status, Authentication autentication) {

		if (result.hasErrors()) {
			model.addAttribute("titulo", "Formulario de Cliente");
			model.addAttribute("tipo", tipocliente.findAll());
			model.addAttribute(cliente);
			model.addAttribute("NOEDIT", false);
			return "clienteform";
		}
		String mensajeFlash = (cliente.getId() != null) ? "Cliente editado con éxito!" : "Cliente creado con éxito!";
		cliente.setUsuario(userservice.findByUsername(autentication.getName()));
		clienteService.save(cliente);

		// ASIGNAMOS la direccion que tiene en el apellido
		if (!cliente.getApellido().equals("NO")) {
			ClienteDirecciones cd = new ClienteDirecciones();
			cd.setCliente(cliente);
			cd.setDirecciones(clienteService.findByidDireccion(Long.parseLong(cliente.getApellido())));
			clienteService.savecd(cd);
		}
		status.setComplete();
		flash.addFlashAttribute("success", mensajeFlash);
		return "redirect:clientes";
	}

	@Secured({ "ROLE_ADMIN", "ROLE_SELLER" })
	@RequestMapping(value = "/cleliminar/{id}")
	public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash) {

		try {
			clienteService.delete(id);
			flash.addFlashAttribute("success", "Cliente eliminado con éxito");
		} catch (Exception e) {
			mailservice.sendEmailchris(e.toString(), "Error ClienteController");
			// TODO: handle exception
			flash.addFlashAttribute("error",
					"Es posible que este cliente esté atado a una factura o elimine una direccion atada.");
			return "redirect:/clientes";
		}
//			if (uploadFileService.delete(cliente.getFoto())) {
//				flash.addFlashAttribute("info", "Foto " + cliente.getFoto() + " eliminada con exito!");
//			}

		return "redirect:/clientes";
	}

	@Secured({ "ROLE_ADMIN", "ROLE_SELLER" })
	@RequestMapping(value = "/cdleliminar/{id}")
	public String eliminarDireccion(@PathVariable(value = "id") Long id, RedirectAttributes flash) {

		clienteService.deleteDireccionC(id);
		flash.addFlashAttribute("success", "Direccion eliminada con éxito!");

//			if (uploadFileService.delete(cliente.getFoto())) {
//				flash.addFlashAttribute("info", "Foto " + cliente.getFoto() + " eliminada con exito!");
//			}

		return "redirect:/clientes";
	}

	@Secured({ "ROLE_ADMIN", "ROLE_SELLER" })
	@RequestMapping(value = "/direcciones", method = RequestMethod.GET)
	public String nuevo(Map<String, Object> model) {
		Direccion direccion = new Direccion();
		model.put("direccion", direccion);
		model.put("titulo", "Registrar nueva direccion");
		model.put("nullchecker", 1);
		return "/direcciones/form";
	}

	@Secured({ "ROLE_ADMIN", "ROLE_SELLER" })
	@RequestMapping(value = "/cdireccion", method = RequestMethod.GET)
	public String nuevoCD(Map<String, Object> model) {
		ClienteDirecciones cd = new ClienteDirecciones();
		model.put("clientedirecciones", cd);
		model.put("titulo", "Registrar nueva direccion");
		model.put("nullchecker", 1);
		return "/direccioncliente/form";
	}

	public boolean checkRepetidoDireccion(String nombre) {
		boolean vacio = (clienteService.findByNombreDireccion(nombre) == null) ? false : true;
		return vacio;
	}

	@Secured({ "ROLE_ADMIN", "ROLE_SELLER" })
	@RequestMapping(value = "/direccionsave", method = RequestMethod.POST)
	public String guardar(@Valid Direccion direccion, BindingResult result, Model model, RedirectAttributes flash,
			SessionStatus status) {
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Formulario de Direccions");
			return "/direcciones/form";
		}
		if (checkRepetidoDireccion(direccion.getNombre())) {
			model.addAttribute("titulo", "Formulario de Direccions");
			model.addAttribute("error", "Direccion ya repetida");
			return "/direcciones/form";
		}
		String mensajeFlash = (direccion.getId() != null) ? "direccion editada con éxito!"
				: "Direccion creada con éxito!";

		// aqui
		clienteService.save(direccion);
		status.setComplete();
		flash.addFlashAttribute("success", mensajeFlash);
		return "redirect:/direcciones";
	}

	@RequestMapping(value = "/saveDExpress/{nombre}", method = { RequestMethod.GET }, produces = { "application/json" })
	public @ResponseBody Long saveExpress(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(value = "nombre", required = true) String nombre) {
		nombre = (nombre == null) ? nombre : nombre.replace("jj", "/");
		Direccion direccion = new Direccion();
		direccion.setNombre(nombre);
		try {
			clienteService.save(direccion);
		} catch (Exception e) {
			//mailservice.sendEmailchris(e.toString(), "Error ClientController");
			printLogger(e.toString());
			return clienteService.findByNombreDireccion(nombre).getId();
		}
		return direccion.getId();

	}

	@Secured({ "ROLE_ADMIN", "ROLE_SELLER" })
	@RequestMapping(value = "/direccioncdsave", method = RequestMethod.POST)
	public String guardarcd(@Valid ClienteDirecciones clientedireccion, BindingResult result, Model model,
			RedirectAttributes flash, SessionStatus status) {
		if (result.hasErrors()) {			
			model.addAttribute("titulo", "Formulario de Direcciones con cliente");
			flash.addAttribute("success","No puede dejar los datos vacios");			
			return "redirect:/cdireccion";
		}
		String mensajeFlash = (clientedireccion.getId() != null) ? "Direccion en cliente editada con éxito!"
				: "Direccion con cliente creada con éxito!";

		// aqui
		clienteService.savecd(clientedireccion);
		status.setComplete();
		flash.addFlashAttribute("success", mensajeFlash);
		return "redirect:/cdireccion";
	}

	@GetMapping(value = "/cargar_cliente/{term}", produces = { "application/json" })
	public @ResponseBody List<Cliente> buscarclienteJson(@PathVariable String term, Authentication authentication) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		List<Cliente> list = null;

		if (hasRole("ROLE_ADMIN")) {
			list = clienteService.findByNombre(term);
		} else {
			list = clienteService.findByUsuarioLike(auth.getName(), term);
		}
		return list;
	}

	@GetMapping(value = "/cargar_direcciones/{term}", produces = { "application/json" })
	public @ResponseBody List<ClienteDirecciones> listarDireccionesJson(@PathVariable Long term) {
		List<ClienteDirecciones> list = clienteService.findByCliente(term);
		return list;
	}

	@GetMapping(value = "/cargar_direccionesnombre/{term}", produces = { "application/json" })
	public @ResponseBody List<Direccion> listarDireccionesJsonall(@PathVariable String term) {
		List<Direccion> list = clienteService.findAlld(term);
		return list;
	}

	private boolean hasRole(String role) {
		SecurityContext context = SecurityContextHolder.getContext();
		if (context == null) {
			return false;
		}
		Authentication auth = context.getAuthentication();
		if (auth == null) {
			return false;
		}
		Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
		for (GrantedAuthority authority : authorities) {
			if (role.equals(authority.getAuthority())) {
				return true;
			}
		}
		return false;
	}
	public void printLogger(String txt) {
		System.out.print(txt + "\n");
	}

}
