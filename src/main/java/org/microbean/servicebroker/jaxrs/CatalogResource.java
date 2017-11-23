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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;

import org.microbean.servicebroker.api.ServiceBroker;
import org.microbean.servicebroker.api.ServiceBrokerException;

import org.microbean.servicebroker.api.query.state.Catalog;

@Path("/catalog")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class CatalogResource {

  @Inject
  private ServiceBroker serviceBroker;
  
  public CatalogResource() {
    super();
  }

  @GET
  public Catalog getCatalog() throws ServiceBrokerException {
    final String cn = this.getClass().getName();
    final String mn = "getCatalog";
    final Logger logger = Logger.getLogger(cn);
    assert logger != null;
    if (logger.isLoggable(Level.FINER)) {
      logger.entering(cn, mn);
    }
    
    assert this.serviceBroker != null;
    Catalog returnValue = this.serviceBroker.getCatalog();
    if (returnValue == null) {
      returnValue = new Catalog();
    }

    if (logger.isLoggable(Level.FINER)) {
      logger.exiting(cn, mn, returnValue);
    }
    return returnValue;
  }
  
}
