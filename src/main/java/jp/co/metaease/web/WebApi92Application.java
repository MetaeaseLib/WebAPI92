/*
 * Copyright 2023 Metaease LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.metaease.web;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/** Microservice template for Cloud Run. */
@SpringBootApplication
public class WebApi92Application {
  private static final Logger logger =
      LoggerFactory.getLogger(WebApi92Application.class);

  public static void main(String[] args) throws IOException {

    String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
    if (projectId == null) {
      projectId = getProjectId();
    }
    logger.info(WebApi92Application.class.getSimpleName() + ": ### ProjectID[" + projectId + "]");

    // Initialize Firebase Admin SDK
    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
    FirebaseOptions options =
        FirebaseOptions.builder().setProjectId(projectId).setCredentials(credentials).build();
    FirebaseApp.initializeApp(options);
    SpringApplication.run(WebApi92Application.class, args);
  }

  /** Register shutdown hook to listen for termination signal. */
  @PreDestroy
  public void tearDown() {
    // Clean up resources on shutdown
    logger.info(WebApi92Application.class.getSimpleName() + ": received SIGTERM.");
    // Flush async logs if needed - current Logback config does not buffer logs
  }

  /** Retrieve project Id from metadata server Set $GOOGLE_CLOUD_PROJECT env var to run locally */
  public static String getProjectId() {
    OkHttpClient ok =
        new OkHttpClient.Builder()
            .readTimeout(500, TimeUnit.MILLISECONDS)
            .writeTimeout(500, TimeUnit.MILLISECONDS)
            .build();

    String metadataUrl = "http://metadata.google.internal/computeMetadata/v1/project/project-id";
    Request request =
        new Request.Builder().url(metadataUrl).addHeader("Metadata-Flavor", "Google").get().build();

    try {
      Response response = ok.newCall(request).execute();
      return response.body().string();
    } catch (IOException e) {
      logger.error("Error retrieving the project Id.");
      throw new RuntimeException("Unable to retrieve project Id.");
    }
  }

}
