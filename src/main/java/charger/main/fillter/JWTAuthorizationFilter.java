package charger.main.fillter;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;

import charger.main.domain.Member;
import charger.main.persistence.MemberRepository;
import charger.main.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JWTAuthorizationFilter extends OncePerRequestFilter {
	private final MemberRepository mrp;
	
	private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
	    response.setStatus(status);
	    response.setContentType("application/json;charset=UTF-8");
	    response.getWriter().write("{\"error\": \"" + message + "\"}");
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException,TokenExpiredException {
		log.info("권한인가 프로세스 시작 ");
		String srcToken = null;
		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		String bearer = JWTUtil.PREFIX;
		String tokener = JWTUtil.JWT_KEY;
		
		
		// 1. Authorization 헤더에서 토큰 추출
		if (authHeader != null && authHeader.startsWith(bearer)) {
		    srcToken = authHeader.substring(bearer.length());
		    log.info("Authorization 헤더에서 토큰 수신됨: " + srcToken);
		}
		
		
		// 2. 쿠키에서 토큰 추출 (Authorization 헤더가 없을 때)
		if (srcToken == null && request.getCookies() != null) {
		    for (Cookie cookie : request.getCookies()) {
		        if ("jwtToken".equals(cookie.getName())) {
//		            srcToken = cookie.getValue();
		            srcToken = URLDecoder.decode(cookie.getValue(),"utf-8");
		            log.info("쿠키에서 토큰 수신됨: " + srcToken);
		        }else {
		        	log.info("불러와진 쿠키이름 : " + cookie.getName());
		        }
		    }
		}else {
			log.error("쿠키 없음");
		}
		
		// 3. 토큰이 없거나 Bearer 형식이 아닐 경우 필터 패스
		if (srcToken == null || srcToken.isBlank()) {
		    filterChain.doFilter(request, response);
		    log.info("토큰이 없거나 찾지 못함");
		    return;
		}
		
		// 토큰 베어러 잘 못붙을 경우 대비
    	if (srcToken.startsWith("Bearer")) {
    		srcToken = srcToken.substring("Bearer".length()).trim();
    		log.info("Bearer 수정 : " + srcToken);
		}
    	
    	// 토큰 베어러+ 잘 못붙을 경우 대비
    	if (srcToken.startsWith("+")) {
    		srcToken = srcToken.substring("Bearer+".length()).trim();
    		log.info("Bearer 수정 : " + srcToken);
		}

		String jwtToken = srcToken.replaceAll(bearer, "");
		String username = "";
		// >> 쿠키 만료시 여기서 부터 문제생김
		try {
	        username = JWT.require(Algorithm.HMAC256(tokener))
	                .build()
	                .verify(jwtToken)
	                .getClaim("username")
	                .asString();
	    } catch (TokenExpiredException ex) {
	        // 토큰 만료됨
	        sendErrorResponse(response, 401, "토큰이 만료되었습니다.");
	        return;
	    } catch (Exception ex) {
	        // 그 외 JWT 파싱/서명 오류
	        sendErrorResponse(response, 401, "유효하지 않은 토큰입니다.");
	        return;
	    }
		Optional<Member> opt = mrp.findById(username);
		if (!opt.isPresent()) {
			filterChain.doFilter(request, response);
			return;
		}
		Member findMembers = opt.get();
		User user = new User(findMembers.getUsername(),findMembers.getPassword(),AuthorityUtils.createAuthorityList(findMembers.getRole().toString()));
		Authentication authentication  = new UsernamePasswordAuthenticationToken(user, null,user.getAuthorities());

		log.info("권한 인증 성공");
		log.info("로그인 시도 유저 {}",authentication.getName());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		//리스폰스 헤더에 인증 정보 추가
		//response.addHeader(HttpHeaders.AUTHORIZATION,JWTUtil.getJWT(jwtToken));
		
		filterChain.doFilter(request, response);
		log.info("권한 인가 성공");
	}
}
