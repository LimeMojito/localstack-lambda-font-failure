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

package com.limemojito.fontfailure;

import com.limemojito.aws.localstack.lambda.LocalstackLambdaConfig;
import com.limemojito.aws.s3.LocalstackS3Config;
import com.limemojito.test.lambda.LambdaSupport;
import com.limemojito.test.lambda.LambdaSupportConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = NONE, classes = LambdaIT.LambdaConfiguration.class)
public class LambdaIT {

    @Configuration
    @Import({JacksonAutoConfiguration.class,
            LambdaSupportConfig.class,
            LocalstackS3Config.class,
            LocalstackLambdaConfig.class})
    public static class LambdaConfiguration {
    }

    @Autowired
    private LambdaSupport lambda;

    @Test
    public void shouldDeployOk() {
        // env that removes font cache warning on AWS real.
        final Map<String, String> env = Map.of("HOME", "/tmp");

        final LambdaSupport.Lambda reportGenerator = lambda.java("../font-failure-lambda",
                                                                 Main.HANDLER,
                                                                 1024,
                                                                 env);

        final String fontDump = lambda.invoke(reportGenerator,
                                              Map.of(),
                                              String.class);
        assertThat(fontDump).doesNotContain("RuntimeException");
    }
}
