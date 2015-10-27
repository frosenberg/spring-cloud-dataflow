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
	private String moduleLauncherImage = DEFAULT_IMAGE_NAME;

	/**
	 * Secrets for a access a private registry to pull images.
	 */
	private String imagePullSecret;

	/**
	 * Delay in seconds when the Kubernetes liveness check of the stream module container
	 * should start checking its health status.
	 */
	private int livenessProbeDelay = 180;

	/**
	 * Timeout in seconds for the Kubernetes liveness check of the stream module container.
	 * If the health check takes longer than this value to return it is assumed as 'unavailable'.
	 */
	private int livenessProbeTimeout = 2;

	/**
	 * Delay in seconds when the readiness check of the stream module container
	 * should start checking if the module is fully up and running.
	 */
	private int readinessProbeDelay = 10;

	/**
	 * Timeout in seconds that the stream module container has to respond its
	 * health status during the readiness check.
	 */
	private int readinessProbeTimeout = 2;

	/**
	 * Memory to allocate for a Pod.
	 */
	private String memory = "512Mi";

	/**
	 * CPU to allocate for a Pod (quarter of a CPU).
	 */
	private String cpu = "250m";

	/**
	 * A name of a Docker registry repository (e.g., <code>springcloud</code>). This only
	 * applies if {@code #usePreBakedImagePerModule} is set to <code>true</code>.
	 */
	private String imageRepository = "";

	/**
	 * A name prefix for a pre-baked module image (e.g.,
	 * springcloud/spring-cloud-stream-module-log-sink). This only applies if
	 * {@code #usePreBakedImagePerModule} is set to <code>true</code>.
	 */
	private String imageNamePrefix = "spring-cloud-stream-module";

	/**
	 * Set to true In case of resolving JARs for each module, use a pre-baked images that
	 * contain the modules. Setting this flag will ignore the {@code #moduleLauncherImage}
	 * field.
	 */
	private boolean usePreBakedImagePerModule;

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

	public String getMemory() {
		return memory;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	public String getImageRepository() {
		return imageRepository;
	}

	public void setImageRepository(String imageRepository) {
		this.imageRepository = imageRepository;
	}

	public String getImageNamePrefix() {
		return imageNamePrefix;
	}

	public void setImageNamePrefix(String imageNamePrefix) {
		this.imageNamePrefix = imageNamePrefix;
	}

	public boolean isUsePreBakedImagePerModule() {
		return usePreBakedImagePerModule;
	}

	public void setUsePreBakedImagePerModule(boolean usePreBakedImagePerModule) {
		this.usePreBakedImagePerModule = usePreBakedImagePerModule;
	}

}
