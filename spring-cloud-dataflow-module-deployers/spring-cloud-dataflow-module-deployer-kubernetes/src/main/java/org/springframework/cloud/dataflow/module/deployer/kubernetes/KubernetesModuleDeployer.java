package org.springframework.cloud.dataflow.module.deployer.kubernetes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.dataflow.core.ModuleDeploymentId;
import org.springframework.cloud.dataflow.core.ModuleDeploymentRequest;
import org.springframework.cloud.dataflow.module.ModuleStatus;
import org.springframework.cloud.dataflow.module.deployer.ModuleArgumentQualifier;
import org.springframework.cloud.dataflow.module.deployer.ModuleDeployer;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;

/**
 * Implements a deployer for the Google Kubernetes project. 
 * 
 * @author Florian Rosenberg
 */
public class KubernetesModuleDeployer implements ModuleDeployer {

	protected static final String SCSM_GROUP_KEY = "scsm-group";
	protected static final String SCSM_LABEL_KEY = "scsm-label";
	private static final String SCSM_EXTENSION = "scsm-extension";
	private static final String SCSM_VERSION = "scsm-version";
	private static final String SCSM_GROUP_ID = "scsm-groupId";
	private static final String SCSM_ARTIFACT_ID = "scsm-artifactId";
	private static final String SPRING_MARKER_VALUE = "scsm-module";
	private static final String SPRING_MARKER_KEY = "role";
	private static final String PORT_KEY = "port";
	private static final String SERVER_PORT_KEY = "server.port";
	private static final String HEALTH_ENDPOINT = "/health";
	private static final String SPRING_REDIS_HOST = "SPRING_REDIS_HOST";
	private static final String CONTAINER_NAME = "spring-module-launcher";
	
	/** 
	 * Use the default spring image. Override with --kubernetes.moduleLauncherImage 
	 * as a Spring Admin parameter to use a different one.
	 */
	private static final String DEFAULT_IMAGE_NAME = "springcloud/stream-module-launcher";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private KubernetesClient kubeClient;
	
	private KubernetesModuleDeployerProperties properties;

	public KubernetesModuleDeployer(
			KubernetesModuleDeployerProperties properties) {
		this.properties = properties;
	}

	@Override
	public ModuleDeploymentId deploy(ModuleDeploymentRequest request) {			
		ModuleDeploymentId id = ModuleDeploymentId.fromModuleDefinition(request.getDefinition()); 
		
		logger.debug("Deploying module: {}", createKubernetesName(id));
		
		Integer externalPort = 8080;
		// we also create a service in case have an '--port' or '--server.port' argument (source or sink)
		Map<String, String> parameters = request.getDefinition().getParameters();
		if (parameters.containsKey(PORT_KEY)) {
			externalPort = Integer.valueOf(parameters.get(PORT_KEY));
			createService(id, request, externalPort);
		} else if (parameters.containsKey(SERVER_PORT_KEY)) {
			externalPort = Integer.valueOf(parameters.get(SERVER_PORT_KEY));		
			createService(id, request, externalPort);
		}		
		createReplicationController(id, request, externalPort);				
		return id;	
	}

	@Override
	public void undeploy(ModuleDeploymentId id) {
		String name = createKubernetesName(id);
		logger.debug("Undeploying module: {}", name);

		try {
			kubeClient.replicationControllers().withName(name).delete();		
			kubeClient.services().withName(name).delete();
		} catch (KubernetesClientException e) {
			logger.error(e.getMessage(), e);
			// FIXME what do we throw??
		}
	}

	@Override
	public ModuleStatus status(ModuleDeploymentId id) {
		String name = createKubernetesName(id);
		logger.debug("Querying module status: {}", name);

		try {
			// The only really interesting status is coming from the containers and pods.
			// The service and the RC don't have "realtime" status info.
			PodList list = kubeClient.pods().withLabels(createIdMap(id)).list();
			return buildModuleStatus(id, list);
		}
		catch (KubernetesClientException e) {
			logger.warn(e.getMessage(), e);
			return buildModuleStatus(id, null);
		}
	}
	
	@Override
	public Map<ModuleDeploymentId, ModuleStatus> status() {
		Map<ModuleDeploymentId, ModuleStatus> result = new HashMap<>();
		
		ReplicationControllerList list = 
				kubeClient.replicationControllers()
				.withLabel(SPRING_MARKER_KEY, SPRING_MARKER_VALUE).list();
		
		for (ReplicationController rc : list.getItems()) {
			Map<String, String> labels = rc.getMetadata().getLabels();			
			String group = labels.get(SCSM_GROUP_KEY);
			String label = labels.get(SCSM_LABEL_KEY);			
			ModuleDeploymentId id = new ModuleDeploymentId(group, label);
			
			PodList pods = kubeClient.pods().withLabels(labels).list();			
			result.put(id, buildModuleStatus(id, pods));	
		}		
		return result;		
	}

	private ReplicationController createReplicationController(ModuleDeploymentId id, ModuleDeploymentRequest request, Integer externalPort) {
        ReplicationController rc = new ReplicationControllerBuilder()
                .withNewMetadata()
                	.withName(createKubernetesName(id))  // does not allow . in the name
                	.withLabels(createIdMap(id))
                	.addToLabels(SPRING_MARKER_KEY, SPRING_MARKER_VALUE)
            		.addToLabels(SCSM_ARTIFACT_ID, request.getCoordinates().getArtifactId())
            		.addToLabels(SCSM_GROUP_ID, request.getCoordinates().getGroupId())
            		.addToLabels(SCSM_VERSION, request.getCoordinates().getVersion())
            		.addToLabels(SCSM_EXTENSION, request.getCoordinates().getExtension())
                .endMetadata()
                .withNewSpec()
                	.withReplicas(request.getCount())
                	.withSelector(createIdMap(id))
                .withNewTemplate()
                	.withNewMetadata()
                		.withLabels(createIdMap(id))
                    	.addToLabels(SPRING_MARKER_KEY, SPRING_MARKER_VALUE)
                	.endMetadata()
	                .withSpec(createPodSpec(request, externalPort))	                	
                .endTemplate()
                .endSpec().build();        
       
        return kubeClient.replicationControllers().create(rc);        
	}

