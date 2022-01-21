package io.elimu.genericapi.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.events.TaskEventSupport;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.kie.api.runtime.Context;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.ContentMarshallerContext;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.TaskPersistenceContext;
import org.kie.internal.task.api.model.ContentData;
import org.kie.internal.task.api.model.InternalContent;
import org.kie.internal.task.api.model.InternalPeopleAssignments;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.internal.task.api.model.InternalTaskData;

public class InternalUpdateTaskCommand extends TaskCommand<Void> {

	private static final long serialVersionUID = -150222852314659273L;
	private final Task task;

	public InternalUpdateTaskCommand(Task task) {
		this.task = task;
		this.taskId = task.getId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Void execute(Context cntxt) {
		TaskContext context = (TaskContext) cntxt;
		TaskEventSupport taskEventSupport = context.getTaskEventSupport();
		TaskPersistenceContext persistenceContext = context.getPersistenceContext();
		InternalTask dbTask = (InternalTask) persistenceContext.findTask(taskId);
		// security check
		taskEventSupport.fireBeforeTaskUpdated(dbTask, context);
		// process task data
		dbTask.setDescription(task.getDescription());
		dbTask.setFormName(task.getFormName());
		dbTask.setName(task.getName());
		dbTask.setPriority(task.getPriority());
		dbTask.setSubject(task.getSubject());
		InternalPeopleAssignments ipa = (InternalPeopleAssignments) task.getPeopleAssignments();
		Set<OrganizationalEntity> entities = new HashSet<>();
		if (ipa != null) {
			InternalPeopleAssignments dbIpa = (InternalPeopleAssignments) dbTask.getPeopleAssignments();
			setOrganizationalEntities(ipa, dbIpa, entities);
		}
		if (task.getTaskData().getActualOwner() != null) {
			entities.add(task.getTaskData().getActualOwner());
		}
		if (task.getTaskData().getCreatedBy() != null) {
			entities.add(task.getTaskData().getCreatedBy());
		}
		for (OrganizationalEntity entity : entities) {
			OrganizationalEntity dbEntity = persistenceContext.findOrgEntity(entity.getId());
			if (dbEntity == null) {
				persistenceContext.persist(entity);
			}
		}
		InternalTaskData dbData = (InternalTaskData) dbTask.getTaskData();
		dbData.setActivationTime(task.getTaskData().getActivationTime());
		dbData.setActualOwner(task.getTaskData().getActualOwner());
		dbData.setCreatedBy(task.getTaskData().getCreatedBy());
		dbData.setCreatedOn(task.getTaskData().getCreatedOn());
		dbData.setExpirationTime(task.getTaskData().getExpirationTime());

		// process status logic
		Status dbStatus = dbData.getStatus();
		Status newStatus = task.getTaskData().getStatus();
		if (dbStatus != newStatus) {
			dbData.setPreviousStatus(dbData.getStatus());
			dbData.setStatus(newStatus);
		}

		// process task inputs
		long inputContentId = task.getTaskData().getDocumentContentId();
		Content inputContent = persistenceContext.findContent(inputContentId);

		Map<String, Object> mergedContent = task.getTaskData().getTaskInputVariables();

		if (task.getTaskData().getTaskInputVariables() != null) {
			if (inputContent == null) {
				ContentMarshallerContext mcontext = context.getTaskContentService().getMarshallerContext(dbTask);
				ContentData outputContentData = ContentMarshallerHelper.marshal(dbTask,
						task.getTaskData().getTaskInputVariables(), mcontext.getEnvironment());
				Content content = TaskModelProvider.getFactory().newContent();
				((InternalContent) content).setContent(outputContentData.getContent());
				persistenceContext.persistContent(content);

				dbData.setOutput(content.getId(), outputContentData);
			} else {
				ContentMarshallerContext mcontext = context.getTaskContentService().getMarshallerContext(dbTask);
				Object unmarshalledObject = ContentMarshallerHelper.unmarshall(inputContent.getContent(),
						mcontext.getEnvironment(), mcontext.getClassloader());
				if (unmarshalledObject instanceof Map) {
					((Map<String, Object>) unmarshalledObject).putAll(task.getTaskData().getTaskInputVariables());
					mergedContent = ((Map<String, Object>) unmarshalledObject);
				}
				ContentData outputContentData = ContentMarshallerHelper.marshal(dbTask, unmarshalledObject,
						mcontext.getEnvironment());
				((InternalContent) inputContent).setContent(outputContentData.getContent());
				persistenceContext.persistContent(inputContent);
			}
			dbData.setTaskInputVariables(mergedContent);
		}

		if (task.getTaskData().getTaskOutputVariables() != null) {
			// process task outputs
			context.getTaskContentService().addOutputContent(taskId, task.getTaskData().getTaskOutputVariables());
		}

		persistenceContext.updateTask(dbTask);
		// finally trigger event support after the updates
		taskEventSupport.fireAfterTaskUpdated(dbTask, context);

		return null;
	}
	
	private void setOrganizationalEntities(InternalPeopleAssignments ipa, InternalPeopleAssignments dbIpa,
			Set<OrganizationalEntity> entities) {
		fillEntities(CollectionUtils.emptyIfNull(ipa.getBusinessAdministrators()), entities);
		dbIpa.setBusinessAdministrators(getOrganizationalEntities(ipa.getBusinessAdministrators()));

		fillEntities(CollectionUtils.emptyIfNull(ipa.getExcludedOwners()), entities);
		dbIpa.setExcludedOwners(getOrganizationalEntities(ipa.getExcludedOwners()));

		fillEntities(CollectionUtils.emptyIfNull(ipa.getPotentialOwners()), entities);
		dbIpa.setPotentialOwners(getOrganizationalEntities(ipa.getPotentialOwners()));

		fillEntities(CollectionUtils.emptyIfNull(ipa.getRecipients()), entities);
		dbIpa.setRecipients(getOrganizationalEntities(ipa.getRecipients()));

		if (ipa.getTaskInitiator() != null) {
			entities.add(ipa.getTaskInitiator());
		}
		dbIpa.setTaskInitiator(ipa.getTaskInitiator());

		fillEntities(CollectionUtils.emptyIfNull(ipa.getTaskStakeholders()), entities);
		dbIpa.setTaskStakeholders(getOrganizationalEntities(ipa.getTaskStakeholders()));
	}
	
	private void fillEntities(Collection<OrganizationalEntity> items, Set<OrganizationalEntity> entities) {
		for(OrganizationalEntity entity: items) {
			entities.add(entity);
		}
	}
	
	private List<OrganizationalEntity> getOrganizationalEntities(List<OrganizationalEntity> item) {
		return (List<OrganizationalEntity>) CollectionUtils.emptyIfNull(item);
	}

}
