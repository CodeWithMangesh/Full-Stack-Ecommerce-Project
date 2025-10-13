package com.ecommerce.project.security.jwt;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import com.ecommerce.project.security.services.UserDetailsImpl;
import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import jakarta.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.util.WebUtils;

/*This class contains utility methods for generating, parsing and validating JWT's
Include generating token from username, validating a JWT, and extract a username from token*/

@Component
public class JwtUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
	@Value("${spring.app.jwtExpirationMs}")
	private int jwtExpirationMs;
	
	@Value("${spring.app.jwtSecret}")
	private String jwtSecret;

	@Value("${spring.ecom.app.jwtCookieName}")
	private String jwtCookie;

	
	//Getting JWT from header
//	public String getJwtFromHeader(HttpServletRequest request) {
//		String bearerToken = request.getHeader("Authorization");
//		logger.debug("Authorization Header: {}", bearerToken);
//		if(null != bearerToken && bearerToken.startsWith("Bearer ")) {
//			return bearerToken.substring(7);
//		}
//		return null;
//	}

	public String getJwtFromCookie(HttpServletRequest req){
		Cookie cookie = WebUtils.getCookie(req, jwtCookie);
		if(cookie != null){
			return cookie.getValue();
		} else{
			return null;
		}
	}

	public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal){
		String jwt = generateTokenFromUsername(userPrincipal.getUsername());
		ResponseCookie cookie = ResponseCookie.from(jwtCookie, jwt)
				.path("/api")
				.maxAge(24*60*60)
				.httpOnly(false)
				.build();
		return cookie;
	}

	public ResponseCookie getCleanJwtCookie(){
		ResponseCookie cookie = ResponseCookie.from(jwtCookie, null)
				.path("/api")
				.build();
		return cookie;
	}

	//Generating Token from Username
	public String generateTokenFromUsername(String userName) {

		return Jwts.builder()
				.subject(userName)
				.issuedAt(new Date())
				.expiration(new Date( (new Date().getTime() + jwtExpirationMs) ))
				.signWith(key())
				.compact();
	}
	//Getting Username from JWT Token
	public String getUserNameFromJwtToken(String token) {
		return Jwts.parser()
				.verifyWith((SecretKey) key())
				.build().parseSignedClaims(token)
				.getPayload().getSubject();
	}
	//Generate Signing key
	
	public Key key() {
		return Keys.hmacShaKeyFor(
				Decoders.BASE64.decode(jwtSecret));
				
	}
	//Validate JWT Token
	public boolean validateJwtToken(String authToken) {
		try {
			System.out.println("validate");
			Jwts.parser()
				.verifyWith((SecretKey) key())
				.build()
				.parseSignedClaims(authToken);
			return true;
			
		} catch(MalformedJwtException exception) {
			logger.error("Invalid JWT Token");
		} catch (ExpiredJwtException e) {
			logger.error("expire");
		} catch (UnsupportedJwtException e) {
			logger.error("token is unsupported");
		} catch (IllegalArgumentException e) {
			logger.error("token claims is empty");
		}
		return false;
	}

}
