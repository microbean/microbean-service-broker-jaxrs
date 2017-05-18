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

import org.microbean.servicebroker.api.ServiceBroker;
import org.microbean.servicebroker.api.ServiceBrokerException;

import org.microbean.servicebroker.api.command.DeleteServiceInstanceCommand;
import org.microbean.servicebroker.api.command.NoSuchServiceInstanceException;
import org.microbean.servicebroker.api.command.ProvisionServiceInstanceCommand;
import org.microbean.servicebroker.api.command.ServiceInstanceAlreadyExistsException;

import org.microbean.servicebroker.api.query.state.ServiceInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Path("/service_instances")
@Produces(MediaType.APPLICATION_JSON)
public class ServiceInstancesResource {

  private final Logger logger;
  
  @Inject
  private ServiceBroker serviceBroker;
  
  public ServiceInstancesResource() {
    super();
    this.logger = LoggerFactory.getLogger(this.getClass());
    assert this.logger != null;
  }

  @GET
  @Path("{instance_id}")
  public ServiceInstance getServiceInstance(@PathParam("instance_id") final String instanceId)
    throws ServiceBrokerException {
    if (logger.isTraceEnabled()) {
      logger.trace("ENTRY {}", instanceId);
    }
    Objects.requireNonNull(instanceId);
    ServiceInstance returnValue = null;
    try {
      returnValue = this.serviceBroker.getServiceInstance(instanceId);
    } catch (final NoSuchServiceInstanceException noSuchServiceInstanceException) {
      throw new NotFoundException(noSuchServiceInstanceException.getMessage(), noSuchServiceInstanceException);
    }
    if (logger.isTraceEnabled()) {
      logger.trace("EXIT {}", returnValue);
    }
    return returnValue;
  }

  @PUT
  @Path("{instance_id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response putServiceInstance(@PathParam("instance_id") final String instanceId,
                                     final ProvisionServiceInstanceCommand command,
                                     @Context final UriInfo uriInfo)
    throws ServiceBrokerException {
    if (logger.isTraceEnabled()) {
      logger.trace("ENTRY {}, {}, {}", instanceId, command, uriInfo);
    }
    Objects.requireNonNull(instanceId);
    Objects.requireNonNull(command);
    Objects.requireNonNull(uriInfo);
    if (command.getInstanceId() == null) {
      command.setInstanceId(instanceId);
    }
    Response returnValue = null;
    try {
      final ProvisionServiceInstanceCommand.Response commandResponse = this.serviceBroker.execute(command);
      returnValue = Response.created(uriInfo.getAbsolutePath()).entity(commandResponse).build();
    } catch (final ServiceInstanceAlreadyExistsException serviceInstanceAlreadyExistsException) {
      returnValue = Response.ok(serviceInstanceAlreadyExistsException.getResponse()).build();
    }
    if (logger.isTraceEnabled()) {
      logger.trace("EXIT {}", returnValue);
    }
    return returnValue;
  }

  @DELETE
  @Path("{instance_id}")
  public Response deleteServiceInstance(@PathParam("instance_id") final String instanceId,
                                        @QueryParam("service_id") final String serviceId,
                                        @QueryParam("plan_id") final String planId,
                                        @QueryParam("accepts_incomplete") final boolean acceptsIncomplete)
    throws ServiceBrokerException {
    if (logger.isTraceEnabled()) {
      logger.trace("ENTRY {}, {}, {}", instanceId, serviceId, planId, acceptsIncomplete);
    }
    Objects.requireNonNull(instanceId);
    Objects.requireNonNull(serviceId);
    Objects.requireNonNull(planId);

    Response returnValue = null;
    final DeleteServiceInstanceCommand command = new DeleteServiceInstanceCommand(instanceId, serviceId, planId, acceptsIncomplete);
    try {
      final DeleteServiceInstanceCommand.Response commandResponse = this.serviceBroker.execute(command);
      returnValue = Response.ok(commandResponse).build();
    } catch (final NoSuchServiceInstanceException noSuchServiceInstanceException) {
      returnValue = Response.status(Response.Status.GONE).entity(noSuchServiceInstanceException.getResponse()).build();
    }
    if (logger.isTraceEnabled()) {
      logger.trace("EXIT {}", returnValue);
    }
    return returnValue;
  }
  
}
