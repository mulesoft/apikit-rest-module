/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static java.util.Collections.singletonList;
import static org.mule.metadata.api.model.MetadataFormat.JAVA;
import static org.mule.parser.service.ParserMode.AUTO;
import static org.mule.runtime.api.meta.Category.COMMUNITY;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static org.mule.runtime.api.meta.model.parameter.ParameterRole.BEHAVIOUR;
import static org.mule.runtime.api.meta.model.stereotype.StereotypeModelBuilder.newStereotype;
import static org.mule.runtime.core.api.exception.Errors.CORE_NAMESPACE_NAME;
import static org.mule.runtime.extension.api.stereotype.MuleStereotypes.CONFIG;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.metadata.api.ClassTypeLoader;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.ObjectType;
import org.mule.module.apikit.utils.MuleVersionUtils;
import org.mule.parser.service.ParserMode;
import org.mule.runtime.api.meta.model.ImportedTypeModel;
import org.mule.runtime.api.meta.model.ParameterDslConfiguration;
import org.mule.runtime.api.meta.model.XmlDslModel;
import org.mule.runtime.api.meta.model.declaration.fluent.ConfigurationDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.OperationDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.ParameterGroupDeclarer;
import org.mule.runtime.api.meta.model.error.ErrorModel;
import org.mule.runtime.api.meta.model.error.ErrorModelBuilder;
import org.mule.runtime.api.meta.model.stereotype.StereotypeModel;
import org.mule.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;
import org.mule.runtime.extension.api.loader.ExtensionLoadingContext;
import org.mule.runtime.extension.api.loader.ExtensionLoadingDelegate;
import org.mule.runtime.extension.api.runtime.config.ConfigurationProvider;

public class ApikitExtensionLoadingDelegate implements ExtensionLoadingDelegate {

  public static final String EXTENSION_NAME = "APIKit";
  public static final String PREFIX_NAME = "apikit";
  public static final String EXTENSION_DESCRIPTION = "APIKit plugin";
  public static final String VENDOR = "Mulesoft";
  public static final String VERSION = "2.0.0-SNAPSHOT";
  public static final String XSD_FILE_NAME = "mule-apikit.xsd";
  private static final String UNESCAPED_LOCATION_PREFIX = "http://";
  private static final String SCHEMA_LOCATION = "www.mulesoft.org/schema/mule/mule-apikit";
  private static final String SCHEMA_VERSION = "current";
  private static final String EXTENSION_NAMESPACE = "APIKIT";

  protected final BaseTypeBuilder typeBuilder = BaseTypeBuilder.create(JAVA);

