package io.elimu.cdshookapi.service;

import io.elimu.a2d2.cdsresponse.entity.CDSProcessResponse;
import io.elimu.a2d2.cdsresponse.entity.CDSRequest;
import io.elimu.a2d2.cdsresponse.entity.Response;
import io.elimu.cdshookapi.entity.CDSService;

public interface CDSHookService {
	public static final String SERVICE_ID_MEDICATION_PRESCRIBE = "pgx";
	public static final String SERVICE_ID_PATIENT_VIEW = "patient-view";
	public static final String SERVICE_ID_TEST = "test";
	
	public String getId();
	public CDSService getDefinition();
	public Response execute(CDSRequest cdsRequest) throws Exception;
	public CDSProcessResponse executeProcess(CDSRequest cdsRequest) throws Exception;
	
	
}
