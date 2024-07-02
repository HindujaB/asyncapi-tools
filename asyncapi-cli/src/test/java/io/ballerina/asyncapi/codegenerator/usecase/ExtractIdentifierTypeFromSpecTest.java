/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.asyncapi.codegenerator.usecase;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.models.asyncapi.AsyncApiDocument;
import io.ballerina.asyncapi.codegenerator.configuration.BallerinaAsyncApiException;
import io.ballerina.asyncapi.codegenerator.repository.FileRepository;
import io.ballerina.asyncapi.codegenerator.repository.FileRepositoryImpl;
import org.testng.annotations.Test;

/**
 * Test the extraction of identifier type from the AsyncAPI specification.
 */
public class ExtractIdentifierTypeFromSpecTest {
    FileRepository fileRepository = new FileRepositoryImpl();

    @Test(
            description = "Test the functionality of the extract function when the Async API spec does not contains " +
                    "the x-ballerina-identifier-path attribute in the channel",
            expectedExceptions = BallerinaAsyncApiException.class,
            expectedExceptionsMessageRegExp = "x-ballerina-event-identifier attribute " +
                    "is not found in the Async API Specification"
    )
    public void testExtractWithoutIdentifierPath() throws BallerinaAsyncApiException {
        String asyncApiSpecStr = fileRepository
                .getFileContentFromResources("specs/spec-without-identifier-path.yml");
        String asyncApiSpecJson = fileRepository.convertYamlToJson(asyncApiSpecStr);
        AsyncApiDocument asyncApiSpec = (AsyncApiDocument) Library.readDocumentFromJSONString(asyncApiSpecJson);
        Extractor extractIdentifierTypeFromSpec = new ExtractIdentifierTypeFromSpec(asyncApiSpec);
        extractIdentifierTypeFromSpec.extract();
    }

    @Test(
            description = "Test the functionality of the extract function " +
                    "when the Async API spec contains the x-ballerina-identifier-path attribute in the channel " +
                    "but missing the type attribute inside it",
            expectedExceptions = BallerinaAsyncApiException.class,
            expectedExceptionsMessageRegExp = "type attribute is not found within the attribute " +
                    "x-ballerina-event-identifier in the Async API Specification"
    )
    public void testExtractWithIdentifierPathMissingType() throws BallerinaAsyncApiException {
        String asyncApiSpecStr = fileRepository
                .getFileContentFromResources("specs/spec-with-identifier-path-missing-type.yml");
        String asyncApiSpecJson = fileRepository.convertYamlToJson(asyncApiSpecStr);
        AsyncApiDocument asyncApiSpec = (AsyncApiDocument) Library.readDocumentFromJSONString(asyncApiSpecJson);
        Extractor extractIdentifierTypeFromSpec = new ExtractIdentifierTypeFromSpec(asyncApiSpec);
        extractIdentifierTypeFromSpec.extract();
    }

    @Test(
            description = "Test the functionality of the extract function " +
                    "when the Async API spec contains the x-ballerina-event-identifier attribute in the channel " +
                    "but invalid type attribute value",
            expectedExceptions = BallerinaAsyncApiException.class,
            expectedExceptionsMessageRegExp = "header or body is not provided as the value of type attribute within " +
                    "the attribute x-ballerina-event-identifier in the Async API Specification"
    )
    public void testExtractWithIdentifierPathInvalidType() throws BallerinaAsyncApiException {
        String asyncApiSpecStr = fileRepository
                .getFileContentFromResources("specs/spec-with-event-identifier-invalid-type.yml");
        String asyncApiSpecJson = fileRepository.convertYamlToJson(asyncApiSpecStr);
        AsyncApiDocument asyncApiSpec = (AsyncApiDocument) Library.readDocumentFromJSONString(asyncApiSpecJson);
        Extractor extractIdentifierTypeFromSpec = new ExtractIdentifierTypeFromSpec(asyncApiSpec);
        extractIdentifierTypeFromSpec.extract();
    }
}
