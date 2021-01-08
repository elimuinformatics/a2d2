#process that needs to start
config.client.version=111
kie.project.processId=example
kie.project.logexec=true
#I want to populate the user in the ServiceRequest
kie.project.authtype=BASIC
#But anyone can run this service
kie.project.validationtype=NONE
services.questionnaire.workItemHandlers.Questioonnaire=new io.elimu.a2d2-1234.questionnaire.QuestionnaireDelegate()

