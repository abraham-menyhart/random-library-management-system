package com.library.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    
    @Bean
    public Counter successfulBorrowsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("library.borrows.successful.total")
                .description("Total number of successful book borrows")
                .register(meterRegistry);
    }
    
    @Bean
    public Counter failedBorrowsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("library.borrows.failed.total")
                .description("Total number of failed borrow attempts")
                .register(meterRegistry);
    }
}