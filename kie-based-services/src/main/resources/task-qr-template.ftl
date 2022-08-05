<#assign nowDateTime = .now>
<#if task??>
{
  "resourceType":"QuestionnaireResponse",
  "status":"completed",
  "authored":"${nowDateTime?iso_local}",
  <#if task?? && task.taskData?? && task.taskData.taskInputVariables?? && task.taskData.taskInputVariables['patient']?? >
    <#assign patId = task.taskData.taskInputVariables['patient']>
    "subject": {
      "reference": "Patient/${patId}"
    },
  </#if>
  "item":[
  <#if task?? && task.taskData?? && task.taskData.taskInputVariables??>
    <#list task.taskData.taskOutputVariables?keys as key>
      <#assign item = task.taskData.taskOutputVariables[key]>
      <#assign outtype = helper.getInputType(item)>
      { 
        "linkId": "${key}",
        <#if outtype == "integer" || outtype == "long">
          "answer":[{"valueInteger":${item?c}}]
        <#elseif outtype == "double" || outtype == "float">
          "answer":[{"valueDecimal":${item?c}}]
        <#elseif outtype == "date">
          "answer":[{"valueDateTime":"${item?time?iso_utc}"}]
        <#elseif outtype == "boolean">
          "answer":[{"valueBoolean": ${item?c}}]
        <#elseif outtype == "uri" || outtype == "url">
          "answer":[{"valueUri": "${item.toString()}"}]
        <#elseif outtype == "reference">
          "answer":[{"valueReference": {
            "reference": "${item.reference}"
          }}]
        <#elseif outtype?ends_with("quantity")>
          "answer":[{"valueQuantity": {
            "value": ${item.value?c},
            "unit": "${item.unit}"
          }}]
        <#else>
          "answer":[{"valueString":"${item}"}]
        </#if>
      }<#if key?has_next>,</#if>
    </#list>
  </#if>
  ]
}
</#if>
