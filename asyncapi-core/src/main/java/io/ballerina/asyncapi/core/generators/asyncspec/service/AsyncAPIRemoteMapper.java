/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package io.ballerina.asyncapi.core.generators.asyncspec.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.apicurio.datamodels.models.Operation;
import io.apicurio.datamodels.models.asyncapi.v25.*;
import io.ballerina.asyncapi.core.generators.asyncspec.diagnostic.AsyncAPIConverterDiagnostic;
import io.ballerina.asyncapi.core.generators.asyncspec.utils.ConverterCommonUtils;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.TypeBuilder;
import io.ballerina.compiler.api.symbols.*;
import io.ballerina.compiler.syntax.tree.*;

import java.util.*;
import java.util.function.Function;

import static io.ballerina.asyncapi.core.generators.asyncspec.Constants.*;


/**
 * This class will do resource mapping from ballerina to openApi.
 *
 * @since 2.0.0
 */
public class AsyncAPIRemoteMapper {
    private final SemanticModel semanticModel;
    private final  AsyncApi25ChannelsImpl pathObject = new AsyncApi25ChannelsImpl();
    private final AsyncApi25ComponentsImpl components = new AsyncApi25ComponentsImpl();

    private final AsyncAPIComponentMapper componentMapper=new AsyncAPIComponentMapper(components);
    private final List<AsyncAPIConverterDiagnostic> errors;

    public List<AsyncAPIConverterDiagnostic> getErrors() {
        return errors;
    }

    /**
     * Initializes a resource parser for openApi.
     */
    AsyncAPIRemoteMapper(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
        this.errors = new ArrayList<>();
    }

    public AsyncApi25ComponentsImpl getComponents() {
        return components;
    }
    /**
     * This method will convert ballerina resource to openApi Paths objects.
     *
//     * @param resources Resource list to be converted.
     * @return map of string and openApi path objects.
     */
    public AsyncApi25ChannelsImpl getChannels(FunctionDefinitionNode resource,List<ClassDefinitionNode> classDefinitionNodes) {
//        for (FunctionDefinitionNode resource : resources) {
//            List<String> methods = this.getHttpMethods(resource);
        String serviceClassName= getServiceClassName(resource);


        if (!serviceClassName.isEmpty()){

            for (ClassDefinitionNode node: classDefinitionNodes) {
//               Optional<Symbol> clasnode= semanticModel.symbol(node);
               String testClassName1= node.className().text();

                if (testClassName1.equals(serviceClassName)) {
                    return getRemotePath(resource,node);
                }
            }

        }

        return pathObject;
    }

