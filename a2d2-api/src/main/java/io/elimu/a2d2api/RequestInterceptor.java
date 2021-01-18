package io.elimu.a2d2api;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.google.common.net.HttpHeaders;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestInterceptor implements Filter {

	@Override
	public void doFilter(ServletRequest request, javax.servlet.ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		String headers = req.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
		String methods = req.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
		String httpMethod = req.getMethod();
		if (HttpMethod.OPTIONS.name().equals(httpMethod) && headers != null && methods != null) {
			HttpServletResponse response = (HttpServletResponse) servletResponse;
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, HEAD, PUT, DELETE");
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, headers);
			response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "86400");

		} else
			chain.doFilter(request, servletResponse);
	}
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		Filter.super.init(filterConfig);
	}
	
	@Override
	public void destroy() {
		Filter.super.destroy();
	}
}