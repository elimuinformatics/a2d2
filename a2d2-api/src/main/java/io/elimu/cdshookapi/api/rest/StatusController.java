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

import java.util.concurrent.ForkJoinPool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import io.elimu.genericapi.service.RunningServices;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = {"actuator", "health", "status"})
public class StatusController {

	private static final Logger LOG = LoggerFactory.getLogger(StatusController.class);
	
	@CrossOrigin
	@RequestMapping(value = "/actuator/health", method = {RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.DELETE}, 
	consumes = "*/*", produces = "*/*")
	@ApiOperation(value = "Checks health of services", notes = "Checks if all services are up and running already")
	@Transactional(timeout = 3600)
	public DeferredResult<ResponseEntity<String>> healthCheck(HttpServletRequest request, HttpServletResponse response) {
		DeferredResult<ResponseEntity<String>> output = new DeferredResult<>(3600000L);
	    ForkJoinPool.commonPool().submit(() -> {
	        LOG.info("Processing in separate thread");
	        while (!RunningServices.getInstance().isStarted()) {
	        	try {
	        		Thread.sleep(1000);
	        	} catch (InterruptedException e) {
	        	
	        	}
	        }
	        HttpHeaders headers = new HttpHeaders();
	        headers.add("Content-Type", "application/json");
	        output.setResult(new ResponseEntity<String>("{\"status\": \"UP\"}", headers , HttpStatus.OK));
	    });
	    
	    LOG.info("servlet thread freed");
	    return output;
	}
}
