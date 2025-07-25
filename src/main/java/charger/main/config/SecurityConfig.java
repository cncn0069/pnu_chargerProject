package charger.main.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import charger.main.fillter.JWTAuthenticationFilter;
import charger.main.fillter.JWTAuthorizationFilter;
import charger.main.persistence.MemberRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig{
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(); 
	}
	@Autowired
	private AuthenticationConfiguration authenticationConfiguration;	
	@Autowired
	private MemberRepository memberRepo;
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
		http.cors(cors->cors.configurationSource(corsSource()));
		http.authorizeHttpRequests(security->security
				.requestMatchers("/member/**").authenticated()
				.requestMatchers("/reserve/**").authenticated()
				.anyRequest().permitAll());
		http.csrf(csrf->csrf.disable());
		http.formLogin(form->form.disable());
		http.httpBasic(basic->basic.disable());
		http.sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.addFilterBefore(new JWTAuthorizationFilter(memberRepo),AuthorizationFilter.class);
		http.addFilter(new JWTAuthenticationFilter(authenticationConfiguration.getAuthenticationManager(),memberRepo));
		return http.build();
	}
		
	 private CorsConfigurationSource corsSource() {
		 CorsConfiguration config = new CorsConfiguration();
		 config.addAllowedOrigin("http://localhost:3000");
		 config.addAllowedOrigin("http://localhost:3001");
		 config.addAllowedOrigin("http://10.125.121.173:3001");
		 config.addAllowedMethod(CorsConfiguration.ALL);
		 config.addAllowedHeader(CorsConfiguration.ALL);
		 config.setAllowCredentials(true);
		 config.addExposedHeader(HttpHeaders.AUTHORIZATION);
		 UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		 source.registerCorsConfiguration("/**", config);
		 return source;
		 }
}
