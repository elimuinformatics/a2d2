
package io.elimu.cdshookapi.api.rest;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.elimu.cdshookapi.entity.CDSService;
import io.swagger.annotations.ApiOperation;

@RestController
public class RequestForwardJWTController {

	public static final String BASE_REST_URL = "/api/jwt";
        public static final String SERVICE_PARAM = "serviceId";
        public static final String BASE_SERVICE_URL = "/services/";
        public static final String REST_SERVICE_URL = BASE_SERVICE_URL + "{" + SERVICE_PARAM + "}";
        public static final String FULL_SERVICE_BASEURL = BASE_REST_URL + BASE_SERVICE_URL;
        public static final String CLIENT_PARAM="client";
        public static final String SERVICETYPE_PARAM="serviceType";
        public static final String PACKAGENAME_PARAM="packageName";

	@Autowired
	SimpleServiceController simple;

	@CrossOrigin
	@RequestMapping(value = REST_SERVICE_URL, 
	method = {RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.DELETE}, 
	consumes = "*/*", produces = "*/*")
	@ApiOperation(value = "Calling a Simple Service", notes = "A client application calls a Simple service by POSTing any content to the service endpoint, "
		+ "which can be constructed from the CDS Service base URL and an individual service id as " 
		+ SimpleServiceController.FULL_SERVICE_BASEURL + "{" + SimpleServiceController.SERVICE_PARAM + "}. ")
	public void processService(HttpServletRequest request, HttpServletResponse response, @PathVariable(SimpleServiceController.SERVICE_PARAM) String serviceId) {
		simple.processService(request, response, serviceId);
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
		return simple.getAllServices(request, response, client, packageName, serviceType);
	}
}
