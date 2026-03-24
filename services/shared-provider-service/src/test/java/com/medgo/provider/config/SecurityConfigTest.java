//package com.medgo.provider.config;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.mock.web.MockHttpServletResponse;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class SecurityConfigTest {
//
//    @Test
//    void apiKeyFilter_allowsHealth() throws ServletException, IOException {
//        SecurityConfig cfg = new SecurityConfig();
//        OncePerRequestFilter filter = cfg.apiKeyFilter();
//        HttpServletRequest req = mock(HttpServletRequest.class);
//        HttpServletResponse res = mock(HttpServletResponse.class);
//        FilterChain chain = mock(FilterChain.class);
//        when(req.getRequestURI()).thenReturn("/health");
//        filter.doFilter(req, res, chain);
//        verify(chain, times(1)).doFilter(req, res);
//    }
//
//    @Test
//    void apiKeyFilter_deniesWhenMissing() throws ServletException, IOException {
//        SecurityConfig cfg = new SecurityConfig();
//        OncePerRequestFilter filter = cfg.apiKeyFilter();
//        MockHttpServletRequest req = new MockHttpServletRequest();
//        req.setRequestURI("/v1/doctor");
//        MockHttpServletResponse res = new MockHttpServletResponse();
//        FilterChain chain = mock(FilterChain.class);
//        filter.doFilter(req, res, chain);
//        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, res.getStatus());
//        verify(chain, never()).doFilter(any(), any());
//    }
//}
//
//
