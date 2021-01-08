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

package io.elimu.a2d2.corewih;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class RestManageErrorHandler implements ResponseErrorHandler {

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
			throw new RestManageException(response.getRawStatusCode(), response.getStatusText(),
					br.lines().collect(Collectors.joining("\n")));
		}
	}

	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		return response.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
			|| response.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR;
	}
}