    /**
     * Resource mapper when a resource has more than 1 http method.
     *
     * @param resource The ballerina resource.
//     * @param httpMethods   Sibling methods related to operation.
     */
    private AsyncApi25ChannelsImpl getRemotePath(FunctionDefinitionNode resource,ClassDefinitionNode classDefinitionNode) {
        String path = ConverterCommonUtils.unescapeIdentifier(generateRelativePath(resource));
        NodeList<Node> classMethodNodes= classDefinitionNode.members();
        AsyncApi25OperationImpl publishOperationItem=new AsyncApi25OperationImpl();
        AsyncApi25OperationImpl subscribeOperationItem=new AsyncApi25OperationImpl();
        AsyncApi25MessageImpl subscribeMessage= new AsyncApi25MessageImpl();
        AsyncApi25MessageImpl publishMessage= new AsyncApi25MessageImpl();
        for(Node node: classMethodNodes){
            if (node.kind().equals(SyntaxKind.OBJECT_METHOD_DEFINITION)){
                FunctionDefinitionNode remoteFunctionNode= (FunctionDefinitionNode)node;
                String functionName= remoteFunctionNode.functionName().toString();
                if (functionName.startsWith(ON)){
                    //TODO : have to pass this through unescape identifier
                    String remoteRequestTypeName=functionName.substring(2);
                    SimpleNameReferenceNode simpleNameReferenceNode=checkParameterContainsCustomType(remoteRequestTypeName,remoteFunctionNode);
                    if (simpleNameReferenceNode!=null){
                        TypeSymbol typeSymbol = (TypeSymbol) semanticModel.symbol(simpleNameReferenceNode).orElseThrow();
                        AsyncApi25MessageImpl publishOneOf=new AsyncApi25MessageImpl();
                        AsyncApi25MessageImpl componentMessage = extractMessageSchemaReference(publishMessage, remoteRequestTypeName, typeSymbol, publishOneOf);

                        ReturnTypeDescriptorNode remoteReturnNode = remoteFunctionNode.functionSignature().returnTypeDesc().get();
                        if (remoteReturnNode!=null) {
                            Node remoteReturnType = remoteReturnNode.type();
                            AsyncApi25MessageImpl subscribeOneOf = new AsyncApi25MessageImpl();
                            if (remoteReturnType.kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
                                TypeSymbol returnTypeSymbol = (TypeSymbol) semanticModel.symbol(remoteReturnType).orElseThrow();
                                String remoteReturnTypeName = remoteReturnType.toString();
                                //Creating return type message reference
                                AsyncApi25MessageImpl componentReturnMessage= extractMessageSchemaReference(subscribeMessage, remoteReturnTypeName, returnTypeSymbol, subscribeOneOf);
                                components.addMessage(remoteRequestTypeName, componentReturnMessage);
                                ObjectNode messageRefObject = new ObjectNode(JsonNodeFactory.instance);
                                messageRefObject.put($REF, MESSAGE_REFERENCE + ConverterCommonUtils.unescapeIdentifier(remoteReturnTypeName));
                                componentMessage.addExtension(X_RESPONSE, messageRefObject);
                                componentMessage.addExtension(X_RESPONSE_TYPE, new TextNode(SIMPLE_RPC));
                            } else if (remoteReturnType.kind().equals(SyntaxKind.STREAM_TYPE_DESC)) {
                                String remoteReturnstreamTypeString= ((StreamTypeParamsNode)((StreamTypeDescriptorNode)remoteReturnType).streamTypeParamsNode().get()).leftTypeDescNode().toString();

                                setSubscribeResponse(subscribeMessage, componentMessage, subscribeOneOf,remoteReturnstreamTypeString, STREAMING);

                            }else if(remoteReturnType instanceof BuiltinSimpleNameReferenceNode){
                                String remoteReturnTypeString= ConverterCommonUtils.unescapeIdentifier(remoteReturnType.toString());

                                setSubscribeResponse(subscribeMessage, componentMessage, subscribeOneOf, remoteReturnTypeString, SIMPLE_RPC);
                            }
                            components.addMessage(remoteRequestTypeName, componentMessage);
                            //TODO : Handle if remote method signature doesn't contain method name
                        }else{

                        }


                    }

                }

            }

        }
        publishOperationItem.setMessage(publishMessage);
        subscribeOperationItem.setMessage(subscribeMessage);
        AsyncApi25ChannelItemImpl channelItem= (AsyncApi25ChannelItemImpl) pathObject.createChannelItem();
        channelItem.setSubscribe(subscribeOperationItem);
        channelItem.setPublish(publishOperationItem);
        Map<String, String> apiDocs = listAPIDocumentations(resource,channelItem);
        pathObject.addItem(path,channelItem);
        AsyncAPIParameterMapper asyncAPIParameterMapper = new AsyncAPIParameterMapper(resource,apiDocs, components,
                semanticModel);
        asyncAPIParameterMapper.getResourceInputs(channelItem,components,semanticModel);
        return pathObject;

//        Optional<OperationAdaptor> operationAdaptor = convertRemoteToOperation(resource, path);
//        if (operationAdaptor.isPresent()) {
//            operation = operationAdaptor.get().getOperation();
//            generatePathItem(httpMethod, pathObject, operation, path);
//        } else {
//            break;
//        }
    }

    private void setSubscribeResponse(AsyncApi25MessageImpl subscribeMessage, AsyncApi25MessageImpl componentMessage, AsyncApi25MessageImpl subscribeOneOf, String streamType, String responseType) {
        ObjectNode typeMessageObject =new ObjectNode(JsonNodeFactory.instance);
        typeMessageObject.put(TYPE,streamType);
        subscribeOneOf.setPayload(typeMessageObject );
        subscribeMessage.addOneOf(subscribeOneOf);
        ObjectNode typeObject = new ObjectNode(JsonNodeFactory.instance);
        typeObject.put(TYPE,streamType);
        ObjectNode payloadObject= new ObjectNode(JsonNodeFactory.instance);
        payloadObject.put(PAYLOAD,typeObject);
        componentMessage.addExtension(X_RESPONSE, payloadObject);
        componentMessage.addExtension(X_RESPONSE_TYPE, new TextNode(responseType));
    }

    private AsyncApi25MessageImpl extractMessageSchemaReference(AsyncApi25MessageImpl publishMessage, String typeName, TypeSymbol typeSymbol, AsyncApi25MessageImpl messageType) {
        messageType.set$ref(MESSAGE_REFERENCE+ ConverterCommonUtils.unescapeIdentifier(typeName));
        publishMessage.addOneOf(messageType);
        AsyncApi25MessageImpl componentMessage= (AsyncApi25MessageImpl) components.createMessage();
        ObjectNode objNode1=new ObjectNode(JsonNodeFactory.instance);
        objNode1.put($REF,SCHEMA_REFERENCE+ ConverterCommonUtils.unescapeIdentifier(typeName));
        componentMessage.setPayload(objNode1);
        componentMapper.createComponentSchema( typeSymbol);
        return componentMessage;
    }

