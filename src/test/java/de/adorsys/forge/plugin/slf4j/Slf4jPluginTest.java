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

import javax.enterprise.event.Event;
import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.parser.java.JavaSource;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.resources.java.JavaResource;
import org.jboss.forge.shell.events.PickupResource;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

import de.adorsys.forge.plugin.slf4j.Slf4jPlugin;

/**
 * @author Sandro Sonntag - Adorsys
 */
public class Slf4jPluginTest extends AbstractShellTest {
	
	@Inject
	private Event<PickupResource> pickEvent;
	
	@Deployment
	public static JavaArchive getDeployment() {
		return AbstractShellTest.getDeployment().addPackages(true,
				Slf4jPlugin.class.getPackage());
	}

	@Test
	public void testSetupLog4J() throws Exception {
		Project project = initializeJavaProject();
		queueInputLines("30", "30");
		getShell().execute("slf4j setup --log-backend LOG4J");
		
		ResourceFacet facet = project.getFacet(ResourceFacet.class);
		Assert.assertNotNull(facet.getResource("/log4j.xml"));
	}
	
	@Test
	public void testSetupJBoss() throws Exception {
		Project project = initializeJavaProject();
		queueInputLines("30");
		getShell().execute("slf4j setup --log-backend JBOSS_AS_7");
		
		ResourceFacet facet = project.getFacet(ResourceFacet.class);
		Assert.assertNotNull(facet.getResource("/META-INF/MANIFEST.MF"));
	}
	
	@Test
	public void testUninstall() throws Exception {
		initializeJavaProject();
		queueInputLines("30", "30");
		getShell().execute("slf4j setup");
		getShell().execute("slf4j uninstall");
	}

	@Test
	public void testAddLogger() throws Exception {
		Project project = initializeJavaProject();
		queueInputLines("30", "30");
		getShell().execute("slf4j setup");

		JavaSourceFacet javaSourceFacet = project.getFacet(JavaSourceFacet.class);
		JavaSource<?> testClass = JavaParser.parse("public class Test {}");
		testClass.setPackage("test");
		JavaResource javaTestResource = javaSourceFacet.saveJavaSource(testClass);
		pickEvent.fire(new PickupResource(javaTestResource));
		getShell().execute("slf4j addlogger");
		javaTestResource = javaSourceFacet.getJavaResource("test.Test");
		JavaClass testJavaClass = (JavaClass) javaTestResource.getJavaSource();
		Assert.assertTrue("log field containt", testJavaClass.hasField("LOG"));
		Assert.assertTrue("import org.slf4j.Logger", testJavaClass.hasImport("org.slf4j.Logger"));
		Assert.assertTrue("import org.slf4j.LoggerFactory", testJavaClass.hasImport("org.slf4j.LoggerFactory"));
	}

}
