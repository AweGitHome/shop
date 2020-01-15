package com.leyou.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "awe.email")
@Data
public class EmailProperties {
    private String sendAddress;
    private String password;
    private String smtpHost;
    private String protocol;
    private String template;
}
