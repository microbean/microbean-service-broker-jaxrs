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

import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;
import javax.inject.Provider;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.microbean.servicebroker.api.ServiceBroker;
import org.microbean.servicebroker.api.ServiceBrokerException;

import org.microbean.servicebroker.api.command.NoSuchServiceInstanceException;

import org.microbean.servicebroker.api.command.DeleteBindingCommand;
import org.microbean.servicebroker.api.command.ProvisionBindingCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Path("/service_instances/{instance_id}/service_bindings")
@Produces(MediaType.APPLICATION_JSON)
public class ServiceBindingsResource {

  private final Logger logger;
  
  @Inject
  private ServiceBroker serviceBroker;
  
  public ServiceBindingsResource() {
    super();
    this.logger = LoggerFactory.getLogger(this.getClass());
    assert this.logger != null;
  }

  @PUT
  @Path("{binding_id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response putServiceBinding(@PathParam("instance_id") final String instanceId,
                                    @PathParam("binding_id") final String bindingId,
                                    final ProvisionBindingCommand command)
    throws ServiceBrokerException {
    if (logger.isTraceEnabled()) {
      logger.trace("ENTRY {}, {}, {}", instanceId, bindingId, command);
    }
    Objects.requireNonNull(instanceId);
    Objects.requireNonNull(bindingId);
    Objects.requireNonNull(command);
    if (command.getServiceInstanceId() == null) {
      command.setServiceInstanceId(instanceId);
    }
    if (command.getBindingInstanceId() == null) {
      command.setBindingInstanceId(bindingId);
    }
    final Response returnValue;
    Response temp = null;
    try {
      final ProvisionBindingCommand.Response commandResponse = this.serviceBroker.execute(command);
      if (commandResponse == null) {
        temp = Response.serverError().entity("{}").build();
      } else {
        temp = Response.status(201).entity(commandResponse).build();
      }
    } catch (final NoSuchServiceInstanceException noSuchServiceInstanceException) {
      temp = Response.status(Response.Status.BAD_REQUEST)
        .entity("{\"description\":\"" + noSuchServiceInstanceException.toString() + "\"}")
        .build();
    } finally {
      returnValue = temp;
    }
    if (logger.isTraceEnabled()) {
      logger.trace("EXIT {}", returnValue);
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
    if (logger.isTraceEnabled()) {
      logger.trace("ENTRY {}, {}, {}, {}", instanceId, bindingId, serviceId, planId);
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
    if (logger.isTraceEnabled()) {
      logger.trace("EXIT {}", returnValue);
    }
    return returnValue;
  }
  
}
