package charger.main.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import charger.main.domain.Member;
import charger.main.domain.Role;
import charger.main.util.CustomMyUtil;
import charger.main.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler{
	@Autowired
	private charger.main.persistence.MemberRepository mrp;
	@Lazy @Autowired
	private PasswordEncoder encoder;
	
	@Value("${redirect.cookie.next}")
	private String oauthCallBack;
	
//	@Value("${spring.server.ngrokIp")
//	private String ngrokIp;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		log.info("요청URL : " + request.getRequestURI());
		log.info("요청헤더Referer : " + request.getHeader("Referer"));
		
		OAuth2User user = (OAuth2User) authentication.getPrincipal();
		String username = CustomMyUtil.getUsernameFromOAuth2User(user);
		
		if (username == null) {
			log.error("on AuthenticationSuccess :  Oauth 응답에서 사용자 이름을 추출 할 수없음");
			throw new ServletException("cannot generate username from oauth2user");
		}
		
		String nick = null;
		int lastIndexUnderbar = username.lastIndexOf("_");
		if (lastIndexUnderbar != -1 && username.length() > lastIndexUnderbar+5) {
			nick = username.substring(0, lastIndexUnderbar+6);
		}
		
		mrp.save(Member.builder()
				.username(username)
				.password(encoder.encode(""))
				.nickname(nick)
				.phoneNumber("")
				.email("")
				.sex("")
				.role(Arrays.asList(Role.ROLE_MEMBER))
				.enabled(true)
				.createAt(LocalDateTime.now())
				.build()
				);
		
		String jwtToken = JWTUtil.getJWT(username,Arrays.asList(Role.ROLE_MEMBER));
		ResponseCookie cookie = ResponseCookie.from("jwtToken", URLEncoder.encode(jwtToken, "utf-8"))
		        .httpOnly(true)
		        .secure(true)
		        .sameSite("None") // ← 여기서 가능!
		        .path("/")
		        .maxAge((int)JWTUtil.ACCESS_TOKEN_MSEC)
		        .build();
		//HTTPS연결에서만 쿠키가 저장 및 전송됨
		response.addHeader("Set-Cookie",cookie.toString());
		log.info("OAuth2 인증 성공");
		response.sendRedirect(oauthCallBack);
//		response.sendRedirect("http://localhost:3000/api/login/oauth2");
	}
}