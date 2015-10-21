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
	 * Use the default spring image. Override with --kubernetes.moduleLauncherImage
	 * as a Spring Admin parameter to use a different one.
	 */
	private static final String DEFAULT_IMAGE_NAME = "springcloud/stream-module-launcher";

	/**
	 * The Docker image containing the Spring Cloud Stream module launcher.
	 */
	private String moduleLauncherImage;

	/**
	 * Secrets for a access a private registry to pull images.
	 */
	private String imagePullSecret;

	/**
	 * A name of a Docker registry repository (e.g., <code>springcloud</code>). This only
	 * applies if {@code #usePreBakedImagePerModule} is set to
	 * <code>true</code>.
	 */
	private String imageRepository = "";

	/**
	 * A name prefix for a pre-baked module image (e.g., springcloud/spring-cloud-stream-module-log-sink).
	 * This only applies if {@code #usePreBakedImagePerModule} is set to <code>true</code>.
	 */
	private String imageNamePrefix = "spring-cloud-stream-module";

	/**
	 * Set to true In case of resolving JARs for each module, use a pre-baked images that contain
	 * the modules. Setting this flag will ignore the {@code #moduleLauncherImage} field.
	 */
	private boolean usePreBakedImagePerModule;

	/**
	 * Delay in seconds when the Kubernetes liveness check of the stream module container 
	 * should start checking its health status.
	 * 
	 * See here for more information: 
	 * {@link http://kubernetes.io/v1.0/docs/user-guide/production-pods.html#liveness-and-readiness-probes-aka-health-checks}
	 */
	private int livenessProbeDelay = 180;
	
	/**
	 * Timeout in seconds for the Kubernetes liveness check of the stream module container. 
	 * If the health check takes longer than this value to return it is assumed as 'unavailable'.
	 * 
	 * See here for more information: 
	 * {@link http://kubernetes.io/v1.0/docs/user-guide/production-pods.html#liveness-and-readiness-probes-aka-health-checks}
	 */
	private int livenessProbeTimeout = 2;
	
	/**
	 * Delay in seconds when the readiness check of the stream module container 
	 * should start checking if the module is fully up and running.
	 * 
	 * See here for more information: 
	 * {@link http://kubernetes.io/v1.0/docs/user-guide/production-pods.html#liveness-and-readiness-probes-aka-health-checks}
	 */
	private int readinessProbeDelay = 10;
	
	/**
	 * Timeout in seconds that the stream module container has to respond its
	 * health status during the readiness check. 
	 * 
	 * See here for more information: 
	 * {@link http://kubernetes.io/v1.0/docs/user-guide/production-pods.html#liveness-and-readiness-probes-aka-health-checks}
	 */
	private int readinessProbeTimeout = 2;
	

	public String getModuleLauncherImage() {
		return moduleLauncherImage == null ? DEFAULT_IMAGE_NAME : moduleLauncherImage;
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

	public boolean usePreBakedImagePerModule() {
		return usePreBakedImagePerModule;
	}

	public void setUsePreBakedImagePerModule(boolean usePreBakedImagePerModule) {
		this.usePreBakedImagePerModule = usePreBakedImagePerModule;
	}

	public int getReadinessProbeTimeout() {
		return readinessProbeTimeout;
	}

	public void setReadinessProbeTimeout(int readinessProbeTimeout) {
		this.readinessProbeTimeout = readinessProbeTimeout;
	}

	public int getReadinessProbeDelay() {
		return readinessProbeDelay;
	}

	public void setReadinessProbeDelay(int readinessProbeDelay) {
		this.readinessProbeDelay = readinessProbeDelay;
	}

	public int getLivenessProbeTimeout() {
		return livenessProbeTimeout;
	}

	public void setLivenessProbeTimeout(int livenessProbeTimeout) {
		this.livenessProbeTimeout = livenessProbeTimeout;
	}

	public int getLivenessProbeDelay() {
		return livenessProbeDelay;
	}

	public void setLivenessProbeDelay(int livenessProbeDelay) {
		this.livenessProbeDelay = livenessProbeDelay;
	}

	public String getImageNamePrefix() {
		return imageNamePrefix;
	}

	public void setImageNamePrefix(String imageNamePrefix) {
		this.imageNamePrefix = imageNamePrefix;
	}

	public String getImageRepository() {
		return imageRepository;
	}

	public void setImageRepository(String imageRepository) {
		this.imageRepository = imageRepository;
	}

}