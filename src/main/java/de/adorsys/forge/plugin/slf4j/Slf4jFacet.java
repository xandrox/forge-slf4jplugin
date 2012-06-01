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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

@Alias("loggingFacet")
@RequiresFacet( { MavenCoreFacet.class, DependencyFacet.class, ResourceFacet.class })
public class Slf4jFacet extends BaseFacet
{

   private static final DependencyBuilder SLF4J = DependencyBuilder.create("org.slf4j:slf4j-api:1.6.1:compile:jar");

   @Override
   public boolean install()
   {
      DependencyFacet mdf = getProject().getFacet(DependencyFacet.class);
      mdf.addDirectDependency(SLF4J);

      ResourceFacet resource = getProject().getFacet(ResourceFacet.class);
      InputStream resourceAsStream = Slf4jFacet.class.getResourceAsStream("/logging.properties.vm");
      try
      {
         char[] data = IOUtils.toCharArray(resourceAsStream);
         FileResource<?> r = resource.createResource(data, "/logging.properties");
      }
      catch (IOException e)
      {
         return false;
      }
      return true;
   }

   @Override
   public boolean isInstalled()
   {
      DependencyFacet mdf = getProject().getFacet(DependencyFacet.class);
      return mdf.hasDirectDependency(SLF4J);
   }

   @Override
   public boolean uninstall()
   {
      DependencyFacet mdf = getProject().getFacet(DependencyFacet.class);
      ResourceFacet resource = getProject().getFacet(ResourceFacet.class);
      mdf.removeDependency(SLF4J);
      FileResource<?> r = resource.getResource("/logging.properties");
      if (r != null)
      {
         r.delete();
      }
      return true;
   }

}
