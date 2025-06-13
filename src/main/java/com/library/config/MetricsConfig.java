package com.library.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    
    @Bean
    public Counter booksBorrowedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("library.books.borrowed.total")
                .description("Total number of books borrowed")
                .register(meterRegistry);
    }
    
    @Bean
    public Counter booksAddedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("library.books.added.total")
                .description("Total number of books added to library")
                .register(meterRegistry);
    }
    
    @Bean
    public Counter borrowersCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("library.borrowers.created.total")
                .description("Total number of borrowers created")
                .register(meterRegistry);
    }
    
    @Bean
    public Timer bookOperationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("library.book.operation.duration")
                .description("Duration of book operations")
                .register(meterRegistry);
    }
    
    @Bean
    public Timer borrowerOperationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("library.borrower.operation.duration")
                .description("Duration of borrower operations")
                .register(meterRegistry);
    }
}