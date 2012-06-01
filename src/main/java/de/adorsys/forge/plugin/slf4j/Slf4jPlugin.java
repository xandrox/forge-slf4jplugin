/**
 * Copyright (C) 2012 Sandro Sonntag sso@adorsys.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.adorsys.forge.plugin.slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyInstaller;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.project.facets.events.RemoveFacets;
import org.jboss.forge.resources.java.JavaResource;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Current;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.shell.plugins.SetupCommand;
import org.jboss.seam.render.TemplateCompiler;
import org.jboss.seam.render.template.CompiledTemplateResource;

/**
 * @author Sandro Sonntag Adorsys
 */
@Alias("slf4j")
@Help("Setup slf4j for a project and add logging constants to classes")
@RequiresFacet(Slf4jFacet.class)
public class Slf4jPlugin implements Plugin {
	
	@Inject
	private Shell shell;

	@Inject
	private Project project;

	@Inject
	private Event<InstallFacets> installFacets;

	@Inject
	private Event<RemoveFacets> uninstallFacet;

	@Current
	@Inject
	private JavaResource javaResource;
	
	@Inject
	private DependencyInstaller installer;
	
	private CompiledTemplateResource templateLog4jXml;
	
	@Inject
	public Slf4jPlugin(TemplateCompiler compiler) {
		templateLog4jXml = compiler.compileResource(Slf4jPlugin.class
				.getResourceAsStream("/templates/log4j.xml.jv"));
	}

	@SetupCommand(help = "setup slf4j logging for this project")
	public void setup(
			@Option(name = "log-backend", defaultValue = "LOG4J", help = "Set the logging backend that should be used") LogBackend logBackend,
			PipeOut out) throws IOException {
		if (!project.hasFacet(Slf4jFacet.class)) {
			installFacets.fire(new InstallFacets(Slf4jFacet.class));
			
			switch(logBackend) {
				case LOG4J:
					installLog4J();
					
					break;
				case JBOSS_AS_7:
					installJbossAs();
					break;
			}
			
		} else {
			ShellMessages.info(out, "slf4j is already installed");
		}
	}

	private void installJbossAs() throws IOException {
		InputStream is = Slf4jPlugin.class.getResourceAsStream("/templates/MANIFEST.MF");
		ResourceFacet resource = project.getFacet(ResourceFacet.class);
		resource.createResource(IOUtils.toCharArray(is), "/META-INF/MANIFEST.MF");
		
		Slf4jFacet slf4jFacet = project.getFacet(Slf4jFacet.class);
		slf4jFacet.setSlf4JProvided();
	}

	private void installLog4J() {
		DependencyFacet deps = project.getFacet(DependencyFacet.class);

		List<Dependency> slf4jVersions = deps
				.resolveAvailableVersions("org.slf4j:slf4j-log4j12:[,]:runtime");
		Dependency slf4JVersion = shell.promptChoiceTyped(
				"Install which version of the slf4j-log4j12 API?", slf4jVersions);
		installer.install(project, slf4JVersion);
		
		ResourceFacet resource = project.getFacet(ResourceFacet.class);
		JavaSourceFacet javaSourceFacet = project.getFacet(JavaSourceFacet.class);
		HashMap<Object, Object> context = new HashMap<Object, Object>();
		context.put("projectPackage", javaSourceFacet.getBasePackage());
		String renderedContent = templateLog4jXml.render(context);
		resource.createResource(renderedContent.toCharArray(), Slf4jFacet.LOG4J_XML);
	}

	@Command
	public void uninstall() {
		if (project.hasFacet(Slf4jFacet.class)) {
			uninstallFacet.fire(new RemoveFacets(Slf4jFacet.class));
		}
	}

	@Command(help="Add a logger constant to a java source")
	public void addLogger(@Option(help="the javasource for the logger constant") JavaResource javaResource, PipeOut out)
			throws FileNotFoundException {
		if (javaResource == null) {
			javaResource = this.javaResource;
		}
		if (javaResource == null) {
			ShellMessages
					.error(out,
							"no java resource is given (go to a .java file or add a valid java file as parameter)");
			return;
		}

		JavaClass java = (JavaClass) javaResource.getJavaSource();
		java.addImport("org.slf4j.Logger");
		java.addImport("org.slf4j.LoggerFactory");

		if (!java.hasField("LOG")) {
			java.addField("private static final Logger LOG = LoggerFactory.getLogger("
					+ java.getName() + ".class)");
		}

		JavaSourceFacet javaSourceFacet = project
				.getFacet(JavaSourceFacet.class);
		javaSourceFacet.saveJavaSource(java);
	}

}
