package es.bsc.demiurge.ws.gui.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
@Controller
public class LoginController {
	@RequestMapping("/login")
	public String showTest() {
		return "login";
	}
}
