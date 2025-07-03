package com.prodash.config;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import java.util.function.Predicate;

public class LlmApiRetryExceptionPredicate implements Predicate<Throwable> {

    @Override
    public boolean test(Throwable throwable) {
        // Retry on 5xx server errors
        if (throwable instanceof HttpServerErrorException) {
            return true;
        }
        // Retry on 429 Too Many Requests
        if (throwable instanceof HttpClientErrorException e) {
            return e.getStatusCode().value() == 429;
        }
        // Do not retry on other client errors (4xx) or other exceptions
        return false;
    }
}