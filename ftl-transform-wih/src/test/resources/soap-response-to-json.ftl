<#ftl
  ns_prefixes={
    "s":"http://schemas.xmlsoap.org/soap/envelope/",
    "D": "urn:my.example:schema"
  }
>
<#outputformat "JSON">
{
  "resourceType": "Encounter",
  "patient": {
    "reference": "Patient/${xml_param_encounter["s:Envelope"]["s:Body"].Patient.Id}" 
  }
}
</#outputformat>
