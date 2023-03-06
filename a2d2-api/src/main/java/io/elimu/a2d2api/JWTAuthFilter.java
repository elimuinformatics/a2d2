package io.elimu.a2d2api;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import io.elimu.serviceapi.config.JWTAuthUtil;

public class JWTAuthFilter extends AbstractPreAuthenticatedProcessingFilter  {

	private static final Logger LOGGER = LoggerFactory.getLogger(JWTAuthFilter.class);
	
	private final String startPath;
	
	public JWTAuthFilter(String startPath) {
		this.startPath = startPath;
		setAuthenticationManager(authentication -> {
			String jwtToken = (String) authentication.getPrincipal();
			String path = (String) authentication.getCredentials();
			if (path.contains(startPath)) {
				path = path.substring(path.indexOf(startPath) + startPath.length());
				String[] parts = path.split("/");
				String serviceId = parts.length > 1 ? parts[1]: "";
				authentication.setAuthenticated(JWTAuthUtil.isValidJwt(jwtToken, serviceId));
			}
			return authentication;
		});
	}
	
	@Override
	protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
		String path = request.getRequestURI();
		if (request.getContextPath() != null && request.getContextPath().length() > 0) {
			path =  path.replace(request.getContextPath(), "");
		}
		return path;
	}
	
	@Override
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		return header == null ? null : header.replace("Bearer ", "").trim();
	}

	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (getPreAuthenticatedCredentials((HttpServletRequest) request).toString().contains(startPath)) {
			super.doFilter(request, response, chain);
		} else {
			LOGGER.trace("Did not JWT authenticate since request did not match " + this.startPath);
			chain.doFilter(request, response);
		}
	}
}