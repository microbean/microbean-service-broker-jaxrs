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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;

import javax.ws.rs.ApplicationPath;

import org.microbean.servicebroker.jaxrs.provider.jackson.ObjectMapperProvider;

@ApplicationPath("/v2/")
@Singleton
public class Application extends javax.ws.rs.core.Application {

  private final Set<Class<?>> classes;
  
  public Application() {
    super();
    final Set<Class<?>> classes = this.createClasses();
    if (classes == null || classes.isEmpty()) {
      this.classes = Collections.emptySet();
    } else {
      this.classes = Collections.unmodifiableSet(new LinkedHashSet<>(classes));
    }
  }

  protected Set<Class<?>> createClasses() {
    final String cn = this.getClass().getName();
    final String mn = "createClasses";
    final Logger logger = Logger.getLogger(cn);
    assert logger != null;
    if (logger.isLoggable(Level.FINER)) {
      logger.entering(cn, mn);
    }

    final Set<Class<?>> classes = new HashSet<>();

    // Root resource classes
    classes.add(CatalogResource.class);
    classes.add(ServiceInstancesResource.class);
    classes.add(ServiceBindingsResource.class);

    // Providers
    // TODO: remove this
    classes.add(ObjectMapperProvider.class);
    
    final Set<Class<?>> returnValue = Collections.unmodifiableSet(classes);

    if (logger.isLoggable(Level.FINER)) {
      logger.exiting(cn, mn, returnValue);
    }
    return returnValue;
  }
  
  @Override
  public Set<Class<?>> getClasses() {
    return this.classes;
  }
  
}
