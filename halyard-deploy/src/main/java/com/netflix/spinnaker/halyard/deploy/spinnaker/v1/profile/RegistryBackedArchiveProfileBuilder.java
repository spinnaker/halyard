/*
 * Copyright 2017 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package com.netflix.spinnaker.halyard.deploy.spinnaker.v1.profile;

import com.netflix.spinnaker.halyard.config.model.v1.node.DeploymentConfiguration;
import com.netflix.spinnaker.halyard.core.error.v1.HalException;
import com.netflix.spinnaker.halyard.core.problem.v1.Problem;
import com.netflix.spinnaker.halyard.core.registry.v1.ProfileRegistry;
import com.netflix.spinnaker.halyard.deploy.services.v1.ArtifactService;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerArtifact;
import com.netflix.spinnaker.halyard.deploy.spinnaker.v1.SpinnakerRuntimeSettings;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class RegistryBackedArchiveProfileBuilder {
  @Autowired
  ProfileRegistry profileRegistry;

  @Autowired
  private ArtifactService artifactService;

  public List<Profile> build(DeploymentConfiguration deploymentConfiguration, String baseOutputPath, SpinnakerArtifact artifact, String archiveName) {
    String version = artifactService.getArtifactVersion(deploymentConfiguration.getName(), artifact);
    String archiveObjectName = ProfileRegistry.profilePath(artifact.getName(), version, archiveName + ".tar.gz");

    InputStream is;
    try {
      is = profileRegistry.getObjectContents(archiveObjectName);
    } catch (IOException e) {
      throw new HalException(Problem.Severity.FATAL, "Error retrieving contents of archive " + archiveObjectName, e);
    }

    TarArchiveInputStream tis;
    try {
      tis = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);
    } catch (ArchiveException e) {
      throw new HalException(Problem.Severity.FATAL, "Failed to unpack tar archive", e);
    }

    try {
      List<Profile> result = new ArrayList<>();

      ArchiveEntry profileEntry = tis.getNextEntry();
      while (profileEntry != null) {
        if (profileEntry.isDirectory()) {
          profileEntry = tis.getNextEntry();
          continue;
        }

        String entryName = profileEntry.getName();
        String profileName = String.join("/", artifact.getName(), archiveName, entryName);
        String outputPath = Paths.get(baseOutputPath, archiveName, entryName).toString();
        String contents = IOUtils.toString(tis);

        result.add((new ProfileFactory() {
          @Override
          protected void setProfile(Profile profile, DeploymentConfiguration deploymentConfiguration, SpinnakerRuntimeSettings endpoints) {
            profile.setContents(profile.getBaseContents());
          }

          @Override
          protected Profile getBaseProfile(String name, String version, String outputFile) {
            return new Profile(name, version, outputFile, contents);
          }

          @Override
          protected boolean showEditWarning() {
            return false;
          }

          @Override
          protected ArtifactService getArtifactService() {
            return artifactService;
          }

          @Override
          public SpinnakerArtifact getArtifact() {
            return artifact;
          }

          @Override
          protected String commentPrefix() {
            return null;
          }
        }).getProfile(profileName, outputPath, deploymentConfiguration, null));

        profileEntry = tis.getNextEntry();
      }

      return result;
    } catch (IOException e) {
      throw new HalException(Problem.Severity.FATAL, "Failed to read profile entry", e);
    }

  }
}
