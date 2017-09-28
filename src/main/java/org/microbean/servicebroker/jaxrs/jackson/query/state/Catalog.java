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
package org.microbean.servicebroker.jaxrs.jackson.query.state;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

public abstract class Catalog {

  @JsonInclude(content = JsonInclude.Include.NON_EMPTY, value = JsonInclude.Include.NON_EMPTY)
  @JsonNaming(SnakeCaseStrategy.class)
  @JsonPropertyOrder({ "name", "id", "description", "tags", "requires", "bindable", "metadata", "dashboard_client", "plan_updateable", "plans" })
  public static abstract class ServiceMixin {
    
    @JsonProperty("plan_updateable")
    public abstract boolean isPlanUpdatable();

  }

  public static abstract class Service {
    
    @JsonInclude(content = JsonInclude.Include.NON_EMPTY, value = JsonInclude.Include.NON_EMPTY)
    @JsonNaming(SnakeCaseStrategy.class)
    @JsonPropertyOrder({ "id", "secret", "redirect_uri" })
    public abstract class DashboardClientMixin {
      
      @JsonProperty("redirect_uri")
      public abstract URI getRedirectUri();
      
    }
    
    @JsonInclude(content = JsonInclude.Include.NON_EMPTY, value = JsonInclude.Include.NON_EMPTY)
    @JsonNaming(SnakeCaseStrategy.class)
    @JsonPropertyOrder({ "id", "name", "description", "metadata", "free", "bindable" })
    public static abstract class PlanMixin {

    }
    
  }
  
}
