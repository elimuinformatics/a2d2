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

import java.util.List;
import java.util.Map;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import io.elimu.a2d2.exception.WorkItemHandlerException;

/**
 * This work item handler sends text messages to a phone number. It uses Twilio's services to do so.
 * An implementer will need an account with Twilio to use this WIH. Once you have your account, set three system
 * properties:
 * twilio.sid: The sid or username of the twilio account
 * twilio.authtoken: The authentication token or password of that account
 * twilio.fromphone: The phone number set up to send the messages from your account
 * 
 * The handler requires two parameters: 
 * message: A string containing the message to send
 * tophone: The phone number to send the message
 * 
 * In addition, these parameters override the corresponding twilio.xxx system properties mentioned above
 * sid
 * authtoken
 * fromphone
 */
public class SendSMSDelegate implements WorkItemHandler {

	private static final Logger log = LoggerFactory.getLogger(SendSMSDelegate.class);

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

		Map<String, Object> workItemResult = workItem.getResults();
		String messageText = (String) workItem.getParameter("message");
		String toPhoneNumber = (String) workItem.getParameter("tophone");
		String sid = (String) workItem.getParameter("sid");
		String auth = (String) workItem.getParameter("authtoken");
		String fromPhone = (String) workItem.getParameter("fromphone");

		// these parameters can be obtained from system properties if not provided by the task
		if (sid == null)
			sid = System.getProperty("twilio.sid");
		if (auth == null)
			auth= System.getProperty("twilio.authtoken");
		if (fromPhone == null)
			fromPhone = System.getProperty("twilio.fromphone");

		List<String> missingValues = new java.util.ArrayList<String>();
		if (messageText == null)
			missingValues.add("message");
		if (toPhoneNumber == null)
			missingValues.add("tophone");
		if (sid == null)
			missingValues.add("sid");
		if (auth == null)
			missingValues.add("authtoken");
		if (fromPhone == null)
			missingValues.add("fromphone");

		if (missingValues.size() > 0) {
			log.error("Required parameters not provided");
			throw new WorkItemHandlerException("SendSMSDelegate requires these work item parameters: " + missingValues.toString());
		}
				
		try {
			Twilio.init(sid, auth);
			Message message = Message.creator(new PhoneNumber(toPhoneNumber), // to
					new PhoneNumber(fromPhone), // from
					messageText).create();
			workItemResult.put("status", message.getStatus().toString());
			log.debug("Sent text message");
		}
		catch (Exception twilioException) {
			log.error("Error sending SMS message via Twilio");
			throw new WorkItemHandlerException("SendSMSDelegate could not send message: " + twilioException.getMessage());
		}
		manager.completeWorkItem(workItem.getId(), workItemResult);

	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// we should never invoke manager->abortWorkItem on a service task that is
		// synchronous, even if an error occurs
		// manager.abortWorkItem(workItem.getId());
	}

}
