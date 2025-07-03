package charger.main.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberController {
	@GetMapping({"/", "/index"})
	 public String index() {
	 return "index";
	 }
}
