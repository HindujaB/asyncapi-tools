/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package io.ballerina.asyncapi.websocketscore.generators.asyncspec.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.apicurio.datamodels.models.union.BooleanUnionValueImpl;

/**
 * This {@code BalBooleanSchema} contains details related to AsyncApi Boolean schema.
 * In Apicurio data model ,they are using BooleanSchemaUnion but boolean: true and value: true fields need to be ignored
 * therefore those defined as @JsonIgnore
 */
public class BalBooleanSchema extends BooleanUnionValueImpl {

    public BalBooleanSchema(Boolean value) {
        super(value);
    }

    @JsonIgnore
    @Override
    public boolean isBoolean() {
        return true;
    }

    @JsonIgnore
    @Override
    public Boolean getValue() {
        return true;
    }

}
