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
package io.ballerina.asyncapi.websocketscore.generators.asyncspec.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

import static io.ballerina.asyncapi.websocketscore.generators.asyncspec.Constants.JSON_EXTENSION;
import static io.ballerina.asyncapi.websocketscore.generators.asyncspec.Constants.YAML_EXTENSION;

/**
 * Utilities used by ballerina asyncapi code generator.
 *
 */
public final class CodegenUtils {

    /**
     * Writes a file with content to specified {@code filePath}.
     *
     * @param filePath valid file path to write the content
     * @param content  content of the file
     * @throws IOException when a file operation fails
     */
    public static void writeFile(Path filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath.toString(), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }

    /**
     * This method use for checking the duplicate files.
     *
     * @param outPath      output path for file generated
     * @param asyncApiName given file name
     * @return file name with duplicate number tag
     */
    public static String resolveContractFileName(Path outPath, String asyncApiName, Boolean isJson) {
        if (outPath != null && Files.exists(outPath)) {
            final File[] listFiles = new File(String.valueOf(outPath)).listFiles();
            if (listFiles != null) {
                asyncApiName = checkAvailabilityOfGivenName(asyncApiName, listFiles, isJson);
            }
        }
        return asyncApiName;
    }

    private static String checkAvailabilityOfGivenName(String asyncApiName, File[] listFiles, Boolean isJson) {
        for (File file : listFiles) {
            if (System.console() != null && file.getName().equals(asyncApiName)) {
                String userInput = System.console().readLine("There is already a file named ' " + file.getName() +
                        "' in the target location. Do you want to overwrite the file? [y/N] ");
                if (!Objects.equals(userInput.toLowerCase(Locale.ENGLISH), "y")) {
                    asyncApiName = setGeneratedFileName(listFiles, asyncApiName, isJson);
                }
            }
        }
        return asyncApiName;
    }

    /**
     * This method for setting the file name for generated file.
     *
     * @param listFiles generated files
     * @param fileName  File name
     */
    private static String setGeneratedFileName(File[] listFiles, String fileName, boolean isJson) {
        int duplicateCount = 0;
        for (File listFile : listFiles) {
            String listFileName = listFile.getName();
            if (listFileName.contains(".") && ((listFileName.split("\\.")).length >= 2)
                    && (listFileName.split("\\.")[0]
                    .equals(fileName.split("\\.")[0]))) {
                duplicateCount++;
            }
        }
        if (isJson) {
            return fileName.split("\\.")[0] + "." + duplicateCount + JSON_EXTENSION;
        }
        return fileName.split("\\.")[0] + "." + duplicateCount + YAML_EXTENSION;
    }
}
