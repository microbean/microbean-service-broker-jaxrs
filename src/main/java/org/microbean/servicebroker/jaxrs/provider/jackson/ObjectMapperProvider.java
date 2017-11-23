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
package org.microbean.servicebroker.jaxrs.provider.jackson;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.fasterxml.jackson.databind.introspect.ClassIntrospector;

@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

  private final ObjectMapper objectMapper;
  
  public ObjectMapperProvider() {
    super();
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    objectMapper.setMixInResolver(new MixinResolver());
    this.objectMapper = objectMapper;
  }

  @Override
  public final ObjectMapper getContext(final Class<?> type) {
    final String cn = this.getClass().getName();
    final String mn = "getContext";
    final Logger logger = Logger.getLogger(cn);
    assert logger != null;
    if (logger.isLoggable(Level.FINER)) {
      logger.entering(cn, mn, type);
      logger.exiting(cn, mn, this.objectMapper);
    }
    return this.objectMapper;
  }

  private static final class MixinResolver implements ClassIntrospector.MixInResolver {
    
    private MixinResolver() {
      super();
    }

    @Override
    public final Class<?> findMixInClassFor(final Class<?> c) {
      final String cn = this.getClass().getName();
      final String mn = "findMixInClassFor";
      final Logger logger = Logger.getLogger(cn);
      assert logger != null;
      if (logger.isLoggable(Level.FINER)) {
        logger.entering(cn, mn, c);
      }
      Class<?> returnValue = null;
      if (c != null) {
        String className = c.getName();
        if (className.startsWith("org.microbean.servicebroker.api.")) {
          assert className.length() > "org.microbean.servicebroker.api.".length();
          final StringBuilder newClassName = new StringBuilder("org.microbean.servicebroker.jaxrs.jackson.");
          newClassName.append(className.substring("org.microbean.servicebroker.api.".length()));
          newClassName.append("Mixin");
          try {
            returnValue = Class.forName(newClassName.toString(), true, Thread.currentThread().getContextClassLoader());
          } catch (final ClassNotFoundException cnfe) {
            returnValue = null;
          }
        }
      }
      if (logger.isLoggable(Level.FINER)) {
        logger.exiting(cn, mn, returnValue);
      }
      return returnValue;
    }

    @Override
    public final ClassIntrospector.MixInResolver copy() {
      return this; // we're immutable
    }
    
  }
  
}
