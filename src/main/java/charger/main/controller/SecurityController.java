package charger.main.controller;

import java.net.URLDecoder;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class SecurityController {
	@GetMapping("/api/user")
	public ResponseEntity<?> jwtCallBack(HttpServletRequest request){
		log.info("SecuritController :  jwtCallBack");
		String jwtToken = null;
		Cookie[] cookies = request.getCookies();
		System.out.println(cookies);
		for(Cookie cookie :cookies) {
			if (!cookie.getName().equals("jwtToken")) continue; 
			try {
				jwtToken = URLDecoder.decode(cookie.getValue(),"utf-8");
				System.out.println("cookie token "+jwtToken);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			break;
		}
		log.info(jwtToken);
		return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "\""+ jwtToken + "\"").build();
	}
}
