/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2017 MicroBean.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.servicebroker.jaxrs;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;

import javax.ws.rs.core.Response;

import javax.ws.rs.ext.Provider;

import org.microbean.servicebroker.api.command.InvalidServiceBrokerCommandException;

@Provider
public final class InvalidServiceBrokerCommandExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<InvalidServiceBrokerCommandException> {

  public InvalidServiceBrokerCommandExceptionMapper() {
    super();
    final String cn = this.getClass().getName();
    final Logger logger = Logger.getLogger(cn);
    assert logger != null;
    final String mn = "<init>";
    if (logger.isLoggable(Level.FINER)) {
      logger.entering(cn, mn);
      logger.exiting(cn, mn);
    }
  }
  
  @Override
  public final Response toResponse(final InvalidServiceBrokerCommandException exception) {
    final String cn = this.getClass().getName();
    final Logger logger = Logger.getLogger(cn);
    assert logger != null;
    final String mn = "<init>";
    if (logger.isLoggable(Level.FINER)) {
      logger.entering(cn, mn, exception);
    }
    if (logger.isLoggable(Level.SEVERE)) {
      String message = exception.getMessage();
      if (message == null) {
        message = exception.toString();
      }
      logger.logp(Level.SEVERE, cn, mn, message, exception);
    }
    final Response returnValue = Response.status(400)
      .entity("{\"description\": \"" + exception.toString() + "\"}")
      .build();
    if (logger.isLoggable(Level.FINER)) {
      logger.exiting(cn, mn, returnValue);
    }
    return returnValue;
  }
  
}
