/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.dataflow.module.deployer.kubernetes;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Florian Rosenberg
 */
@ConfigurationProperties("kubernetes")
class KubernetesModuleDeployerProperties {

	/**
	 * The docker image containing the Spring Cloud Stream module launcher. 
	 */
	private String moduleLauncherImage;
	
	/**
	 * Secrets for a private registry in case a different {@link #moduleLauncherImage} 
	 * is used.
	 */
	private String imagePullSecret;

	public String getModuleLauncherImage() {
		return moduleLauncherImage;
	}

	public void setModuleLauncherImage(String moduleLauncherImage) {
		this.moduleLauncherImage = moduleLauncherImage;
	}

	public String getImagePullSecret() {
		return imagePullSecret;
	}

	public void setImagePullSecret(String imagePullSecret) {
		this.imagePullSecret = imagePullSecret;
	}

}
