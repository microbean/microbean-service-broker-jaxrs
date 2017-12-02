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

import javax.ws.rs.core.Response;

import javax.ws.rs.ext.Provider;

import org.microbean.servicebroker.api.command.AbstractCommand;
import org.microbean.servicebroker.api.command.DeleteServiceInstanceCommand;
import org.microbean.servicebroker.api.command.NoSuchServiceInstanceException;
import org.microbean.servicebroker.api.command.ProvisionServiceInstanceCommand;

@Provider
public final class NoSuchServiceInstanceExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<NoSuchServiceInstanceException> {

  public NoSuchServiceInstanceExceptionMapper() {
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
  public final Response toResponse(final NoSuchServiceInstanceException exception) {
    final String cn = this.getClass().getName();
    final Logger logger = Logger.getLogger(cn);
    assert logger != null;
    final String mn = "<init>";
    if (logger.isLoggable(Level.FINER)) {
      logger.entering(cn, mn, exception);
    }

    String message = exception.getMessage();
    if (message == null) {
      message = exception.toString();
    }
    if (logger.isLoggable(Level.SEVERE)) {
      logger.logp(Level.SEVERE, cn, mn, message, exception);
    }
    
    final Response returnValue;
    final AbstractCommand command = exception.getCommand();
    if (command instanceof DeleteServiceInstanceCommand) {
      // See
      // https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#response-6
      returnValue = Response.status(Response.Status.GONE).entity("{}").build();
    } else if (command instanceof ProvisionServiceInstanceCommand) {
      // Edge case; if someone asked to provision a service instance,
      // of course it doesn't exist.  So that's not a bad request.
      returnValue = Response.status(500)
        .entity("{\n  \"description\" : \"" + message + "\"\n  }")
        .build();
    } else {
      // No matter what else the command might be, it indicates a
      // problem with the request.
      returnValue = Response.status(400)
        .entity("{\n  \"description\" : \"" + message + "\"\n  }")
        .build();
    }
    
    if (logger.isLoggable(Level.FINER)) {
      logger.exiting(cn, mn, returnValue);
    }
    return returnValue;
  }
  
}
