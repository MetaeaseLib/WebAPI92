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

package jp.co.metaease.web.ctrl;

import java.util.Map;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/** Example REST controller to demonstrate structured logging. */
@RestController
public class WebApi920101Controller {
  // 'spring-cloud-gcp-starter-logging' module provides support for
  // associating a web request trace ID with the corresponding log entries.
  // https://cloud.spring.io/spring-cloud-gcp/multi/multi__stackdriver_logging.html
  private static final Logger logger = LoggerFactory.getLogger(WebApi920101Controller.class);

  /** endpoint handler. */
  @GetMapping("/")
  public @ResponseBody String index() {
    // Example of structured logging - add custom fields
    MDC.put("logField", "index");
    MDC.put("arbitraryField", "任意の領域");
    // Use logger with log correlation
    // https://cloud.google.com/run/docs/logging#correlate-logs
    logger.info("Structured logging.");
    return "ルートディレクトリ起動";
  }

  /** endpoint handler. */
  @GetMapping("/test")
  public @ResponseBody String test() {
    // Example of structured logging - add custom fields
    MDC.put("logField", "test");
    MDC.put("arbitraryField", "任意の領域");
    // Use logger with log correlation
    // https://cloud.google.com/run/docs/logging#correlate-logs
    logger.info("Structured logging.");
    return "Hello World!!!!!!!!";
  }

  /** endpoint handler. */
  @PostMapping("/")
  @ResponseBody
  public LoginMsg getLoginMsg(@RequestHeader Map<String, String> headers, @RequestParam Map<String, String> body) {
 
    LoginMsg rtn = new LoginMsg();

    // Example of structured logging - add custom fields
    MDC.put("logField", "getLoginMsg");
    MDC.put("arbitraryField", "■");
    // Use logger with log correlation
    // https://cloud.google.com/run/docs/logging#correlate-logs
    logger.info("Structured logging.getLoginMsg START");

    // Get decoded Id Platform user id
    String uid = authenticateJwt(headers);

    Date date = new Date();
    Timestamp timestamp = new Timestamp(date.getTime());

    MDC.put("uid", uid);
    logger.info("authenticateJwt OK.uid=[" + uid + "]");

    rtn.setMessage("Successfully authentication for " + uid + " at " + timestamp.toLocalDateTime());

    return (rtn);
//    return "Successfully authentication for " + uid + " at " + timestamp.toLocalDateTime();
//    return "ルートディレクトリ起動";
  }

  // [START cloudrun_user_auth_jwt]
  /** Extract and verify Id Token from header */
  private String authenticateJwt(Map<String, String> headers) {
    String authHeader =
        (headers.get("authorization") != null)
            ? headers.get("authorization")
            : headers.get("Authorization");
    if (authHeader != null) {
      String idToken = authHeader.split(" ")[1];
      // If the provided ID token has the correct format, is not expired, and is
      // properly signed, the method returns the decoded ID token
      try {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        String uid = decodedToken.getUid();
        return uid;
      } catch (FirebaseAuthException e) {
        logger.error("Error with authentication: " + e.toString());
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "", e);
      }
    } else {
      logger.error("Error no authorization header");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
  }
  // [END cloudrun_user_auth_jwt]

  // Message
  public static class LoginMsg{
    private String message;

    public String getMessage(){
      return this.message;
    }

    public void setMessage(String msg){
        this.message = msg;
    }
  }
}
