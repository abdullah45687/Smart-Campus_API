package com.campus.api.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String START_TIME_NANOS = "requestStartTimeNanos";
    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        requestContext.setProperty(START_TIME_NANOS, System.nanoTime());
        LOGGER.info(() -> String.format(
                "INCOMING REQUEST: method=%s uri=%s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()
        ));
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        long durationMs = -1;
        Object start = requestContext.getProperty(START_TIME_NANOS);
        if (start instanceof Long) {
            durationMs = (System.nanoTime() - (Long) start) / 1_000_000;
        }

        long finalDurationMs = durationMs;
        LOGGER.info(() -> String.format(
                "OUTGOING RESPONSE: method=%s uri=%s status=%d durationMs=%d",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri(),
                responseContext.getStatus(),
                finalDurationMs
        ));
    }
}