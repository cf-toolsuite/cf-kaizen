package org.cftoolsuite.cfapp.config;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.spring.boot.CfEnvProcessor;
import io.pivotal.cfenv.spring.boot.CfEnvProcessorProperties;

import java.util.Map;

public class CfKaizenLlmEnvProcessor implements CfEnvProcessor {

    private static final String SERVICE_NAME = "cf-kaizen-llm-secrets";

    private static void addOrUpdatePropertyValue(String propertyName, String credentialName, CfCredentials cfCredentials, Map<String, Object> properties) {
        Object credential = cfCredentials.getMap().get(credentialName);
        if (credential != null) {
            properties.put(propertyName, credential);
        }
    }

    private static void addPropertyValue(String propertyName, Object propertyValue, Map<String, Object> properties) {
        properties.put(propertyName, propertyValue);
    }

    @Override
    public boolean accept(CfService service) {
        return
                service.getName().equalsIgnoreCase(SERVICE_NAME);
    }

    @Override
    public CfEnvProcessorProperties getProperties() {
        return
                CfEnvProcessorProperties
                        .builder()
                        .serviceName(SERVICE_NAME)
                        .build();
    }

    @Override
    public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
        addPropertyValue("credhub.url", "https://credhub.service.cf.internal:8844", properties);
        addOrUpdatePropertyValue("spring.ai.openai.api-key", "OPENAI_API_KEY", cfCredentials, properties);
        addOrUpdatePropertyValue("spring.ai.openai.chat.options.model", "CHAT_MODEL", cfCredentials, properties);
        addOrUpdatePropertyValue("spring.ai.openai.embedding.options.model", "EMBEDDING_MODEL", cfCredentials, properties);
    }
}

