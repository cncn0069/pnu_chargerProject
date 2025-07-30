package charger.main.util;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import charger.main.domain.Role;

public class JWTUtil {
	public static final String JWT_KEY = "edu.pnu.jwt";
	public static final long ACCESS_TOKEN_MSEC = 60000 * (1000) *30;
	private static final String claimName = "username";
	public static final String PREFIX = "Bearer ";
	
	private static String getJWTSource(String token) {
		if(token.startsWith(PREFIX)) return token.replace(PREFIX,"");
		return token;
	}
	public static String getJWT(String username,List<Role> roles) {
		
		List<String> roleNames = roles.stream()
                .map(role -> role.toString()) // Role에서 "ROLE_USER" 등 반환
                .collect(Collectors.toList());
		
		String src = JWT.create()
				.withClaim(claimName, username)
				.withClaim("role", roleNames)
				.withExpiresAt(new Date(System.currentTimeMillis()+ACCESS_TOKEN_MSEC))
				.sign(Algorithm.HMAC256(JWT_KEY));
		return PREFIX + src;
	}
	public static String getClaim(String token) {
		String tok = getJWTSource(token);
		return JWT.require(Algorithm.HMAC256(JWT_KEY)).build().verify(tok).getClaim(claimName).asString();
	}
	public static boolean isExpired(String token) {
		String tok = getJWTSource(token);
		return JWT.require(Algorithm.HMAC256(JWT_KEY)).build().verify(tok).getExpiresAt().before(new Date());
	}
	
}
