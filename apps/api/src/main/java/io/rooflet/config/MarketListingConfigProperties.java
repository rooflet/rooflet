package io.rooflet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rooflet.market-listings")
@Data
public class MarketListingConfigProperties {

    /**
     * Maximum number of zip codes a user can watch
     * Default: 10
     */
    private int maxWatchedZipCodes = 10;
}

