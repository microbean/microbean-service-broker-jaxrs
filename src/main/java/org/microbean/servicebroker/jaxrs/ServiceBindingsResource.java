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

import java.net.URI;

import java.util.Objects;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.microbean.servicebroker.api.ServiceBroker;
import org.microbean.servicebroker.api.ServiceBrokerException;

import org.microbean.servicebroker.api.command.AbstractResponse;
import org.microbean.servicebroker.api.command.BindingAlreadyExistsException;
import org.microbean.servicebroker.api.command.DeleteBindingCommand;
import org.microbean.servicebroker.api.command.IdenticalBindingAlreadyExistsException;
import org.microbean.servicebroker.api.command.NoSuchServiceInstanceException;
import org.microbean.servicebroker.api.command.ProvisionBindingCommand;
import org.microbean.servicebroker.api.command.UnbindablePlanException;

@Path("/service_instances/{instance_id}/service_bindings")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ServiceBindingsResource {

  @Inject
  private ServiceBroker serviceBroker;
  
  public ServiceBindingsResource() {
    super();
  }

  @PUT
  @Path("{binding_id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response putServiceBinding(@PathParam("instance_id") final String instanceId,
                                    @PathParam("binding_id") final String bindingId,
                                    final ProvisionBindingCommand command)
    throws ServiceBrokerException {
    final String cn = this.getClass().getName();
    final String mn = "putServiceBinding";
    final Logger logger = Logger.getLogger(cn);
    assert logger != null;
    if (logger.isLoggable(Level.FINER)) {
      logger.entering(cn, mn, new Object[] { instanceId, bindingId, command });
    }

    Objects.requireNonNull(instanceId);
    Objects.requireNonNull(bindingId);
    Objects.requireNonNull(command);
    
    if (command.getInstanceId() == null) {
      command.setInstanceId(instanceId);
    }
    if (command.getBindingId() == null) {
      command.setBindingId(bindingId);
    }

    final Response returnValue;
    if (!this.serviceBroker.isPlanBindable(command.getServiceId(), command.getPlanId())) {
      // The specification is ambiguous as to whether a 404 or a 400
      // is called for.  See
      // https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#binding.
      returnValue = Response.status(404).entity("{}").build();
    } else {
      Response temp = null;
      try {
        final ProvisionBindingCommand.Response commandResponse = this.serviceBroker.execute(command);
        if (commandResponse == null) {
          temp = Response.serverError().entity("{}").build();
        } else {
          temp = Response.status(201).entity(commandResponse).build();
        }
      } catch (final NoSuchServiceInstanceException noSuchServiceInstanceException) {
        // See https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#response-4
        temp = Response.status(Response.Status.BAD_REQUEST)
          .entity("{\"description\":\"" + noSuchServiceInstanceException.toString() + "\"}")
          .build();
      } catch (final UnbindablePlanException unbindablePlanException) {
        // The specification is ambiguous as to whether a 404 or a 400
        // is called for.  See
        // https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#binding.
        temp = Response.status(404).entity("{}").build();
      } catch (final IdenticalBindingAlreadyExistsException identicalBindingAlreadyExistsException) {
        // See https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#response-4
        final AbstractResponse abstractResponse = identicalBindingAlreadyExistsException.getResponse();
        if (abstractResponse == null) {
          temp = Response.ok().entity("{}").build();
        } else {
          temp = Response.ok().entity(abstractResponse).build();
        }
      } catch (final BindingAlreadyExistsException bindingAlreadyExistsException) {
        // See https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#response-4
        final String message = bindingAlreadyExistsException.getMessage();
        if (message == null) {
          temp = Response.status(409).entity("{}").build();
        } else {
          temp = Response.status(409).entity("{\n  \"description\" : \"" + message + "\"\n  }").build();
        }
      } finally {
        returnValue = temp;
      }
    }

    if (logger.isLoggable(Level.FINER)) {
      logger.exiting(cn, mn, returnValue);
    }
    return returnValue;
  }

  @DELETE
  @Path("{binding_id}")
  public Response deleteServiceBinding(@PathParam("instance_id") final String instanceId,
                                       @PathParam("binding_id") final String bindingId,
                                       @QueryParam("service_id") final String serviceId,
                                       @QueryParam("plan_id") final String planId)
    throws ServiceBrokerException {
    final String cn = this.getClass().getName();
    final String mn = "deleteServiceBinding";
    final Logger logger = Logger.getLogger(cn);
    assert logger != null;
    if (logger.isLoggable(Level.FINER)) {
      logger.entering(cn, mn, new Object[] { instanceId, bindingId, serviceId, planId });
    }

    Objects.requireNonNull(instanceId);
    Objects.requireNonNull(bindingId);
    Objects.requireNonNull(serviceId);
    Objects.requireNonNull(planId);

    final DeleteBindingCommand command = new DeleteBindingCommand(instanceId, bindingId, serviceId, planId);
    final Response returnValue;
    Response temp = null;
    try {
      final DeleteBindingCommand.Response commandResponse = this.serviceBroker.execute(command);
      if (commandResponse == null) {
        temp = Response.serverError().entity("{}").build();
      } else {
        temp = Response.ok(commandResponse).build();
      }
    } catch (final NoSuchServiceInstanceException noSuchServiceInstanceException) {
      temp = Response.status(Response.Status.GONE)
        .entity("{\"description\":\"" + noSuchServiceInstanceException.toString() + "\"}")
        .build();
    } finally {
      returnValue = temp;
    }

    if (logger.isLoggable(Level.FINER)) {
      logger.exiting(cn, mn, returnValue);
    }
    return returnValue;
  }
  
}
