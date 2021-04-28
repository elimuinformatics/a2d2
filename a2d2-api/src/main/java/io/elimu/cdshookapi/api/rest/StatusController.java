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

package io.elimu.cdshookapi.api.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.elimu.genericapi.service.RunningServices;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = {"actuator", "health", "status"})
public class StatusController {

	@CrossOrigin
	@RequestMapping(value = "/actuator/health", method = {RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.DELETE}, 
	consumes = "*/*", produces = "*/*")
	@ApiOperation(value = "Checks health of services", notes = "Checks if all services are up and running already")
	public void healthCheck(HttpServletRequest request, HttpServletResponse response) {
		try {
			boolean started = RunningServices.getInstance().isStarted();
			response.setStatus(started ? 200 : 503);
			response.addHeader("Content-Type", "application/json");
			response.getWriter().println("{\"status\": \"" + (started ? "UP" : "STARTING") + "\"}");
		} catch (Exception e) { 
			//no op
		}
	}
}

