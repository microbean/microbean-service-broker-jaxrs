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
package org.microbean.servicebroker.jaxrs.jackson.command;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(content = JsonInclude.Include.NON_EMPTY, value = JsonInclude.Include.NON_EMPTY)
@JsonNaming(SnakeCaseStrategy.class)
@JsonPropertyOrder({ "instance_id", "service_id", "plan_id", "parameters", "accepts_incomplete", "organization_guid", "space_guid" })
abstract class ProvisionServiceInstanceCommandMixin {

  @JsonCreator
  ProvisionServiceInstanceCommandMixin(@JsonProperty("instance_id") final String instanceId,
                                       @JsonProperty("service_id") final String serviceId,
                                       @JsonProperty("plan_id") final String planId,
                                       @JsonProperty("parameters") final Map<? extends String, ? extends Object> parameters,
                                       @JsonProperty("accepts_incomplete") final boolean acceptsIncomplete,
                                       @JsonProperty("organization_guid") final String organizationGuid,
                                       @JsonProperty("space_guid") final String spaceGuid) {
    super();
  }

}
