package es.bsc.demiurge.ws.gui.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.ws.rs.Path;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
@Controller
public class IndexController {
	@RequestMapping({"/", "/index"})
	public String showIndex() {
		return "index";
	}
}
