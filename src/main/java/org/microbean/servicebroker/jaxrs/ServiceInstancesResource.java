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

import java.util.Objects;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
// import javax.ws.rs.PATCH; // sigh
import javax.ws.rs.PUT;
import javax.ws.rs.Consumes;
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

import org.microbean.servicebroker.api.query.LastOperationQuery;
import org.microbean.servicebroker.api.query.state.LastOperation;

import org.microbean.servicebroker.api.command.DeleteServiceInstanceCommand;
import org.microbean.servicebroker.api.command.IdenticalServiceInstanceAlreadyExistsException;
import org.microbean.servicebroker.api.command.NoSuchServiceInstanceException;
import org.microbean.servicebroker.api.command.ProvisionServiceInstanceCommand;
import org.microbean.servicebroker.api.command.ServiceInstanceAlreadyExistsException;
import org.microbean.servicebroker.api.command.UpdateServiceInstanceCommand;

@Path("/service_instances")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ServiceInstancesResource {

  @Inject
  private ServiceBroker serviceBroker;

  public ServiceInstancesResource() {
    super();
  }

  @GET
  @Path("{instance_id}/last_operation")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response getLastOperation(@PathParam("instance_id") final String instanceId,
                                   @QueryParam("service_id") final String serviceId,
                                   @QueryParam("plan_id") final String planId,
                                   @QueryParam("operation") final String operationId)
    throws ServiceBrokerException {
    final String cn = this.getClass().getName();
    final String mn = "getLastOperation";
    final Logger logger = Logger.getLogger(cn);
    assert logger != null;
    if (logger.isLoggable(Level.FINER)) {
      logger.entering(cn, mn, new Object[] { instanceId, serviceId, planId, operationId });
    }
    Objects.requireNonNull(instanceId);

    // The specification implies but does not state that operation is required.
    // https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#parameters
    if (operationId == null) {
      throw new BadRequestException("The operation query parameter was not specified");
    }
    
    final Response returnValue;
    Response temp = null;
    final LastOperationQuery query = new LastOperationQuery(serviceId, instanceId, planId, operationId);
    try {
      final LastOperation lastOperation = this.serviceBroker.getLastOperation(query);
      if (lastOperation == null) {
        temp = Response.serverError().entity("{}").build();
      } else {
        temp = Response.ok().entity(lastOperation).build();
      }
    } catch (final NoSuchServiceInstanceException noSuchServiceInstanceException) {
      // See
      // https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#response-1
      temp = Response.status(410).entity("{}").build();
    } finally {
      returnValue = temp;
    }    
    
    if (logger.isLoggable(Level.FINER)) {
      logger.exiting(cn, mn, returnValue);
    }
    return returnValue;
  }
  
  @PUT
  @Path("{instance_id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response putServiceInstance(@PathParam("instance_id") final String instanceId,
                                     final ProvisionServiceInstanceCommand command,
                                     @QueryParam("accepts_incomplete") final boolean acceptsIncomplete)
    throws ServiceBrokerException {
    final String cn = this.getClass().getName();
    final String mn = "putServiceInstance";
    final Logger logger = Logger.getLogger(cn);
    assert logger != null;
    if (logger.isLoggable(Level.FINER)) {
      logger.entering(cn, mn, new Object[] { instanceId, command });
    }
    Objects.requireNonNull(instanceId);
    Objects.requireNonNull(command);

    command.setAcceptsIncomplete(acceptsIncomplete);
    
    if (command.getInstanceId() == null) {
      command.setInstanceId(instanceId);
    }

    final Response returnValue;
    if (!acceptsIncomplete && this.serviceBroker.isAsynchronousOnly()) {
      // See https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#asynchronous-operations
      returnValue = Response.status(422).entity("{\n" +
                                                "  \"error\": \"AsyncRequired\",\n" +
                                                "  \"description\": \"This service plan requires client support for asynchronous service operations.\"\n" +
                                                "}").build();
    } else {
      Response temp = null;
      try {
        final ProvisionServiceInstanceCommand.Response commandResponse = this.serviceBroker.execute(command);
        if (commandResponse == null) {
          temp = Response.serverError().entity("{}").build();
        } else if (commandResponse.getOperation() == null) {        
          // The specification mandates a 201 return code, but does not
          // say what the Location: header should contain, so we don't
          // return one.
          // See https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#response-2
          temp = Response.status(201).entity(commandResponse).build();
        } else {
          // The command response contained an operation property, so
          // that means it's asynchronous.
          // See https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#response-2
          temp = Response.status(202).entity(commandResponse).build();
        }
      } catch (final IdenticalServiceInstanceAlreadyExistsException identicalServiceInstanceAlreadyExistsException) {
        // See https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#response-2
        temp = Response.status(409).entity(identicalServiceInstanceAlreadyExistsException.getResponse()).build();
      } catch (final ServiceInstanceAlreadyExistsException serviceInstanceAlreadyExistsException) {
        // See https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#response-2
        temp = Response.status(200).entity(serviceInstanceAlreadyExistsException.getResponse()).build();
      } finally {
        returnValue = temp;
      }
    }

    if (logger.isLoggable(Level.FINER)) {
      logger.exiting(cn, mn, returnValue);
    }
    return returnValue;
  }

  @PATCH  
  @Path("{instance_id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateServiceInstance(@PathParam("instance_id") final String instanceId,
                                        final UpdateServiceInstanceCommand command,
                                        @QueryParam("accepts_incomplete") final boolean acceptsIncomplete)
    throws ServiceBrokerException {
    final String cn = this.getClass().getName();
    final String mn = "updateServiceInstance";
    final Logger logger = Logger.getLogger(cn);
    assert logger != null;
    if (logger.isLoggable(Level.FINER)) {
      logger.entering(cn, mn, new Object[] { instanceId, command, Boolean.valueOf(acceptsIncomplete) });
    }
    Objects.requireNonNull(instanceId);
    Objects.requireNonNull(command);

    command.setAcceptsIncomplete(acceptsIncomplete);
    
    if (command.getInstanceId() == null) {
      command.setInstanceId(instanceId);
    }    
    
    final Response returnValue;
    if (!acceptsIncomplete && this.serviceBroker.isAsynchronousOnly()) {
      // See https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#asynchronous-operations
      returnValue = Response.status(422).entity("{\n" +
                                                "  \"error\": \"AsyncRequired\",\n" +
                                                "  \"description\": \"This service plan requires client support for asynchronous service operations.\"\n" +
                                                "}").build();
    } else {
      Response temp = null;
      try {
        final UpdateServiceInstanceCommand.Response commandResponse = this.serviceBroker.execute(command);
        if (commandResponse == null) {
          temp = Response.serverError().entity("{}").build();
        } else if (commandResponse.getOperation() == null) {        
          // The specification mandates a 201 return code, but does not
          // say what the Location: header should contain, so we don't
          // return one.
          // See https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#response-2
          temp = Response.status(201).entity(commandResponse).build();
        } else {
          // The command response contained an operation property, so
          // that means it's asynchronous.
          // See https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#response-2
          temp = Response.status(202).entity(commandResponse).build();
        }
      } catch (final ServiceInstanceAlreadyExistsException serviceInstanceAlreadyExistsException) {
        // See https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#response-2
        temp = Response.status(200).entity(serviceInstanceAlreadyExistsException.getResponse()).build();
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
  @Path("{instance_id}")
  @Consumes(MediaType.WILDCARD)
  public Response deleteServiceInstance(@PathParam("instance_id") final String instanceId,
                                        @QueryParam("service_id") final String serviceId,
                                        @QueryParam("plan_id") final String planId,
                                        @QueryParam("accepts_incomplete") final boolean acceptsIncomplete)
    throws ServiceBrokerException {
    final String cn = this.getClass().getName();
    final String mn = "deleteServiceInstance";
    final Logger logger = Logger.getLogger(cn);
    assert logger != null;
    if (logger.isLoggable(Level.FINER)) {
      logger.entering(cn, mn, new Object[] { instanceId, serviceId, planId, acceptsIncomplete });
    }
    Objects.requireNonNull(instanceId);
    Objects.requireNonNull(serviceId);
    Objects.requireNonNull(planId);

    final Response returnValue;
    if (!acceptsIncomplete && this.serviceBroker.isAsynchronousOnly()) {
      // See https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#asynchronous-operations
      returnValue = Response.status(422).entity("{\n" +
                                                "  \"error\": \"AsyncRequired\",\n" +
                                                "  \"description\": \"This service plan requires client support for asynchronous service operations.\"\n" +
                                                "}").build();
    } else {
      final DeleteServiceInstanceCommand command = new DeleteServiceInstanceCommand(instanceId, serviceId, planId, acceptsIncomplete);
      Response temp = null;
      try {
        final DeleteServiceInstanceCommand.Response commandResponse = this.serviceBroker.execute(command);
        if (commandResponse == null) {
          temp = Response.serverError().entity("{}").build();
        } else {
          temp = Response.ok(commandResponse).build();
        }
      } catch (final NoSuchServiceInstanceException noSuchServiceInstanceException) {
        temp = Response.status(Response.Status.GONE).entity(noSuchServiceInstanceException.getResponse()).build();
      } finally {
        returnValue = temp;
      }
    }

    if (logger.isLoggable(Level.FINER)) {
      logger.exiting(cn, mn, returnValue);
    }
    return returnValue;
  }

  @javax.ws.rs.HttpMethod("PATCH")
  private @interface PATCH {

  }
  
}