  @Override
  public void accept(ExtensionDeclarer extensionDeclarer, ExtensionLoadingContext extensionLoadingContext) {
    ErrorModel muleAnyErrorType = ErrorModelBuilder.newError("ANY", CORE_NAMESPACE_NAME).build();
    ErrorModel apikitAnyErrorType = ErrorModelBuilder.newError("ANY", EXTENSION_NAMESPACE).withParent(muleAnyErrorType).build();
    ErrorModel badRequestErrorModel =
        ErrorModelBuilder.newError("BAD_REQUEST", EXTENSION_NAMESPACE).withParent(apikitAnyErrorType).build();
    ErrorModel notAcceptableErrorModel =
        ErrorModelBuilder.newError("NOT_ACCEPTABLE", EXTENSION_NAMESPACE).withParent(apikitAnyErrorType).build();
    ErrorModel unsupportedMediaTypeErrorModel =
        ErrorModelBuilder.newError("UNSUPPORTED_MEDIA_TYPE", EXTENSION_NAMESPACE).withParent(apikitAnyErrorType).build();
    ErrorModel methodNotAllowedErrorModel =
        ErrorModelBuilder.newError("METHOD_NOT_ALLOWED", EXTENSION_NAMESPACE).withParent(apikitAnyErrorType).build();
    ErrorModel notFoundErrorModel =
        ErrorModelBuilder.newError("NOT_FOUND", EXTENSION_NAMESPACE).withParent(apikitAnyErrorType).build();
    ErrorModel notImplementedErrorModel =
        ErrorModelBuilder.newError("NOT_IMPLEMENTED", EXTENSION_NAMESPACE).withParent(apikitAnyErrorType).build();

    XmlDslModel xmlDslModel = XmlDslModel.builder()
        .setPrefix(PREFIX_NAME)
        .setXsdFileName(XSD_FILE_NAME)
        .setSchemaVersion(VERSION)
        .setSchemaLocation(String.format("%s/%s/%s", UNESCAPED_LOCATION_PREFIX + SCHEMA_LOCATION, SCHEMA_VERSION, XSD_FILE_NAME))
        .setNamespace(UNESCAPED_LOCATION_PREFIX + SCHEMA_LOCATION)
        .build();
    ClassTypeLoader typeLoader = ExtensionsTypeLoaderFactory.getDefault().createTypeLoader();

    extensionDeclarer.named(EXTENSION_NAME)
        .describedAs(EXTENSION_DESCRIPTION)
        .fromVendor(VENDOR)
        .onVersion(VERSION)
        .withCategory(COMMUNITY)
        .withXmlDsl(xmlDslModel)
        .withErrorModel(badRequestErrorModel)
        .withErrorModel(apikitAnyErrorType)
        .withErrorModel(notAcceptableErrorModel)
        .withErrorModel(unsupportedMediaTypeErrorModel)
        .withErrorModel(methodNotAllowedErrorModel)
        .withErrorModel(notFoundErrorModel)
        .withErrorModel(notImplementedErrorModel);
    extensionDeclarer.withImportedType(new ImportedTypeModel((ObjectType) typeLoader.load(HttpRequestAttributes.class)));

    // config
    final StereotypeModel apikitConfigStereotype = newStereotype("APIKIT_CONFIG", EXTENSION_NAMESPACE).withParent(CONFIG).build();
    ConfigurationDeclarer apikitConfig = extensionDeclarer.withConfig("config")
        .describedAs(PREFIX_NAME)
        .withStereotype(apikitConfigStereotype);
    ParameterGroupDeclarer parameterGroupDeclarer = apikitConfig.onDefaultParameterGroup();
    parameterGroupDeclarer.withOptionalParameter("raml").ofType(typeLoader.load(String.class));
    parameterGroupDeclarer.withOptionalParameter("api").ofType(typeLoader.load(String.class));
    parameterGroupDeclarer.withRequiredParameter("outboundHeadersMapName").ofType(typeLoader.load(String.class));
    parameterGroupDeclarer.withRequiredParameter("httpStatusVarName").ofType(typeLoader.load(String.class));
    parameterGroupDeclarer.withOptionalParameter("keepApiBaseUri").defaultingTo(false).ofType(typeLoader.load(String.class));
    parameterGroupDeclarer.withOptionalParameter("keepRamlBaseUri").defaultingTo(false).ofType(typeLoader.load(String.class));
    parameterGroupDeclarer.withOptionalParameter("disableValidations").defaultingTo(false).ofType(typeLoader.load(String.class));
    parameterGroupDeclarer.withOptionalParameter("queryParamsStrictValidation").defaultingTo(false)
        .ofType(typeLoader.load(String.class));
    parameterGroupDeclarer.withOptionalParameter("headersStrictValidation").defaultingTo(false)
        .ofType(typeLoader.load(String.class));
    parameterGroupDeclarer.withOptionalParameter("parser").defaultingTo(AUTO).ofType(typeLoader.load(ParserMode.class));
    parameterGroupDeclarer.withOptionalParameter("flowMappings")
        .withDsl(ParameterDslConfiguration.builder().allowsReferences(false).build())
        .withExpressionSupport(NOT_SUPPORTED)
        .ofType(typeBuilder.arrayType().of(typeLoader.load(FlowMapping.class)).build());

    // router
    OperationDeclarer routerDeclarer = apikitConfig.withOperation("router");
    routerDeclarer.withOutputAttributes().ofType(typeLoader.load(HttpRequestAttributes.class));
    routerDeclarer.withOutput().ofType(typeLoader.load(Object.class));
    routerDeclarer.withErrorModel(badRequestErrorModel)
        .withErrorModel(notAcceptableErrorModel)
        .withErrorModel(unsupportedMediaTypeErrorModel)
        .withErrorModel(methodNotAllowedErrorModel)
        .withErrorModel(notFoundErrorModel);
    addConfigRefParameter(routerDeclarer, apikitConfigStereotype);

    // console
    OperationDeclarer consoleDeclarer = apikitConfig.withOperation("console");
    consoleDeclarer.withOutputAttributes().ofType(typeLoader.load(HttpRequestAttributes.class));
    consoleDeclarer.withOutput().ofType(typeLoader.load(Object.class));
    consoleDeclarer.withErrorModel(notFoundErrorModel);
    addConfigRefParameter(consoleDeclarer, apikitConfigStereotype);
  }

  // This code below is taken from ConfigRefDeclarationEnricher in mule-extensions-api
  private void addConfigRefParameter(OperationDeclarer declarer, final StereotypeModel apikitConfigStereotype) {
    // For plder versions, the generated schema would have the parameter duplicated
    if (MuleVersionUtils.isAtLeast("4.3.0")) {
      declarer.onDefaultParameterGroup().withRequiredParameter("config-ref")
          .describedAs("The name of the configuration to be used to execute this component")
          .withRole(BEHAVIOUR)
          .withDsl(ParameterDslConfiguration.builder().allowsReferences(true).build())
          .ofType(buildConfigRefType())
          .withExpressionSupport(NOT_SUPPORTED)
          .withAllowedStereotypes(singletonList(apikitConfigStereotype));
    }
  }

  private static MetadataType buildConfigRefType() {
    return BaseTypeBuilder.create(JAVA).objectType().id(ConfigurationProvider.class.getName()).build();
  }
}
