<#setting number_format="computer">
<#assign internalKeys = ["io.elimu.fhir:taskid", "io.elimu.fhir:insurance", "io.elimu.fhir:intent", "io.elimu.fhir:encounter", "io.elimu.fhir:reasonCode", "io.elimu.fhir:reasonRef", "io.elimu.fhir:focus", "io.elimu.fhir:for"]>
<#assign inputs = event.task.taskData.taskInputVariables>
<#assign allKeys = inputs?keys>
<#assign otherKeys = allKeys?filter(x -> !(internalKeys?seq_contains(x)))>
<#assign ipa = event.task.peopleAssignments>
<#macro toTaskStatus status><#compress>
	<#if status??>
		<#if status.name() == "Completed">"completed"
		<#elseif status.name() == "Created">"draft"
		<#elseif status.name() == "Error">"entered-in-error"
		<#elseif status.name() == "Failed">"failed"
		<#elseif status.name() == "InProgress">"in-progress"
		<#elseif status.name() == "Obsolete">"cancelled"
		<#elseif status.name() == "Exited">"cancelled"
		<#elseif status.name() == "Ready">"requested"
		<#elseif status.name() == "Reserved">"accepted"
		<#elseif status.name() == "Suspended">"on-hold"
		<#else>null
		</#if>
	</#if>
</#compress></#macro>
<#macro toTaskPriority priority><#compress>
	<#if priority??>
		<#if priority == 1>
			"stat"
		<#elseif priority == 2>
			"asap"
		<#elseif priority == 3>
			"urgent"
		<#else>
			"routine"
		</#if>
	<#else>
		null
	</#if>
</#compress></#macro>
<#macro asValueType value><#compress>
	<#if value??>
		<#if value?is_date>
			"valueDateTime": "${value?datetime?iso_local}"
		<#elseif value?is_boolean>
			"valueBoolean": ${value?c}
		<#elseif value?is_number && value?floor != value>
			"valueDecimal": ${value?c}
		<#elseif value?is_number && value?floor == value>
			"valueInteger": ${value?long?c}
		<#elseif value?is_string>
			"valueString": "${value}"
		<#else>
			"valueString": "${value?c}"
		</#if>
	</#if>
</#compress></#macro>
<#macro addPerformerType entity sysName><#compress>
<#if entity??>{
  "coding": [
    {
      "system": "${sysName}",
      "code": "<#if entity.class.simpleName?contains("Group")>Organization/<#else>Patient/</#if>${entity.id}",
      "display": "${entity.id}"
    }
  ],
  "text": "${entity.id}"
}</#if>
</#compress></#macro>
<#macro addPerformerTypes entities sysName><#compress>
  <#list entities as entity>
    <@addPerformerType entity sysName/>
    <#sep>,</#sep>
  </#list>
</#compress></#macro>
<#macro toOrgEntityReference orgEntity><#compress>
<#if orgEntity??>{
    "reference": "<#if orgEntity.class.simpleName?contains("Group")>Organization/<#else>Patient/</#if>${orgEntity.id}",
    "display": "${orgEntity.id}"
}</#if>
</#compress></#macro>