	private PodSpec createPodSpec(ModuleDeploymentRequest request, Integer port) {
		ContainerBuilder container = new ContainerBuilder();
	
		// Add default image or override with customer image
		if (properties.getModuleLauncherImage() == null) {
			container.withImage(DEFAULT_IMAGE_NAME);			
		} else {
			container.withImage(properties.getModuleLauncherImage());
		}		
		container.withName(CONTAINER_NAME)
	     	.withEnv(createModuleLauncherEnvArgs(request))
	     	.withArgs(createCommandArgs(request))
	     	.addNewPort()
	     		.withContainerPort(port)
	     	.endPort()
	     	.withReadinessProbe(createProbe(port, 1, 10));
		 	//TODO figure out why this is restarting the container .withLivenessProbe(createProbe(externalPort, 2, 120))
		
		PodSpecBuilder podSpec = new PodSpecBuilder();
		
		// Add image secrets if set
		if (properties.getImagePullSecret() != null) {
			podSpec.addNewImagePullSecret(properties.getImagePullSecret());
		}
		
		podSpec.addToContainers(container.build());
		return podSpec.build();
	}

	private void createService(ModuleDeploymentId id, ModuleDeploymentRequest request, Integer externalPort) {		
		kubeClient.services().inNamespace(kubeClient.getNamespace()).createNew()
				.withNewMetadata()
                  .withName(createKubernetesName(id)) // does not allow . in the name
                  .withLabels(createIdMap(id))
                  .addToLabels(SPRING_MARKER_KEY, SPRING_MARKER_VALUE)
                .endMetadata()
                .withNewSpec()
                	.withSelector(createIdMap(id))
                	.addNewPort()                	
                		.withPort(externalPort)
                	.endPort()
                .endSpec()
                .done();			
	}

	// does not allow . in the name
	protected String createKubernetesName(ModuleDeploymentId id) {
		return id.toString().replace('.', '-');
	}

	
	/**
	 * Creates a map of labels for a given ID. This will allow Kubernetes services 
	 * to "select" the right ReplicationControllers.
	 */
	private Map<String, String> createIdMap(ModuleDeploymentId id) {
		Map<String, String> map = new HashMap<>();
		map.put(SCSM_GROUP_KEY, id.getGroup());
		map.put(SCSM_LABEL_KEY, id.getLabel());
		return map;
	}

	private static String bashEscape(String original) {
		// Adapted from http://ruby-doc.org/stdlib-1.9.3/libdoc/shellwords/rdoc/Shellwords.html#method-c-shellescape
		return original.replaceAll("([^A-Za-z0-9_\\-.,:\\/@\\n])", "\\\\$1").replaceAll("\n", "'\\\\n'");
	}
	
	/**
	 * Create a readiness probe for the /health endpoint exposed by each module.
	 */
	private Probe createProbe(Integer externalPort, long timeout, long initialDelay) {
		return new ProbeBuilder()
				.withHttpGet(
						new HTTPGetActionBuilder()
								.withPath(HEALTH_ENDPOINT)
								.withNewPort(externalPort)
								.build()
				)				
				.withTimeoutSeconds(timeout)
				.withInitialDelaySeconds(initialDelay)
				.build();
	}
	
	private List<String> createCommandArgs(ModuleDeploymentRequest request) {
		HashMap<String, String> args = new HashMap<>();
		args.put("modules", request.getCoordinates().toString());
		args.putAll(ModuleArgumentQualifier.qualifyArgs(0, request.getDefinition().getParameters()));
		args.putAll(ModuleArgumentQualifier.qualifyArgs(0, request.getDeploymentProperties()));
		
		List<String> cmdArgs = new LinkedList<String>();
		for (Map.Entry<String, String> entry : args.entrySet()) {

			cmdArgs.add(String.format("--%s=%s", bashEscape(entry.getKey()), 
					bashEscape(entry.getValue())));
		}
		return cmdArgs;	
	}
	
	private List<EnvVar> createModuleLauncherEnvArgs(ModuleDeploymentRequest request) {
		List<EnvVar> envVars = new LinkedList<EnvVar>();		
		
		// Pass on the same REDIS host configuration that the Spring Admin uses to each Kubernetes
		// RC/POD that it creates. This may be a limitation in case we want different redis per 
		// customer. We could move this to module deployment properties if needed.
		String redisHost = System.getenv(SPRING_REDIS_HOST);
		if (redisHost != null) {
			envVars.add(new EnvVarBuilder()
					.withName(SPRING_REDIS_HOST)
					.withValue(redisHost)
					.build());
		}		
		return envVars;		
	}	
	
	private ModuleStatus buildModuleStatus(ModuleDeploymentId id, PodList list) {
		ModuleStatus.Builder statusBuilder = ModuleStatus.of(id);
		String moduleId = id.toString();
		
		if (list == null) {
			statusBuilder.with(new KubernetesModuleInstanceStatus(moduleId, null));
		} else {
			
			for (Pod pod : list.getItems()) {			
				statusBuilder.with(new KubernetesModuleInstanceStatus(moduleId, pod));
			}		
		}		
		return statusBuilder.build();
	}
}