    private SimpleNameReferenceNode checkParameterContainsCustomType(String customTypeName,FunctionDefinitionNode remoteFunctionNode) {
        SeparatedNodeList<ParameterNode> remoteParameters = remoteFunctionNode.functionSignature().parameters();
        for (ParameterNode remoteParameterNode : remoteParameters) {
            if (remoteParameterNode.kind() == SyntaxKind.REQUIRED_PARAM) {
                RequiredParameterNode requiredParameterNode = (RequiredParameterNode) remoteParameterNode;
                Node parameterTypeNode =requiredParameterNode.typeName();
                SyntaxKind syntaxKind = requiredParameterNode.typeName().kind();
//                if (parameterTypeNode.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
//                    QualifiedNameReferenceNode referenceNode = (QualifiedNameReferenceNode) parameterTypeNode;
//                    String typeName = (referenceNode).modulePrefix().text() + ":" + (referenceNode).identifier().text();
                if (parameterTypeNode.kind()==SyntaxKind.SIMPLE_NAME_REFERENCE) {
                    SimpleNameReferenceNode simpleNameReferenceNode=(SimpleNameReferenceNode) parameterTypeNode;
                    String simpleType= simpleNameReferenceNode.name().toString().trim();
                    if (simpleType.equals(customTypeName)) {
                        return simpleNameReferenceNode;
                    }
                }
            }
        }
        return null;
    }
//                    if (typeName.equals(WEBSOCKET_CALLER) {
//                        RequestBody requestBody = new RequestBody();
//                        MediaType mediaType = new MediaType();
//                        mediaType.setSchema(new Schema<>().description(WILD_CARD_SUMMARY));
//                        requestBody.setContent(new Content().addMediaType(WILD_CARD_CONTENT_KEY, mediaType));
//                        operationAdaptor.getOperation().setRequestBody(requestBody);
//                    }

//                }



    private String getServiceClassName(FunctionDefinitionNode resource) {
        String serviceClassName="";
        FunctionBodyNode functionBodyNode = resource.functionBody();
        ChildNodeList childNodeList = functionBodyNode.children();
        for (Node node : childNodeList) {
            if (node instanceof ReturnStatementNode) {
                ReturnStatementNode returnStatementNode = (ReturnStatementNode) node;
                Optional<ExpressionNode> expression = returnStatementNode.expression();
                if (expression.get() instanceof ExplicitNewExpressionNode) {
                    ExplicitNewExpressionNode explicitNewExpressionNode = (ExplicitNewExpressionNode) expression.get();
                    TypeDescriptorNode typeDescriptorNode = explicitNewExpressionNode.typeDescriptor();
                    serviceClassName=typeDescriptorNode.toString();
                }
            }

        }
        return serviceClassName;
    }

//    private void generatePathItem(String httpMethod, Paths path, Operation operation, String pathName) {
//        PathItem pathItem = new PathIte();
//        switch (httpMethod.trim().toUpperCase(Locale.ENGLISH)) {
//            case Constants.GET:
//                if (pathObject.containsKey(pathName)) {
//                    pathObject.get(pathName).setGet(operation);
//                } else {
//                    pathItem.setGet(operation);
//                    path.addPathItem(pathName, pathItem);
//                }
//                break;
//            case Constants.PUT:
//                if (pathObject.containsKey(pathName)) {
//                    pathObject.get(pathName).setPut(operation);
//                } else {
//                    pathItem.setPut(operation);
//                    path.addPathItem(pathName, pathItem);
//                }
//                break;
//            case Constants.POST:
//                if (pathObject.containsKey(pathName)) {
//                    pathObject.get(pathName).setPost(operation);
//                } else {
//                    pathItem.setPost(operation);
//                    path.addPathItem(pathName, pathItem);
//                }
//                break;
//            case Constants.DELETE:
//                if (pathObject.containsKey(pathName)) {
//                    pathObject.get(pathName).setDelete(operation);
//                } else {
//                    pathItem.setDelete(operation);
//                    path.addPathItem(pathName, pathItem);
//                }
//                break;
//            case Constants.OPTIONS:
//                if (pathObject.containsKey(pathName)) {
//                    pathObject.get(pathName).setOptions(operation);
//                } else {
//                    pathItem.setOptions(operation);
//                    path.addPathItem(pathName, pathItem);
//                }
//                break;
//            case Constants.PATCH:
//                if (pathObject.containsKey(pathName)) {
//                    pathObject.get(pathName).setPatch(operation);
//                } else {
//                    pathItem.setPatch(operation);
//                    path.addPathItem(pathName, pathItem);
//                }
//                break;
//            case Constants.HEAD:
//                if (pathObject.containsKey(pathName)) {
//                    pathObject.get(pathName).setHead(operation);
//                } else {
//                    pathItem.setHead(operation);
//                    path.addPathItem(pathName, pathItem);
//                }
//                break;
//            default:
//                break;
//        }
//    }

