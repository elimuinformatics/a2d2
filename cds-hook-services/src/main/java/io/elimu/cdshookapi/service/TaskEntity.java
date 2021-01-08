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

package io.elimu.cdshookapi.service;

import java.util.Map;

public class TaskEntity {

	private long taskId;
	private String taskName;
	private long processInstanceId;
	private String processId;
	private Map<String, Object> taskInputs;
	private Map<String, Object> taskOutputs;

	public long getTaskId() {
		return taskId;
	}
	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public long getProcessInstanceId() {
		return processInstanceId;
	}
	public void setProcessInstanceId(long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	public String getProcessId() {
		return processId;
	}
	public void setProcessId(String processId) {
		this.processId = processId;
	}
	public Map<String, Object> getTaskInputs() {
		return taskInputs;
	}
	public void setTaskInputs(Map<String, Object> taskInputs) {
		this.taskInputs = taskInputs;
	}
	public Map<String, Object> getTaskOutputs() {
		return taskOutputs;
	}
	public void setTaskOutputs(Map<String, Object> taskOutputs) {
		this.taskOutputs = taskOutputs;
	}

}
