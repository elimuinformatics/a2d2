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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.genericmodel.ServiceResponse;
import io.elimu.a2d2.serviceapi.performance.PerformanceHelper;
import io.elimu.a2d2.web.WebUtils;
import io.elimu.cdshookapi.entity.CDSService;
import io.elimu.cdshookapi.service.ServiceHelper;
import io.elimu.genericapi.service.GenericService;
import io.elimu.genericapi.service.GenericServiceException;
import io.elimu.genericapi.service.RunningServices;
import io.elimu.service.dao.jpa.CDSServiceRepository;
import io.elimu.service.models.ServiceInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = SimpleServiceController.BASE_REST_URL)
@Api(tags = {"cds-services", "services"})
public class SimpleServiceController {

	@Autowired
	private CDSServiceRepository repository;
	
	@Autowired
	private ServiceHelper serviceHelper;

	public static final String BASE_REST_URL = "/api/v2";
	public static final String SERVICE_PARAM = "service.id";
	public static final String BASE_SERVICE_URL = "/services/";
	public static final String REST_SERVICE_URL = BASE_SERVICE_URL + "{" + SERVICE_PARAM + "}";
	public static final String FULL_SERVICE_BASEURL = BASE_REST_URL + BASE_SERVICE_URL;
	public static final String CLIENT_PARAM="client";
	public static final String SERVICETYPE_PARAM="serviceType";
	public static final String PACKAGENAME_PARAM="packageName";

	private static final Logger log = LoggerFactory.getLogger(SimpleServiceController.class);
	
	@CrossOrigin
	@RequestMapping(value = REST_SERVICE_URL, 
	method = {RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.DELETE}, 
	consumes = "*/*", produces = "*/*")
	@ApiOperation(value = "Calling a Simple Service", notes = "A client application calls a Simple service by POSTing any content to the service endpoint, "
		+ "which can be constructed from the CDS Service base URL and an individual service id as " 
		+ SimpleServiceController.FULL_SERVICE_BASEURL + "{" + SimpleServiceController.SERVICE_PARAM + "}. ")
	public void processService(HttpServletRequest request, HttpServletResponse response, @PathVariable(SimpleServiceController.SERVICE_PARAM) String serviceId) {
		try {
			PerformanceHelper.getInstance().beginClock("SimpleServiceController", serviceId);
			request = WebUtils.log(request);
			GenericService service = RunningServices.getInstance().get(serviceId);
			if (service == null) {
				response.setStatus(404);
				try {
					response.getWriter().println("Service " + Encode.forJava(serviceId) + " not found.");
				} catch (Exception ignore) {
					//no-op
				}
				return;
			}
			log.info("Service found for the service id " + serviceId);
			try {
				ServiceRequest serviceRequest = buildRequest(request, serviceId);
				ServiceResponse serviceResponse = service.execute(serviceRequest);
				serviceResponse.addHeaderValue("X-A2D2-Service-Release-Info", service.getDependency().getExternalForm());
				WebUtils.populate(response, serviceResponse);
			}  catch (IOException | GenericServiceException e) {
				log.info("Exception occured due to  " + e.getMessage(), e);
				populate(response, serviceId, e);
			}
		} finally {
			PerformanceHelper.getInstance().endClock("SimpleServiceController", serviceId);
		}
	}

	private ServiceRequest buildRequest(HttpServletRequest request, String serviceId) throws IOException {
		ServiceRequest retval = WebUtils.buildRequest(request, serviceId);
                String path = request.getRequestURI();
                if (path != null && path.startsWith(request.getContextPath())) {
                        path = path.substring(request.getContextPath().length());
                }
                retval.setPath(path);
		return retval;
	}

	private void populate(HttpServletResponse response, String serviceId, Exception e) {
		try (PrintWriter writer = response.getWriter()) {
			GenericService service = RunningServices.getInstance().get(serviceId);
			if (service != null) {
				response.setHeader("X-A2D2-Service-Release-Info", service.getDependency().getExternalForm());
			}
			writer.println(e.getMessage());
			response.setStatus(500);
		} catch (IOException e1) {
			log.error("Couldn't print error response", e);
		}
	}
	

	@CrossOrigin
	@RequestMapping(value = "/{client}/{packageName}/{serviceType}",
	method = RequestMethod.GET,
	produces = {"application/json", "application/xml"})
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Discovery", notes = "The discovery endpoint is always available at {baseUrl}/cds-services. For example, if the baseUrl is https://example.com, the EHR would invoke")
	public
	@ResponseBody
	Map<String, List<CDSService>> getAllServices( HttpServletRequest request, HttpServletResponse response,
			@PathVariable(CLIENT_PARAM) String client,
			@PathVariable(PACKAGENAME_PARAM) String packageName,
			@PathVariable(SERVICETYPE_PARAM) String serviceType) {
		List<CDSService> list = new ArrayList<>();
		for (ServiceInfo info : repository.findAll()) {
			try {
				CDSService cdsService = serviceHelper.getDefinition(info, serviceType,client,packageName);
				if(cdsService != null) {
					list.add(cdsService);					
				}
			} catch (ClassCastException ex){ 
				log.error("Error in casting of CDSService", ex);
			}
			catch(Exception ex) { 
				log.error("Exception in getAllServices", ex);
			}
		}

		Map<String, List<CDSService>> resp = new HashMap<>();
		resp.put("services", list);

		return resp;
	}

}