                /**
                 * This method will convert ballerina @Resource to ballerina @OperationAdaptor.
                 *
                 * @return Operation Adaptor object of given resource
                 */
//    private Optional<OperationAdaptor> convertRemoteToOperation(FunctionDefinitionNode resource, String httpMethod,
//                                                                String generateRelativePath) {
//        OperationAdaptor op = new OperationAdaptor();
//        op.setHttpOperation(httpMethod);
//        op.setPath(generateRelativePath);
//        /* Set operation id */
//        String resName = (resource.functionName().text() + "_" +
//                generateRelativePath).replaceAll("\\{///\\}", "_");
//
//        if (generateRelativePath.equals("/")) {
//            resName = resource.functionName().text();
//        }
//        op.getOperation().setOperationId(getOperationId(resName));
//        op.getOperation().setParameters(null);
//        // Set operation summary
//        // Map API documentation
//        Map<String, String> apiDocs = listAPIDocumentations(resource, op);
//        //Add path parameters if in path and query parameters
//        AsyncAPIParameterMapper asyncAPIParameterMapper = new AsyncAPIParameterMapper(resource, op, apiDocs, components,
//                semanticModel);
//        asyncAPIParameterMapper.getResourceInputs(components, semanticModel);
//        if (asyncAPIParameterMapper.getErrors().size() > 1 || (asyncAPIParameterMapper.getErrors().size() == 1 &&
//                !asyncAPIParameterMapper.getErrors().get(0).getCode().equals("OAS_CONVERTOR_113"))) {
//            errors.addAll(asyncAPIParameterMapper.getErrors());
//            return Optional.empty();
//        }
//        errors.addAll(asyncAPIParameterMapper.getErrors());
//
//        AsyncAPIResponseMapper asyncAPIResponseMapper = new AsyncAPIResponseMapper(semanticModel, components,
//                resource.location());
//        asyncAPIResponseMapper.getResourceOutput(resource, op);
//        if (!asyncAPIResponseMapper.getErrors().isEmpty()) {
//            errors.addAll(asyncAPIResponseMapper.getErrors());
//            return Optional.empty();
//        }
//        return Optional.of(op);
//    }

                /**
                 * Filter the API documentations from resource function node.
                 */
    private Map<String, String> listAPIDocumentations(FunctionDefinitionNode resource,AsyncApi25ChannelItemImpl channelItem) {

        Map<String, String> apiDocs = new HashMap<>();
        if (resource.metadata().isPresent()) {
            Optional<Symbol> resourceSymbol = semanticModel.symbol(resource);
            if (resourceSymbol.isPresent()) {
                Symbol symbol = resourceSymbol.get();
                Optional<Documentation> documentation = ((Documentable) symbol).documentation();
                if (documentation.isPresent()) {
                    Documentation documentation1 = documentation.get();
                    Optional<String> description = documentation1.description();
                    if (description.isPresent()) {
                        String resourceFunctionAPI = description.get().trim();
                        apiDocs = documentation1.parameterMap();
                        channelItem.setDescription(resourceFunctionAPI);

//                        op.getOperation().setSummary(resourceFunctionAPI);
                    }
                }
            }
        }
        return apiDocs;
    }

        /**
         * Gets the http methods of a resource.
         *
         * @param resource    The ballerina resource.
         * @return A list of http methods.
         */
        private String generateRelativePath(FunctionDefinitionNode resource) {

            StringBuilder relativePath = new StringBuilder();
            relativePath.append("/");
            if (!resource.relativeResourcePath().isEmpty()) {
                for (Node node: resource.relativeResourcePath()) {
                    if (node instanceof ResourcePathParameterNode) {
                        ResourcePathParameterNode pathNode = (ResourcePathParameterNode) node;
                        relativePath.append("{");
                        relativePath.append(pathNode.paramName().get());
                        relativePath.append("}");
                    } else if ((resource.relativeResourcePath().size() == 1) && (node.toString().trim().equals("."))) {
                        return relativePath.toString();
                    } else {
                        relativePath.append(node.toString().trim());
                    }
                }
            }
            return relativePath.toString();
        }

}
