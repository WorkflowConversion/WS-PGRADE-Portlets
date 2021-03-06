package com.workflowconversion.portlet.core.execution.impl;

import java.util.Map;

import com.workflowconversion.portlet.core.execution.JobExecutionPropertiesHandler;
import com.workflowconversion.portlet.core.workflow.Job;

/**
 * Handles jobs that are locally executed on the WS-PGRADE instance.
 * 
 * @author delagarza
 *
 */
class LocalJobExecutionPropertiesHandler implements JobExecutionPropertiesHandler {

	private final String GRID_TYPE_LOCAL = "local";

	@Override
	public boolean canHandle(final Job job) {
		return GRID_TYPE_LOCAL.equalsIgnoreCase(job.getResourceType());
	}

	@Override
	public void handle(final Job job, final Map<String, String> jobExecutionProperties) {
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_GRID_TYPE, GRID_TYPE_LOCAL);
		jobExecutionProperties.put(JOB_EXECUTION_COMMAND_LINE,
				job.getApplication().getPath() + ' ' + job.getParameters());
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_JOB_TYPE, "binary");
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_GRID, job.getResourceName());
	}

}
