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
package io.ballerina.asyncapi.cmd;

import static io.ballerina.asyncapi.cmd.AsyncApiConstants.CLIENT;
import static io.ballerina.asyncapi.cmd.AsyncApiConstants.JSON_FLAG;
import static io.ballerina.asyncapi.cmd.AsyncApiConstants.LICENSE_FLAG;
import static io.ballerina.asyncapi.cmd.AsyncApiConstants.SERVICE_FLAG;
import static io.ballerina.asyncapi.cmd.AsyncApiConstants.SPEC;
import static io.ballerina.asyncapi.cmd.AsyncApiConstants.TEST_FLAG;

/**
 * This class contains the messages constants required for AsyncApi tool.
 */
public class AsyncApiMessages {
    public static final String MESSAGE_FOR_MISSING_INPUT = "An AsyncApi definition file is required to generate the " +
            "listener. \ne.g: bal asyncapi --input <AsyncAPIContract>";
    public static final String CLIENT_GENERATION_FAILED = "Error occurred when generating client for AsyncAPI contract";
    public static final String MISSING_CONTRACT_PATH = "Bal service file is required to generate the " +
            "asyncapi definition. \ne.g: bal asyncapi --input <Ballerina file path>";
    public static final String MESSAGE_INVALID_PROTOCOL = "ERROR invalid protocol: %s. Supported protocols are" +
            " `http` and `ws`.";
    public static final String INVALID_OPTION_ERROR_HTTP = "ERROR unsupported %s flag for http protocol";
    public static final String INVALID_OPTION_WARNING = "WARNING the `%s` option is invalid for generating" +
            " %s files and will be ignored.";
    public static final String INVALID_USE_OF_LICENSE_FLAG_WARNING = String.format(INVALID_OPTION_WARNING, LICENSE_FLAG,
            SPEC);
    public static final String INVALID_USE_OF_TEST_FLAG_WARNING = String.format(INVALID_OPTION_WARNING, TEST_FLAG,
            SPEC);
    public static final String INVALID_USE_OF_JSON_FLAG_WARNING = String.format(INVALID_OPTION_WARNING, JSON_FLAG,
            CLIENT);
    public static final String INVALID_USE_OF_SERVICE_FLAG_WARNING = String.format(INVALID_OPTION_WARNING, SERVICE_FLAG,
            CLIENT);
    public static final String MESSAGE_INVALID_LICENSE_STREAM = "Invalid license file path : %s. %s.";
}
