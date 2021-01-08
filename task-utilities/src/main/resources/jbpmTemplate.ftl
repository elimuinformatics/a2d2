<#setting number_format="computer">
<#assign internalKeys = ["io.elimu.fhir:taskid", "io.elimu.fhir:insurance", "io.elimu.fhir:intent", "io.elimu.fhir:encounter", "io.elimu.fhir:reasonCode", "io.elimu.fhir:reasonRef", "io.elimu.fhir:focus", "io.elimu.fhir:for"]>
<#assign originalInputs = originalTask.taskData.taskInputVariables>
<#assign fhirInputs = fhirTask.input>
<#assign originalOutputs = originalTask.taskData.taskOutputVariables>
<#assign fhirOutputs = fhirTask.output>
<#macro toDateTime dt>${dt?datetime?iso_local}</#macro>
<#macro asValue v><#compress>
  <#if v?? && v.value?? && v.value.value??>
    <#if v.value.value?is_date>
      <value xsi:type="xs:datetime" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">${v.value?datetime?iso_local}</value>
    <#elseif v.value.value?is_boolean>
      <value xsi:type="xs:boolean" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">${v.value?c}</value>
    <#elseif v.value.value?is_number && v.value.value?floor != v.value.value>
      <value xsi:type="xs:decimal" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">${v.value.value?c}</value>
    <#elseif v.value.value?is_number && v.value.value?floor == v.value.value>
      <value xsi:type="xs:integer" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">${v.value.value?c}</value>
    <#elseif v.value.value?is_string>
      <value xsi:type="xs:string" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">${v.value.value}</value>
    <#else>
      <value xsi:type="xs:string" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">${v.value.value?c}</value>
    </#if>
  </#if>
</#compress></#macro>
<#macro toOrgEntityFromCode cod><#compress>
  <#if cod??>
    <id>${(cod.display)!}</id>
    <type><#if cod.code?? && cod.code?starts_with("Organization")>GROUP<#else>USER</#if></type>
  </#if>
</#compress></#macro>
<#macro toOrgEntityFromRef ref><#compress>
  <#if ref??>
    <#if ref.display??><id>${(ref.display)!}</id><#elseif ref.value??><id>${(ref.value)!}</id></#if>
    <type><#if ref.reference?? && ref.reference?starts_with("Organization")>GROUP<#else>USER</#if></type>
  </#if>
</#compress></#macro>
<#macro fromTaskStatus status><#compress>
  <#if status?? && status.display??>
    <#if status.display == "Completed">Completed
    <#elseif status.display == "Draft">Created
    <#elseif status.display == "Entered In Error">Error
    <#elseif status.display == "Failed">Failed
    <#elseif status.display == "In Progress">InProgress
    <#elseif status.display == "Cancelled">Exited
    <#elseif status.display == "Requested">Ready
    <#elseif status.display == "Accepted">Reserved
    <#elseif status.display == "On Hold">Suspended
    <#else>null
    </#if>
  </#if>
</#compress></#macro>
<#macro fromTaskPriority priority><#compress>
  <#if priority??>
    <#if priority.display == "Stat">1
    <#elseif priority.display == "ASAP">2
    <#elseif priority.display == "Urgent">3
    <#else>4
    </#if>
  <#else>0
  </#if>