{
  "resourceType": "Task",
  <#if inputs["io.elimu.fhir:taskid"]??>
  	"id": "${event.task.inputs["io.elimu.fhir:taskid"]?c}",
  </#if>
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><b>description</b>: ${(event.task.description)!}</p><p><b>note</b>: ${(event.task.name)!}</p></div>"
  },
  <#if event.task.id??>
	  "identifier": [
	    {
	      "use": "secondary",
	      "system": "http://hl7.org/fhir/identifier-use",
	      "value": "${(event.task.id)!}"
	    }
	  ],
  </#if>
  "basedOn": [
    {
      "display": "${(event.task.formName)!}-${(event.task.id)!}"
    }
  ],
  <#if ipa.potentialOwners?size == 1>
    "groupIdentifier": {
      "use": "official",
      <#assign grp = ipa.potentialOwners[0]>
      "system": "http:/elimu.io/jbpm/<#if grp.class.simpleName?contains("Group")>group<#else>user</#if>/identifiers",
      "value": "${grp.id}"
    },
  </#if>
  "status": <@toTaskStatus event.task.taskData.status/>,
  "businessStatus": {
    "text": "${event.task.taskData.status.name()}"
  },
  <#if inputs["io.elimu.fhir:intent"]??>
     "intent": "${inputs["io.elimu.fhir:intent"]}",
  </#if>
  "priority": <@toTaskPriority event.task.priority/>,
  "code": {
    "text": "${(event.task.formName)!}"
  },
  "description": "${(event.task.description)!}",
  <#if inputs["io.elimu.fhir:focus"]??>
     "focus": {
     	"reference": "${inputs["io.elimu.fhir:focus"]}" 
     },
  </#if>
  <#if inputs["io.elimu.fhir:for"]??>
     "for": {
     	"reference": "${inputs["io.elimu.fhir:for"]}" 
     },
  </#if>
  <#if inputs["io.elimu.fhir:encounter"]??>
    "encounter": {
      "reference": "Encounter/${inputs["io.elimu.fhir:encounter"]}",
      "display": "${inputs["io.elimu.fhir:encounter"]}"
	},
  </#if>  
  "executionPeriod": {
    <#if event.task.taskData.activationTime??>
      "start": "${event.task.taskData.activationTime?datetime?iso_local}",
      <#else>"start": null,
    </#if>
    <#if event.task.taskData.expirationTime??>
      "end": "${event.task.taskData.expirationTime?datetime?iso_local}"
    <#else>"end": null
    </#if>
  },
  <#if event.task.taskData.createdOn??>
    "authoredOn": "${event.task.taskData.createdOn?datetime?iso_local}",
  </#if>
  "lastModified": "${now?datetime?iso_local}",
  <#if event.task.taskData.createdBy??>
    "requester": <@toOrgEntityReference event.task.taskData.createdBy/>,
  </#if>
  "performerType": [
  	<@addPerformerTypes ipa.businessAdministrators "org.jbpm.task:peopleassignments:businessadministrator"/>
  	<#if (ipa.businessAdministrators?size >= 1) && (((ipa.excludedOwners?size >= 1 || ipa.potentialOwners?size >= 1) || ipa.taskStakeholders?size >= 1) || ipa.taskInitiator??)>,</#if>
  	<@addPerformerTypes ipa.excludedOwners "org.jbpm.task:peopleassignments:excludedowner"/>
  	<#if (ipa.businessAdministrators?size >= 1 || ipa.excludedOwners?size >= 1) && ((ipa.potentialOwners?size >= 1 || ipa.taskStakeholders?size >= 1) || ipa.taskInitiator??)>,</#if>
  	<@addPerformerTypes ipa.potentialOwners "org.jbpm.task:peopleassignments:potentialowner"/>
  	<#if ((ipa.businessAdministrators?size >= 1 || ipa.excludedOwners?size >= 1) || ipa.potentialOwners?size >= 1) && (ipa.taskStakeholders?size >= 1 || ipa.taskInitiator??)>,</#if>
  	<@addPerformerTypes ipa.taskStakeholders "org.jbpm.task:peopleassignments:stakeholder"/>
  	<#if (((ipa.businessAdministrators?size >= 1 || ipa.excludedOwners?size >= 1) || ipa.potentialOwners?size >= 1) || ipa.taskStakeholders?size >= 1) && ipa.taskInitiator??>,</#if>
  	<#if ipa.taskInitiator??><@addPerformerType ipa.taskInitiator "org.jbpm.task:initiator"/></#if>
  ],
  <#if event.task.taskData.actualOwner??> "owner": <@toOrgEntityReference event.task.taskData.actualOwner/>,</#if>
  <#if inputs["io.elimu.fhir:reasonCode"]??>
    "reasonCode": {
      "text": "${inputs["io.elimu.fhir:reasonCode"]}"
    },
  </#if>
  <#if inputs["io.elimu.fhir:reasonRef"]??>
  "reasonReference": {
  	"reference": "${inputs["io.elimu.fhir:reasonRef"]}"
  },
  </#if>
  "note": [
    {
      "text": "${(event.task.name)!}"
    }
  ],
  "restriction": {
    "repetitions": 1,
    "period": {
      <#if event.task.taskData.activationTime??>
        "start": "${event.task.taskData.activationTime?datetime?iso_local}",
        <#else>"start": null,
      </#if>
      <#if event.task.taskData.expirationTime??>
        "end": "${event.task.taskData.expirationTime?datetime?iso_local}"
        <#else>"end": null
      </#if>
    }
    <#if (ipa.recipients?? && ipa.recipients?size >= 1)>,
      "recipients": [
      	<#list ipa.recipients as rec><@toOrgEntityReference rec/><#sep>,</#sep></#list>
      ]
    </#if>
  },
  "input": [
  	{
      "type": {
  		"text": "org.jbpm.task:taskType"
  	  },
  	  "valueString": "${(event.task.taskType)!}"
  	},
  	{
      "type": {
  		"text": "org.jbpm.task:deploymentId"
  	  },
  	  "valueString": "${(event.task.taskData.deploymentId)!}"
  	},
  	{
      "type": {
  		"text": "org.jbpm.task:parentId"
  	  },
  	  "valueInteger": ${(event.task.taskData.parentId)!}
  	},
    {
      "type": {
  		"text": "org.jbpm.task:processId"
  	  },
  	  "valueString": "${(event.task.taskData.processId)!}"
  	},
    {
      "type": {
  		"text": "org.jbpm.task:processInstanceId"
  	  },
  	  "valueInteger": ${(event.task.taskData.processInstanceId)!}
  	},
    {
      "type": {
  		"text": "org.jbpm.task:processSessionId"
  	  },
  	  "valueInteger": ${(event.task.taskData.processSessionId)!}
  	},
    {
      "type": {
  		"text": "org.jbpm.task:workItemId"
  	  },
  	  "valueInteger": ${(event.task.taskData.workItemId)!}
  	}
  	<#if (otherKeys?size >= 1)>,</#if>
  	<#list inputs as key,value>
  		<#if !(internalKeys?seq_contains(key))>
    		{
  				"type": {
  					"text": "${key}"
  				},
  				<@asValueType value/>
    		}
  		</#if>
  	<#sep>,</#sep></#list>
  ],
  "output": [
  <#if event.task.taskData.taskOutputVariables??>
    <#list event.task.taskData.taskOutputVariables as key, value>
      {
        "type": {
          "text": "${key}"
        },
        <@asValueType value/>
      }
    <#sep>,</#sep></#list>
  </#if>],
  "text": "${(event.task.subject)!}",
  <#if inputs["io.elimu.fhir:insurance"]??>
  	"insurance": {
  		"reference": "${inputs["io.elimu.fhir:insurance"]}"
  	},
  </#if>
  <#if event.task.taskData.previousStatus??>
    "relevantHistory": [{
    	"display": <@toTaskStatus event.task.taskData.previousStatus/>
    }]
  </#if>
}
