/*
 * The MIT License
 *
 * Copyright (c) 2011-2012, Manufacture Francaise des Pneumatiques Michelin, Daniel Petisme
 * Copyright 2019 Lexmark
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.michelin.cio.jenkins.plugin.requests.model;

import hudson.model.Item;
//import hudson.model.Run;
import hudson.model.Job;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

// Represents a deletion request sent by a user to Jenkins' administrator.
// @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>

public class DeleteJobRequest extends Request {

	private static final Logger LOGGER = Logger.getLogger(DeleteJobRequest.class.getName());

	public DeleteJobRequest(String requestType, String username, String project, String projectFullName, String buildNumber) {
		super(requestType, username, project, projectFullName, buildNumber);
	}

	@Override
	public String getMessage() {
		return Messages.DeleteJobRequest_message(project);
	}

	public boolean execute_backup(Item item) {
		boolean success = false;
		
		try {
			if (Jenkins.get().hasPermission(Item.DELETE)) {
				try {
					item.delete();
					success = true;
					errorMessage = "The Job " + item.getFullName()
							+ " has been properly Deleted";
					LOGGER.log(Level.INFO,
							"The job {0} has been properly deleted",
							item.getFullName());
				} catch (IOException e) {
					errorMessage = e.getMessage();
					LOGGER.log(Level.SEVERE,
							"Unable to delete the job " + item.getFullName(),
							e);
				} catch (InterruptedException e) {
					errorMessage = e.getMessage();
					LOGGER.log(Level.SEVERE,
							"Unable to delete the job " + item.getFullName(),
							e);
				}
			} else {
				errorMessage = "The current user " + username
						+ " does not have permission to delete the job";
				LOGGER.log(Level.FINE,
						"The current user {0} does not have permission to DELETE the job",
						new Object[] { username });
			}

		} catch (NullPointerException e) {
			errorMessage = e.getMessage();
			LOGGER.log(Level.SEVERE, "Unable to Delete the job "
					+ projectFullName + ":" + buildNumber, e.getMessage());

			return false;
		}

		return success;
	}
	
	public boolean execute(Item item) {
		Jenkins jenkins = null;
		boolean success = false;
		String returnStatus;
		StringBuffer stringBuffer = new StringBuffer();
		String[] projectList = null;

		try {
			jenkins = Jenkins.get();
			if (jenkins == null)
				throw new NullPointerException("Jenkins instance is null");

			if (Jenkins.get().hasPermission(Job.DELETE)) {
				String jenkinsURL = null;
				jenkinsURL = Jenkins.get().getRootUrl();
				if (jenkinsURL == null)
					throw new NullPointerException("Jenkins instance is null");
				
				if (!projectFullName.contains("/job/") && projectFullName.contains("/")) {
					projectList = projectFullName.split("/");
					
					// Need to add '/job/' in between all names:
					int nameCount = projectList.length;
					stringBuffer.append(projectList[0]);
					for (int i = 1; i < nameCount; i++) {
						stringBuffer.append("/job/");
						stringBuffer.append(projectList[i]);
					}
					projectFullName = stringBuffer.toString();
					//LOGGER.info("[INFO] FOLDER Found: " + projectFullName);
				}
				
				String urlString = jenkinsURL + "job/" + projectFullName + "/doDelete";
				RequestsUtility requestsUtility = new RequestsUtility();
				
				//LOGGER.info("[INFO] Delete Build urlString: " + urlString);

				try {
					returnStatus = requestsUtility.runPostMethod(jenkinsURL, urlString);
					
				} catch (IOException e) {
					errorMessage = e.getMessage();
					LOGGER.log(
							Level.SEVERE, "Unable to Delete the build "
									+ projectFullName + ":" + buildNumber,
							e.getMessage());

					return false;
				}

				if (returnStatus.equals("success")) {
					errorMessage = "Job : " + projectFullName + " has been properly Deleted";
					LOGGER.log(Level.INFO,
							"Job {0} has been properly Deleted", projectFullName);
					success = true;

				} else {
					errorMessage = "Delete Job call has failed for " + projectFullName + " : " + returnStatus;
					LOGGER.log(Level.INFO, "Delete Job call has failed: ", projectFullName + " : " + returnStatus);
				}

			} else {
				errorMessage = "The current user " + username + " does not have permission to delete the Job";
				LOGGER.log(Level.FINE, "The current user {0} does not have permission to DELETE the Job", new Object[] { username });
				LOGGER.log(Level.FINE, "The current user does not have the DELETE permission");
			}
		} catch (NullPointerException e) {
			errorMessage = e.getMessage();
			LOGGER.log(Level.SEVERE, "Unable to Delete the Job " + projectFullName, e.getMessage());

			return false;
		}

		return success;
	}

}
