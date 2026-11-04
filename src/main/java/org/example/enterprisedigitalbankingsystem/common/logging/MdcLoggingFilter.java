package org.example.enterprisedigitalbankingsystem.common.logging;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Day 62: MDC (Mapped Diagnostic Context) Logging Filter.
 * Generates or extracts a distributed trace correlation ID and attaches it to every log line
 * and response header for end-to-end request tracing.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String USER_ID_KEY = "userId";
    public static final String CORRELATION_HEADER = "X-Correlation-ID";
    private static final String TRACE_HEADER = "X-Trace-ID";

    private final ObjectProvider<Tracer> tracerProvider;

    public MdcLoggingFilter(ObjectProvider<Tracer> tracerProvider) {
        this.tracerProvider = tracerProvider;
    }

    public MdcLoggingFilter() {
        this.tracerProvider = null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Resolve or generate correlation trace ID
            String traceId = null;
            Tracer tracer = (tracerProvider != null) ? tracerProvider.getIfAvailable() : null;
            if(tracer != null && tracer.currentSpan() != null){
                traceId = tracer.currentSpan().context().traceId();
            }
            if(traceId == null || traceId.isBlank()){
                traceId = request.getHeader(CORRELATION_HEADER);
            }
            if(traceId ==  null || traceId.isBlank()){
                traceId = UUID.randomUUID().toString().replace("-","").substring(0 , 16);
            }

            MDC.put(TRACE_ID_KEY,traceId);
            response.setHeader(CORRELATION_HEADER, traceId);
            response.setHeader(TRACE_HEADER,traceId);

            if(MDC.get(USER_ID_KEY) == null){
                MDC.put(USER_ID_KEY,"anonymous");
            }

            filterChain.doFilter(request,response);
        } finally {
            MDC.clear();
        }
    }
}

