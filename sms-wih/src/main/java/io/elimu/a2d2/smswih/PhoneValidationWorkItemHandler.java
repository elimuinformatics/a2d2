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

package io.elimu.a2d2.smswih;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.lookups.v1.PhoneNumber;

import io.elimu.a2d2.exception.WorkItemHandlerException;

/**
 * This work item handler validates a phone number. It uses Twilio's Lookup services to do so.
 * An implementer will need an account with Twilio to use this WIH. 
 * The handler requires five parameters: 
 * 
 * phone: The phone number to validate
 * sid: The sid or username of the twilio account
 * authtoken: The authentication token or password of that account
 * 
 * This will return the following results:
 * 
 * isValid: Boolean value if the phone is a valid number
 * countryCode: if isValid is true, it will return the countryCode of the phone number
 * isUSANumber: if isValid is true, it will say true if the countryCode is "US"
 * errorCode: if isValid is false, it will return the error code from the API (20404 for not found)
 * errorMessage: if isValid is false, it will return a descriptive message about why it is not valid
 */
public class PhoneValidationWorkItemHandler implements WorkItemHandler {

	private static final Logger log = LoggerFactory.getLogger(SendSMSDelegate.class);

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		Map<String, Object> workItemResult = workItem.getResults();
		String phone = (String) workItem.getParameter("phone");
		String sid = (String) workItem.getParameter("sid");
		String auth = (String) workItem.getParameter("authtoken");
		// these parameters can be obtained from system properties if not provided by the task
		if (sid == null) {
			sid = System.getProperty("twilio.sid");
		}
		if (auth == null) {
			auth= System.getProperty("twilio.authtoken");
		}
		List<String> missingValues = new java.util.ArrayList<String>();
		if (phone == null) {
			missingValues.add("tophone");
		}
		if (sid == null) {
			missingValues.add("sid");
		}
		if (auth == null) {
			missingValues.add("authtoken");
		}
		if (missingValues.size() > 0) {
			log.error("Required parameters not provided");
			throw new WorkItemHandlerException("PhoneValidationWorkItemHandler requires these work item parameters: " + missingValues.toString());
		}
		try {
			Twilio.init(sid, auth);
			PhoneNumber phoneNumber = PhoneNumber.fetcher(
					new com.twilio.type.PhoneNumber(phone))
				.setType(Arrays.asList("carrier", "type")).fetch();
			//here, the phone number is valid
			//based on country code we can determine nationality
			workItemResult.put("isValid", phoneNumber.getCarrier().get("error_code") == null && !"landline".equalsIgnoreCase(phoneNumber.getCarrier().get("type")));
			if (phoneNumber.getCarrier().get("error_code") == null && !"landline".equalsIgnoreCase(phoneNumber.getCarrier().get("type"))) {
				workItemResult.put("isUSANumber", "US".equalsIgnoreCase(phoneNumber.getCountryCode()));
				workItemResult.put("countryCode", phoneNumber.getCountryCode());
			} else if ("landline".equalsIgnoreCase(phoneNumber.getCarrier().get("type"))) {
				workItemResult.put("errorCode", "-1");
				workItemResult.put("errorMessage", "Phone is a landline, and not SMS enabled");
			} else {
				workItemResult.put("errorCode", phoneNumber.getCarrier().get("error_code"));
				workItemResult.put("errorMessage", parseErrorMessage(phoneNumber.getCarrier().get("error_code")));
			}
			manager.completeWorkItem(workItem.getId(), workItemResult);
		} catch (ApiException goodError) {
			if (goodError.getCode() == 20404) {
				//here, the phone number is NOT valid
				workItemResult.put("isValid", Boolean.FALSE);
				workItemResult.put("errorCode", goodError.getCode());
				workItemResult.put("errorMessage", goodError.getMessage());
				manager.completeWorkItem(workItem.getId(), workItemResult);
			} else {
				log.error("Twilio Lookup API found a problem", goodError);
				throw new WorkItemHandlerException("Twilio Lookup API found a problem", goodError);
			}
		} catch (Exception badError) {
			//here there's a problem. We need to retun
			log.error("Twilio Lookup API found a problem", badError);
			throw new WorkItemHandlerException("Twilio Lookup API found a problem", badError);
		}

	}

	String parseErrorMessage(String code) {
		try {
			int theCode = Integer.valueOf(code);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree(getClass().getResourceAsStream("/error-msgs.json"));
			for (int index = 0; index < node.size(); index++) {
				int thisCode = node.get(index).get("code").asInt();
				if (thisCode == theCode) {
					JsonNode msg = node.get(index).get("secondary_message");
					if (msg == null || msg.isNull()) {
						msg = node.get(index).get("message");
					}
					if (msg != null && msg.isTextual()) {
						return msg.asText();
					}
				}
			}
		} catch (Exception e) {
			log.error("Couldn't read error-msgs.json");
		}
		return "Undetermined error code validating phone: " + code;
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// sync service. No abort implementation
	}

}
