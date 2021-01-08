// Copyright 2018-2020 Elimu Informatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.elimu.a2d2api;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class AuthConfig extends WebSecurityConfigurerAdapter {
 
	private static final String API_KEY_HEADER = "x-api-key";

	@Value("${API_KEYS:#{null}}")
	private String envKey;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		if (envKey == null || envKey.isEmpty()) {
			// no API key, so behave as before
			http.csrf().disable().authorizeRequests().anyRequest().permitAll().and().httpBasic().disable().formLogin();
			return;
		}

		APIKeyAuthFilter filter = new APIKeyAuthFilter(API_KEY_HEADER);
		filter.setAuthenticationManager(authentication -> {
			String apiKey = (String) authentication.getPrincipal();
			if (isKeyValid(apiKey)) {
				authentication.setAuthenticated(true);
				return authentication;
			} else {
				throw new BadCredentialsException("Access Denied.");
			}

		});

		http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				.addFilter(filter).authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll()
				.antMatchers("/api/**").authenticated().anyRequest().permitAll();
		
	}

	private boolean isKeyValid(String k) {
		List<String> items = Arrays.asList(envKey.split(","));
		return items.contains(k);
	}
}