</#compress></#macro>
<task-wrapper>
  <task>
    <#list fhirTask.identifier as identifier>
       <#if identifier.use.name() == "SECONDARY"><id>${identifier.value}</id></#if>
    </#list>
    <description>${(fhirTask.description)!}</description>
    <name>${(fhirTask.noteFirstRep.text)!}</name>
    <form-name>${(fhirTask.code.text)!}</form-name>
    <people-assignments>
      <#list fhirTask.performerType as pt>
       <#if pt.codingFirstRep.system == "org.jbpm.task:initiator">
          <task-initiator-id><@toOrgEntityFromCode pt.codingFirstRep/></task-initiator-id>
       <#elseif pt.codingFirstRep.system == "org.jbpm.task:peopleassignments:stakeholder">
          <task-stakeholders><@toOrgEntityFromCode pt.codingFirstRep/></task-stakeholders>
       <#elseif pt.codingFirstRep.system == "org.jbpm.task:peopleassignments:businessadministrator">
	      <business-administrators><@toOrgEntityFromCode pt.codingFirstRep/></business-administrators>
       <#elseif pt.codingFirstRep.system == "org.jbpm.task:peopleassignments:potentialowner">
          <potential-owners><@toOrgEntityFromCode pt.codingFirstRep/></potential-owners>
       <#elseif pt.codingFirstRep.system == "org.jbpm.task:peopleassignments:excludedowner">
          <excluded-owners><@toOrgEntityFromCode pt.codingFirstRep/></excluded-owners>
       </#if> 
      </#list>
      <#if fhirTask.groupIdentifier??>
        <potential-owners><@toOrgEntityFromRef fhirTask.groupIdentifier/></potential-owners>
      </#if>
      <#if fhirTask.recipient??>
        <#list fhirTask.recipient as rec><recipients><@toOrgEntityFromRef rec/></recipients></#list>
      </#if>
    </people-assignments>
    <priority><@fromTaskPriority fhirTask.priority/></priority>
    <#if fhirTask.text??>
      <subject>${(fhirTask.text.divAsString)!}</subject>
    <#else>
      <subject></subject>
    </#if>
    <taskData>
      <status><@fromTaskStatus fhirTask.status/></status>
      <previous-status><@fromTaskStatus fhirTask.relevantHistoryFirstRep/></previous-status>
      <#if fhirTask.expirationPeriod??>
        <activation-time><@toDateTime fhirTask.expirationPeriod.start/></activation-time>
        <expiration-time><@toDateTime fhirTask.expirationPeriod.end/></expiration-time>
      </#if>
      <actual-owner><@toOrgEntityFromRef fhirTask.owner/></actual-owner> 
      <created-by><@toOrgEntityFromRef fhirTask.requester/></created-by> 
      <#if fhirTask.authoredOn??>
        <created-on><@toDateTime fhirTask.authoredOn/></created-on>
      </#if>
      <skipable>${originalTask.taskData.skipable?c}</skipable>
      <work-item-id>${originalTask.taskData.workItemId}</work-item-id>
      <process-instance-id>${originalTask.taskData.processInstanceId}</process-instance-id>
      <parent-id>${originalTask.taskData.parentId}</parent-id>
      <process-id>${(originalTask.taskData.processId)!}</process-id>
      <process-session-id>${originalTask.taskData.processSessionId}</process-session-id>
      <deployment-id>${(originalTask.taskData.deploymentId)!}</deployment-id>
    </taskData>
  </task>
  <inputs>
    <item key="io.elimu.fhir:reasonCode"><value xsi:type="xs:string" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">fhirTask.reasonCode.text)!}</value></item>
    <item key="io.elimu.fhir:intent"><value xsi:type="xs:string" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">${(fhirTask.intent.display)!}</value></item>
    <item key="io.elimu.fhir:taskid"><value xsi:type="xs:string" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">${(fhirTask.id)!}</value></item>
    <item key="io.elimu.fhir:focus"><value xsi:type="xs:string" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">${(fhirTask.focus.reference)!}</value></item>
    <item key="io.elimu.fhir:for"><value xsi:type="xs:string" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">${(fhirTask.for.reference)!}</value></item>
    <item key="io.elimu.fhir:encounter"><value xsi:type="xs:string" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">${(fhirTask.encounter.display)!}</value></item>
    <item key="io.elimu.fhir:reasonRef"><value xsi:type="xs:string" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">${(fhirTask.reasonReference.reference)!}</value></item>
    <item key="io.elimu.fhir:insurance"><value xsi:type="xs:string" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">${(fhirTask.insurance[0].reference)!}</value></item>
    <#list fhirInputs as inp>
      <#if !(internalKeys?seq_contains(inp.type.text))>
        <item key="${inp.type.text}"><@asValue inp/></item>
    </#if>
  </#list>
  </inputs>
  <outputs>
    <#list fhirOutputs as out>
      <item key="${out.type.text}"><@asValue out/></item>
    </#list>
  </outputs>
</task-wrapper>
