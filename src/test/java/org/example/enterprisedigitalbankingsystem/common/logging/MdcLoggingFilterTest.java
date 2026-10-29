package org.example.enterprisedigitalbankingsystem.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MdcLoggingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private MdcLoggingFilter mdcLoggingFilter;

    @BeforeEach
    void setUp() {
        mdcLoggingFilter = new MdcLoggingFilter();
        MDC.clear();
    }

    @Test
    @DisplayName("Should extract existing X-Correlation-ID header and populate MDC traceId")
    void shouldExtractExistingCorrelationId() throws ServletException, IOException {
        when(request.getHeader("X-Correlation-ID")).thenReturn("custom-trace-12345");

        doAnswer(invocation -> {
            assertThat(MDC.get(MdcLoggingFilter.TRACE_ID_KEY)).isEqualTo("custom-trace-12345");
            assertThat(MDC.get(MdcLoggingFilter.USER_ID_KEY)).isEqualTo("anonymous");
            return null;
        }).when(filterChain).doFilter(request, response);

        mdcLoggingFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setHeader(eq("X-Correlation-ID"), eq("custom-trace-12345"));
        // Ensure MDC is cleared afterwards to prevent thread-pool leakage
        assertThat(MDC.get(MdcLoggingFilter.TRACE_ID_KEY)).isNull();
    }

    @Test
    @DisplayName("Should generate new traceId when X-Correlation-ID header is missing")
    void shouldGenerateTraceIdWhenHeaderMissing() throws ServletException, IOException {
        when(request.getHeader("X-Correlation-ID")).thenReturn(null);

        doAnswer(invocation -> {
            String generated = MDC.get(MdcLoggingFilter.TRACE_ID_KEY);
            assertThat(generated).isNotNull();
            assertThat(generated.length()).isGreaterThanOrEqualTo(16);
            return null;
        }).when(filterChain).doFilter(request, response);

        mdcLoggingFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setHeader(eq("X-Correlation-ID"), anyString());
        assertThat(MDC.get(MdcLoggingFilter.TRACE_ID_KEY)).isNull();
    }
}

