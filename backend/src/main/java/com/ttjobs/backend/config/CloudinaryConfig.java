package com.ttjobs.backend.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.val;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:${CLOUDINARY_CLOUD_NAME:}}")
    private String cloudName;

    @Value("${cloudinary.api-key:${CLOUDINARY_API_KEY:}}")
    private String apiKey;

    @Value("${cloudinary.api-secret:${CLOUDINARY_API_SECRET:}}")
    private String apiSecret;
 
    @Bean
    @Conditional(CloudinaryEnvConfiguredCondition.class)
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    static class CloudinaryEnvConfiguredCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Environment env = context.getEnvironment();
            return hasText(env.getProperty("CLOUDINARY_CLOUD_NAME"))
                    && hasText(env.getProperty("CLOUDINARY_API_KEY"))
                    && hasText(env.getProperty("CLOUDINARY_API_SECRET"));
        }

        private boolean hasText(String value) {
            return value != null && !value.trim().isEmpty();
        }
    }
}
