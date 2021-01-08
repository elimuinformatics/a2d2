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

package io.elimu.a2d2.web;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RereadableHttpServletRequestWrapper extends HttpServletRequestWrapper {

	private static final Logger logger = LoggerFactory.getLogger(RereadableHttpServletRequestWrapper.class);

	private final String body;

	public RereadableHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		StringBuilder stringBuilder = new StringBuilder();
	    try {
	        InputStream inputStream = request.getInputStream();
	        if (inputStream != null) {
	        	try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
	        		char[] charBuffer = new char[128];
		            int bytesRead = -1;
		            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
		                stringBuilder.append(charBuffer, 0, bytesRead);
		            }
	        	}
	        } else {
	            stringBuilder.append("");
	        }
	    } catch (IOException ex) {
	        logger.error("Error reading the request body...");
	    }
	    body = stringBuilder.toString();
	}

	@Override
	public ServletInputStream getInputStream () throws IOException {
	    final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());
	    return new ServletInputStream() {
	        public int read () throws IOException {
	            return byteArrayInputStream.read();
	        }

			@Override
			public boolean isFinished() {
				return byteArrayInputStream.available() <= 0;
			}

			@Override
			public boolean isReady() {
				return byteArrayInputStream.available() > 0;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
				//do nothing
			}
	    };
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}

	public String getBody() {
		return body;
	}
}
