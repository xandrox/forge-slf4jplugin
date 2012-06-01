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

import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.DependencyInstaller;
import org.jboss.forge.project.dependencies.ScopeType;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

/**
 * @author Sandro Sonntag Adorsys
 */
@Alias("loggingFacet")
@RequiresFacet({ MavenCoreFacet.class, DependencyFacet.class,
		ResourceFacet.class })
public class Slf4jFacet extends BaseFacet {
	
	private static final String LOG4J_DEP = "org.slf4j:slf4j-log4j12";
	public static final String LOG4J_XML = "/log4j.xml";
	private static final String SLF4J_IDENTIFIER = "org.slf4j:slf4j-api";
	private static final DependencyBuilder SLF4JDEP = DependencyBuilder.create(SLF4J_IDENTIFIER);

	@Inject
	private Shell shell;

	@Inject
	private DependencyInstaller installer;

	@Override
	public boolean install() {

		DependencyFacet deps = getProject().getFacet(DependencyFacet.class);

		List<Dependency> slf4jVersions = deps
				.resolveAvailableVersions(SLF4J_IDENTIFIER + ":[,]");
		Dependency slf4JVersion = shell.promptChoiceTyped(
				"Install which version of the slf4j API?", slf4jVersions);
		installer.install(project, slf4JVersion);

		return true;
	}
	
	public void setSlf4JProvided(){
		DependencyFacet deps = getProject().getFacet(DependencyFacet.class);
		deps.addDirectDependency(
				DependencyBuilder.create().setGroupId("org.slf4j").setArtifactId("slf4j-api").setScopeType(ScopeType.PROVIDED).setVersion(null));
	}

	@Override
	public boolean isInstalled() {
		DependencyFacet mdf = getProject().getFacet(DependencyFacet.class);
		return mdf.hasEffectiveDependency(SLF4JDEP);
	}

	@Override
	public boolean uninstall() {
		DependencyFacet mdf = getProject().getFacet(DependencyFacet.class);
		ResourceFacet resource = getProject().getFacet(ResourceFacet.class);
		mdf.removeDependency(SLF4JDEP);
		mdf.removeManagedDependency(SLF4JDEP);
		mdf.removeDependency(DependencyBuilder.create(LOG4J_DEP));
		mdf.removeManagedDependency(DependencyBuilder.create(LOG4J_DEP));
		FileResource<?> r = resource.getResource(LOG4J_XML);
		if (r != null) {
			r.delete();
		}
		return true;
	}

}
