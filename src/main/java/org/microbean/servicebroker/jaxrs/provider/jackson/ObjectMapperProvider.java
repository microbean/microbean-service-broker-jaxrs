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

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.fasterxml.jackson.databind.introspect.ClassIntrospector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

  private final Logger logger;
  
  private final ObjectMapper objectMapper;
  
  public ObjectMapperProvider() {
    super();
    this.logger = LoggerFactory.getLogger(this.getClass());
    assert this.logger != null;
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    objectMapper.setMixInResolver(new MixinResolver());
    this.objectMapper = objectMapper;
  }

  @Override
  public final ObjectMapper getContext(final Class<?> type) {
    if (logger.isTraceEnabled()) {
      logger.trace("ENTRY {}", type);
      logger.trace("EXIT {}", this.objectMapper);
    }
    return this.objectMapper;
  }

  private static final class MixinResolver implements ClassIntrospector.MixInResolver {

    private final Logger logger;
    
    private MixinResolver() {
      super();
      this.logger = LoggerFactory.getLogger(this.getClass());
      assert this.logger != null;
    }

    @Override
    public final Class<?> findMixInClassFor(final Class<?> c) {
      if (logger.isTraceEnabled()) {
        logger.trace("ENTRY {}", c);
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
      if (logger.isTraceEnabled()) {
        logger.trace("EXIT {}", returnValue);
      }
      return returnValue;
    }

    @Override
    public final ClassIntrospector.MixInResolver copy() {
      return this; // we're immutable
    }
    
  }
  
}
