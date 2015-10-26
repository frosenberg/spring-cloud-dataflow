package org.springframework.cloud.dataflow.module.deployer.kubernetes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.dataflow.core.ModuleDeploymentRequest;

/**
 * Creates a container that uses a pre-baked Docker image for launching a module.
 * This mean it won't rely on the {@code spring-stream-module-launcher} and the
 * ability to dynamically load the modules' JAR file. Instead, this implementation
 * relies on pre-baked images that contain the modules.
 *
 * @author Florian Rosenberg
 *
 */
public class PreBuiltDockerImageContainerFactory extends DefaultContainerFactory
		implements ContainerFactory {
	
	@Override
	protected String deduceImageName(ModuleDeploymentRequest request) {
		String repo = properties.getImageRepository();
		String prefix = properties.getImageNamePrefix();
		String name = request.getCoordinates().getArtifactId();
		String tag = request.getCoordinates().getVersion();
		return String.format("%s/%s-%s:%s", repo, prefix, name, tag);
	}

	@Override
	protected List<String> createCommandArgs(ModuleDeploymentRequest request) {
		HashMap<String, String> args = new HashMap<>();
		args.putAll(request.getDefinition().getParameters());
		args.putAll(request.getDeploymentProperties());
		
		List<String> cmdArgs = new LinkedList<String>();
		for (Map.Entry<String, String> entry : args.entrySet()) {

			cmdArgs.add(String.format("--%s=%s", bashEscape(entry.getKey()), 
					bashEscape(entry.getValue())));
		}
		return cmdArgs;	
	}
	
}