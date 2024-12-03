package org.rookedsysc.openapirequest;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api-key")
public record ApiKey(String disease) {}
