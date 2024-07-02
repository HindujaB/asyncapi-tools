/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.asyncapi.codegenerator.controller;

import io.apicurio.datamodels.models.asyncapi.AsyncApiSchema;
import io.ballerina.asyncapi.codegenerator.entity.ServiceType;

import java.util.List;
import java.util.Map;

/**
 * This is the Interface used for entity extraction from the AsyncAPI specification related Controllers.
 */
public interface SpecController {
    List<ServiceType> getServiceTypes();
    Map<String, AsyncApiSchema> getSchemas();
    String getEventIdentifierType();
    String getEventIdentifierPath();
}
