/*
 * Copyright 2011-2025 Lime Mojito Pty Ltd
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.limemojito.fontfailure.deploy;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.iam.IManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.AssetCode;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

import static com.limemojito.fontfailure.Main.HANDLER;
import static software.amazon.awscdk.services.iam.ManagedPolicy.fromAwsManagedPolicyName;
import static software.amazon.awscdk.services.lambda.Architecture.X86_64;
import static software.amazon.awscdk.services.lambda.Code.fromAsset;
import static software.amazon.awscdk.services.lambda.Runtime.JAVA_21;
import static software.amazon.awscdk.services.logs.RetentionDays.ONE_DAY;

@Slf4j
public final class JavaLambdaPocStack extends Stack {

    static final String LAMBDA_FUNCTION_ID = "font-failure-lambda";
    private static final String LAMBDA_CODE_PATH = "target/dependency/%s.jar".formatted(LAMBDA_FUNCTION_ID);
    private static final Runtime RUNTIME = JAVA_21;
    private static final Map<String, String> ENVIRONMENT = Map.of(
            /* Point home to writeable tmp directory on lambda runtime */
            "HOME", "/tmp"
    );

    public JavaLambdaPocStack(Construct scope,
                              String id,
                              StackProps props) {
        super(scope, id, props);

        final List<IManagedPolicy> managedPolicies =
                List.of(fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole"));

        final Role role = Role.Builder.create(this, LAMBDA_FUNCTION_ID + "-role")
                                      .assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
                                      .managedPolicies(managedPolicies)
                                      .build();

        final AssetCode assetCode = fromAsset(LAMBDA_CODE_PATH);

        final int memorySize = 1024;
        final int timeoutSeconds = 30;
        final Function function = new Function(this,
                                               LAMBDA_FUNCTION_ID,
                                               FunctionProps.builder()
                                                            .functionName(LAMBDA_FUNCTION_ID)
                                                            .description("Lambda font failure with Java "
                                                                                 + RUNTIME.getName())
                                                            .role(role)
                                                            .timeout(Duration.seconds(timeoutSeconds))
                                                            .memorySize(memorySize)
                                                            .environment(ENVIRONMENT)
                                                            .code(assetCode)
                                                            .runtime(RUNTIME)
                                                            .handler(HANDLER)
                                                            .logRetention(ONE_DAY)
                                                            .architecture(X86_64)
                                                            .build());
        /*CfnFunction cfnFunction = (CfnFunction) function.getNode().getDefaultChild();
        cfnFunction.setSnapStart(CfnFunction.SnapStartProperty.builder()
                                                              .applyOn("PublishedVersions")
                                                              .build());
        Version snapstartVersion = new Version(this,
                                               LAMBDA_FUNCTION_ID + "-snap",
                                               VersionProps.builder()
                                                           .lambda(function)
                                                           .description("Snapstart Version")
                                                           .build());
*/
    }
}
