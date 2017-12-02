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
import java.util.Set;

public final class Resources {

  private static final Set<Class<?>> rootResourceClasses;

  private static final Set<Class<?>> exceptionMapperClasses;

  private static final Set<Class<?>> allClasses;

  static {
    Set<Class<?>> classes = new HashSet<>();
    classes.add(CatalogResource.class);
    classes.add(ServiceInstancesResource.class);
    classes.add(ServiceBindingsResource.class);
    rootResourceClasses = Collections.unmodifiableSet(classes);

    classes = new HashSet<>();
    classes.add(BindingAlreadyExistsExceptionMapper.class);
    classes.add(ExceptionMapper.class);
    classes.add(InvalidServiceBrokerCommandExceptionMapper.class);
    classes.add(InvalidServiceBrokerQueryExceptionMapper.class);
    classes.add(NoSuchBindingExceptionMapper.class);
    classes.add(NoSuchServiceInstanceExceptionMapper.class);
    classes.add(ServiceInstanceAlreadyExistsExceptionMapper.class);
    exceptionMapperClasses = Collections.unmodifiableSet(classes);

    classes = new HashSet<>(rootResourceClasses);
    classes.addAll(exceptionMapperClasses);
    allClasses = Collections.unmodifiableSet(classes);
  }
  
  private Resources() {
    super();
  }

  public static final Set<Class<?>> getRootResourceClasses() {
    return rootResourceClasses;
  }

  public static final Set<Class<?>> getExceptionMapperClasses() {
    return exceptionMapperClasses;
  }

  public static final Set<Class<?>> getClasses() {
    return allClasses;
  }
  
}
