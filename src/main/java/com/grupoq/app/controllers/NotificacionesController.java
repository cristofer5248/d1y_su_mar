package com.grupoq.app.controllers;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.grupoq.app.models.entity.Notificaciones;
import com.grupoq.app.models.service.INotificacionesService;
import com.grupoq.app.util.paginator.PageRender;

@Controller
@RequestMapping("/notificaciones")
public class NotificacionesController {

	@Autowired
	INotificacionesService notificacionesService;

	/* @Secured({ "ROLE_ADMIN", "ROLE_INV" }) */
	@GetMapping(value = "/ver", produces = { "application/json" })
	public @ResponseBody List<Notificaciones> verSolo10() {
		List<Notificaciones> list = notificacionesService.findTop15ByOrderByIdDesc();
		return list;
	}

	@GetMapping(value = "/givenews", produces = { "application/json" })
	public @ResponseBody List<Notificaciones> verNuevo() {
		Date date1 = new Date();	
		Date date2 = new Date(System.currentTimeMillis() - 60 * 1000);
		List<Notificaciones> list = notificacionesService.therenew(date2, date1);
		return list;
	}

	@RequestMapping(value = { "/", "/listar/{op}/{nombrep}" }, method = RequestMethod.GET)
	public String listar(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
			@PathVariable(value = "nombrep", required = false) String nombre) {
		// List<Notificaciones> notificaciones =
		// notificacionesService.findTop15ByOrderByIdDesc();
		Pageable pageRequest = PageRequest.of(page, 30);

		Page<Notificaciones> notificaciones = nombre == null ? notificacionesService.findAll(pageRequest)
				: notificacionesService.findByNombreContaining(pageRequest, nombre);

		PageRender<Notificaciones> pageRender = new PageRender<>("", notificaciones);
		model.addAttribute("titulo", "Listado de movimientos actuales");
		model.addAttribute("notificaciones", notificaciones);
		model.addAttribute("page", pageRender);
		return "/notificaciones/listar";
	}

}
