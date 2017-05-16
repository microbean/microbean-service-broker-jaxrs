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

import org.microbean.servicebroker.api.command.DeleteBindingCommand;
import org.microbean.servicebroker.api.command.ProvisionBindingCommand;

import org.microbean.servicebroker.api.query.state.Binding;

@ApplicationScoped
@Path("/service_instances/{instance_id}/service_bindings")
@Produces(MediaType.APPLICATION_JSON)
public class ServiceBindingsResource {

  @Inject
  private ServiceBroker serviceBroker;
  
  public ServiceBindingsResource() {
    super();
  }

  @GET
  @Path("{binding_id}")
  public Binding getServiceBinding(@PathParam("instance_id") final String instanceId,
                                   @PathParam("binding_id") final String bindingId,
                                   @Context final UriInfo uriInfo)
    throws ServiceBrokerException {
    Objects.requireNonNull(instanceId);
    Objects.requireNonNull(bindingId);
    return this.serviceBroker.getBinding(instanceId, bindingId);    
  }

  @PUT
  @Path("{binding_id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response putServiceBinding(@PathParam("instance_id") final String instanceId,
                                    @PathParam("binding_id") final String bindingId,
                                    final ProvisionBindingCommand command,
                                    @Context final UriInfo uriInfo)
    throws ServiceBrokerException {
    Objects.requireNonNull(instanceId);
    Objects.requireNonNull(bindingId);
    Objects.requireNonNull(command);
    Objects.requireNonNull(uriInfo);
    if (command.getServiceInstanceId() == null) {
      command.setServiceInstanceId(instanceId);
    }
    if (command.getBindingInstanceId() == null) {
      command.setBindingInstanceId(bindingId);
    }
    Response returnValue = null;
    try {
      final ProvisionBindingCommand.Response commandResponse = this.serviceBroker.execute(command);
      returnValue = Response.created(uriInfo.getAbsolutePath()).entity(commandResponse).build();
    } catch (final ServiceBrokerException serviceBrokerException) {
      throw serviceBrokerException;
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
    Objects.requireNonNull(instanceId);
    Objects.requireNonNull(bindingId);
    Objects.requireNonNull(serviceId);
    Objects.requireNonNull(planId);

    Response returnValue = null;
    final DeleteBindingCommand command = new DeleteBindingCommand(instanceId, bindingId, serviceId, planId);
    try {
      final DeleteBindingCommand.Response commandResponse = this.serviceBroker.execute(command);
      returnValue = Response.ok(commandResponse).build();
    } catch (final ServiceBrokerException serviceBrokerException) {
      throw serviceBrokerException;
    }
    return returnValue;
  }
  
}