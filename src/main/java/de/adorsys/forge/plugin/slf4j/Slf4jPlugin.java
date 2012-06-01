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

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.project.facets.events.RemoveFacets;
import org.jboss.forge.resources.java.JavaResource;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Current;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.shell.plugins.SetupCommand;

/**
 *
 */
@Alias("slf4j")
@Help("Setup slf4j for a project and add logging code to classes")
@RequiresFacet(Slf4jFacet.class)
public class Slf4jPlugin implements Plugin
{
   @Inject
   private ShellPrompt prompt;

   @Inject
   private Project project;

   @Inject
   private Event<InstallFacets> installFacets;

   @Inject
   private Event<RemoveFacets> uninstallFacet;

   @Current
   @Inject
   private JavaResource javaResource;

   @SetupCommand(help="setup slf4j logging for this project")
   public void setup(@Option(name="log-backend", defaultValue="LOG4J", help="Set the logging backend that should be used") LogBackend logBackend, PipeOut out)
   {
      if (!project.hasAllFacets(Slf4jFacet.class))
      {
         installFacets.fire(new InstallFacets(Slf4jFacet.class));
      }
      else
      {
         ShellMessages.info(out, "is installed");
      }
   }

   @Command
   public void uninstall()
   {
      if (project.hasAllFacets(Slf4jFacet.class))
      {
         uninstallFacet.fire(new RemoveFacets(Slf4jFacet.class));
      }
   }

   @Command
   public void addLogger(JavaResource javaResource, PipeOut out) throws FileNotFoundException
   {
      System.out.println(this.javaResource);
      JavaClass java = (JavaClass) this.javaResource.getJavaSource();
      java.addImport("org.slf4j.Logger");
      java.addImport("org.slf4j.LoggerFactory");

      if (!java.hasField("LOG"))
      {
         java.addField("private static final Logger LOG = LoggerFactory.getLogger(" + java.getName() + ".class)");
      }

      JavaSourceFacet javaSourceFacet = project.getFacet(JavaSourceFacet.class);
      javaSourceFacet.saveJavaSource(java);
   }

}
