package charger.main.fillter;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import charger.main.domain.Member;
import charger.main.dto.LoginDto;
import charger.main.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter{
	
	private final AuthenticationManager authenticationManager;
	
	
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			log.info("인증 프로세스 시작");
			LoginDto member = mapper.readValue(request.getInputStream(), LoginDto.class); 
			Authentication authToken =  new UsernamePasswordAuthenticationToken(member.getUsername(), member.getPassword());
			
			return authenticationManager.authenticate(authToken);
		} catch (Exception e) {
			// TODO: handle exception
			log.error("아이디나 비번이 비었습니다.");
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
		}
		return null;
		// TODO Auto-generated method stub
	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		log.info("인증완료");
		User user = (User)authResult.getPrincipal();
		String token = JWTUtil.getJWT(user.getUsername());
		
		response.addHeader(HttpHeaders.AUTHORIZATION, token);
		response.setStatus(HttpStatus.OK.value());
		// TODO Auto-generated method stub
	}
}
