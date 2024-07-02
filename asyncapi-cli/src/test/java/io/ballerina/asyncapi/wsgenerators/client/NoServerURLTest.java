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
package io.ballerina.asyncapi.wsgenerators.client;

import io.apicurio.datamodels.models.asyncapi.v25.AsyncApi25DocumentImpl;
import io.ballerina.asyncapi.websocketscore.GeneratorUtils;
import io.ballerina.asyncapi.websocketscore.exception.BallerinaAsyncApiExceptionWs;
import io.ballerina.asyncapi.websocketscore.generators.client.IntermediateClientGenerator;
import io.ballerina.asyncapi.websocketscore.generators.client.model.AasClientConfig;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.asyncapi.wsgenerators.common.TestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;


/**
 * Test client generation when server url is not given in the async-api definition.
 */
public class NoServerURLTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/websockets" +
            "/asyncapi-to-ballerina/client").toAbsolutePath();
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    private SyntaxTree syntaxTree;

    @Test(description = "Test for no server url with no security schema given")
    public void getClientForNoServerURL() throws IOException, BallerinaAsyncApiExceptionWs {
        Path definitionPath = RES_DIR.resolve("NoServerUrl/missing_server_url.yaml");
        Path expectedPath = RES_DIR.resolve("baloutputs/NoServerUrl/missing_server_url.bal");

        AsyncApi25DocumentImpl asyncAPI = GeneratorUtils.normalizeAsyncAPI(definitionPath);
        AasClientConfig.Builder clientMetaDataBuilder = new AasClientConfig.Builder();
        AasClientConfig aasClientConfig = clientMetaDataBuilder
                .withAsyncApi(asyncAPI).build();
        IntermediateClientGenerator intermediateClientGenerator = new IntermediateClientGenerator(aasClientConfig);
        syntaxTree = intermediateClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }
//TODO: Uncomment after adding authentication mechanisms

//    @Test(description = "Test for no server url with HTTP authentication mechanism")
//    public void getClientForNoServerURLWithHTTPAuth() {
//        BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator = new BallerinaAuthConfigGenerator(
//                false, true);
//        String expectedParams = TestConstants.WEBSOCKET_CLIENT_CONFIG_PARAM_NO_URL;
//        StringBuilder generatedParams = new StringBuilder();
//        List<Node> generatedInitParamNodes = ballerinaAuthConfigGenerator.getConfigParamForClassInit(
//                "/");
//        for (Node param: generatedInitParamNodes) {
//            generatedParams.append(param.toString());
//        }
//        expectedParams = (expectedParams.trim()).replaceAll("\\s+", "");
//        String generatedParamsStr = (generatedParams.toString().trim()).replaceAll("\\s+", "");
//        Assert.assertEquals(expectedParams, generatedParamsStr);
//    }
//
//    @Test(description = "Test for no server url with API key authentication mechanism")
//    public void getClientForNoServerURLWithAPIKeyAuth() {
//        BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator = new BallerinaAuthConfigGenerator(
//                true, false);
//        String expectedParams = TestConstants.API_KEY_CONFIG_PARAM_NO_URL;
//        StringBuilder generatedParams = new StringBuilder();
//        List<Node> generatedInitParamNodes = ballerinaAuthConfigGenerator.getConfigParamForClassInit(
//                "/");
//        for (Node param: generatedInitParamNodes) {
//            generatedParams.append(param.toString());
//        }
//        expectedParams = (expectedParams.trim()).replaceAll("\\s+", "");
//        String generatedParamsStr = (generatedParams.toString().trim()).replaceAll("\\s+", "");
//        Assert.assertEquals(expectedParams, generatedParamsStr);
//    }
}
