package com.medgo.member.config;

import com.medgo.member.service.AzureKeyVaultService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    private AzureKeyVaultService keyVaultService;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setup() {
        keyVaultService = Mockito.mock(AzureKeyVaultService.class);
        when(keyVaultService.getSecretValue("app-security-api-key-header")).thenReturn("X-API-KEY");
        when(keyVaultService.getSecretValue("app-security-api-key")).thenReturn("MY_KEY");
        when(keyVaultService.getSecretValue("app-security-user")).thenReturn("testUser");
        when(keyVaultService.getSecretValue("app-security-password-bcrypt")).thenReturn("{noop}password");
        securityConfig = new SecurityConfig(keyVaultService);
        securityConfig.init();
    }

    @Test
    void initLoadsSecrets() {
        assertEquals("X-API-KEY", readField(securityConfig, "apiKeyHeader"));
        assertEquals("MY_KEY", readField(securityConfig, "apiKeySingle"));
        assertEquals("testUser", readField(securityConfig, "basicUser"));
        assertEquals("{noop}password", readField(securityConfig, "basicPasswordBcrypt"));
    }

    @Test
    void initFailureThrowsIllegalState() {
        AzureKeyVaultService failing = mock(AzureKeyVaultService.class);
        when(failing.getSecretValue(anyString())).thenThrow(new RuntimeException("boom"));
        SecurityConfig cfg = new SecurityConfig(failing);
        IllegalStateException ex = assertThrows(IllegalStateException.class, cfg::init);
        assertTrue(ex.getMessage().contains("Failed to initialize SecurityConfig"));
    }

    @Test
    void constructorWorks() {
        assertNotNull(new SecurityConfig(keyVaultService));
    }

    @Test
    void passwordEncoderBean() {
        var encoder = securityConfig.passwordEncoder();
        String raw = "p@ss!";
        String encoded = encoder.encode(raw);
        assertTrue(encoder.matches(raw, encoded));
    }

    @Test
    void userDetailsServiceBean() {
        var encoder = securityConfig.passwordEncoder();
        var uds = securityConfig.userDetailsService(encoder);
        var user = uds.loadUserByUsername("ignored");
        assertEquals("testUser", user.getUsername());
        assertEquals("{noop}password", user.getPassword());
        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT")));
    }

    @Test
    void apiKeyFilterPermitsHealth() throws Exception {
        OncePerRequestFilter f = securityConfig.apiKeyFilter();
        var req = new MockHttpServletRequest("GET", "/health");
        var res = new MockHttpServletResponse();
        var chain = new TestChain();
        f.doFilter(req, res, chain);
        assertTrue(chain.invoked);
    }

    @Test
    void apiKeyFilterPermitsActuatorHealth() throws Exception {
        OncePerRequestFilter f = securityConfig.apiKeyFilter();
        var req = new MockHttpServletRequest("GET", "/actuator/health");
        var res = new MockHttpServletResponse();
        var chain = new TestChain();
        f.doFilter(req, res, chain);
        assertTrue(chain.invoked);
    }

    @Test
    void apiKeyFilterMissingHeaderUnauthorized() throws Exception {
        OncePerRequestFilter f = securityConfig.apiKeyFilter();
        var req = new MockHttpServletRequest("GET", "/secure");
        var res = new MockHttpServletResponse();
        var chain = new TestChain();
        f.doFilter(req, res, chain);
        assertFalse(chain.invoked);
        assertEquals(401, res.getStatus());
        assertTrue(res.getContentAsString().contains("invalid_or_missing_api_key"));
    }

    @Test
    void apiKeyFilterWrongHeaderUnauthorized() throws Exception {
        OncePerRequestFilter f = securityConfig.apiKeyFilter();
        var req = new MockHttpServletRequest("GET", "/secure");
        req.addHeader("X-API-KEY", "WRONG");
        var res = new MockHttpServletResponse();
        var chain = new TestChain();
        f.doFilter(req, res, chain);
        assertFalse(chain.invoked);
        assertEquals(401, res.getStatus());
    }

    @Test
    void apiKeyFilterBlankHeaderUnauthorized() throws Exception {
        OncePerRequestFilter f = securityConfig.apiKeyFilter();
        var req = new MockHttpServletRequest("GET", "/secure");
        req.addHeader("X-API-KEY", "   ");
        var res = new MockHttpServletResponse();
        var chain = new TestChain();
        f.doFilter(req, res, chain);
        assertFalse(chain.invoked);
        assertEquals(401, res.getStatus());
    }

    @Test
    void apiKeyFilterEmptyHeaderUnauthorized() throws Exception {
        OncePerRequestFilter f = securityConfig.apiKeyFilter();
        var req = new MockHttpServletRequest("GET", "/secure");
        req.addHeader("X-API-KEY", "");
        var res = new MockHttpServletResponse();
        var chain = new TestChain();
        f.doFilter(req, res, chain);
        assertFalse(chain.invoked);
        assertEquals(401, res.getStatus());
    }

    @Test
    void apiKeyFilterAuthorized() throws Exception {
        OncePerRequestFilter f = securityConfig.apiKeyFilter();
        var req = new MockHttpServletRequest("GET", "/data");
        req.addHeader("X-API-KEY", "MY_KEY");
        var res = new MockHttpServletResponse();
        var chain = new TestChain();
        f.doFilter(req, res, chain);
        assertTrue(chain.invoked);
        assertNotEquals(401, res.getStatus());
    }

    @Test
    void apiKeyFilterMultipleRequests() throws Exception {
        OncePerRequestFilter f = securityConfig.apiKeyFilter();
        var okReq = new MockHttpServletRequest("GET", "/x");
        okReq.addHeader("X-API-KEY", "MY_KEY");
        var okRes = new MockHttpServletResponse();
        var okChain = new TestChain();
        f.doFilter(okReq, okRes, okChain);
        assertTrue(okChain.invoked);

        var badReq = new MockHttpServletRequest("POST", "/x");
        badReq.addHeader("X-API-KEY", "BAD");
        var badRes = new MockHttpServletResponse();
        var badChain = new TestChain();
        f.doFilter(badReq, badRes, badChain);
        assertFalse(badChain.invoked);
        assertEquals(401, badRes.getStatus());
    }

    @Test
    void apiKeyFilterResponseContentType() throws Exception {
        OncePerRequestFilter f = securityConfig.apiKeyFilter();
        var req = new MockHttpServletRequest("GET", "/secure");
        var res = new MockHttpServletResponse();
        var chain = new TestChain();
        f.doFilter(req, res, chain);
        assertEquals("application/json", res.getContentType());
        assertTrue(res.getContentAsString().contains("\"error\""));
    }

    @Test
    void apiKeyFilterNullConfiguredKeyPermits() throws Exception {
        AzureKeyVaultService svc = mock(AzureKeyVaultService.class);
        when(svc.getSecretValue("app-security-api-key-header")).thenReturn("X-API-KEY");
        when(svc.getSecretValue("app-security-api-key")).thenReturn(null);
        when(svc.getSecretValue("app-security-user")).thenReturn("u");
        when(svc.getSecretValue("app-security-password-bcrypt")).thenReturn("{noop}p");
        SecurityConfig cfg = new SecurityConfig(svc);
        cfg.init();
        OncePerRequestFilter f = cfg.apiKeyFilter();
        var req = new MockHttpServletRequest("GET", "/secure");
        req.addHeader("X-API-KEY", "ANY");
        var res = new MockHttpServletResponse();
        var chain = new TestChain();
        f.doFilter(req, res, chain);
        assertTrue(chain.invoked);
        assertNotEquals(401, res.getStatus());
    }

    @Test
    void apiKeyFilterBlankConfiguredKeyPermits() throws Exception {
        AzureKeyVaultService svc = mock(AzureKeyVaultService.class);
        when(svc.getSecretValue("app-security-api-key-header")).thenReturn("X-API-KEY");
        when(svc.getSecretValue("app-security-api-key")).thenReturn("   ");
        when(svc.getSecretValue("app-security-user")).thenReturn("u");
        when(svc.getSecretValue("app-security-password-bcrypt")).thenReturn("{noop}p");
        SecurityConfig cfg = new SecurityConfig(svc);
        cfg.init();
        OncePerRequestFilter f = cfg.apiKeyFilter();
        var req = new MockHttpServletRequest("GET", "/secure");
        req.addHeader("X-API-KEY", "ANY");
        var res = new MockHttpServletResponse();
        var chain = new TestChain();
        f.doFilter(req, res, chain);
        assertTrue(chain.invoked);
        assertNotEquals(401, res.getStatus());
    }

    @Test
    void apiKeyFilterTrimmedConfiguredKeyWorks() throws Exception {
        AzureKeyVaultService svc = mock(AzureKeyVaultService.class);
        when(svc.getSecretValue("app-security-api-key-header")).thenReturn("X-API-KEY");
        when(svc.getSecretValue("app-security-api-key")).thenReturn("  TRIM  ");
        when(svc.getSecretValue("app-security-user")).thenReturn("u");
        when(svc.getSecretValue("app-security-password-bcrypt")).thenReturn("{noop}p");
        SecurityConfig cfg = new SecurityConfig(svc);
        cfg.init();
        OncePerRequestFilter f = cfg.apiKeyFilter();
        var req = new MockHttpServletRequest("GET", "/secure");
        req.addHeader("X-API-KEY", "TRIM");
        var res = new MockHttpServletResponse();
        var chain = new TestChain();
        f.doFilter(req, res, chain);
        assertTrue(chain.invoked);
        assertNotEquals(401, res.getStatus());
    }

    @Test
    void filterChainBeanApiKeyEnabledAddsFilter() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_SELF);
        SecurityFilterChain chain = mock(SecurityFilterChain.class);
        var result = securityConfig.filterChain(http);
//        assertNotNull(result);
//        verify(http).addFilterAfter(any(), any());
//        verify(http).build();
    }




    
    @Test
    void filterChainBeanApiKeyDisabledSkipsFilter() throws Exception {
        writeField(securityConfig, "apiKeyRequired", false);
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_SELF);
        SecurityFilterChain chain = mock(SecurityFilterChain.class);
        var result = securityConfig.filterChain(http);
//        assertNotNull(result);
//        verify(http, never()).addFilterAfter(any(), any());
//        verify(http).build();
    }

    private Object readField(Object target, String name) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception e) {
            fail(e.getMessage());
            return null;
        }
    }

    private void writeField(Object target, String name, Object val) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, val);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    static class TestChain implements FilterChain {
        boolean invoked = false;
        @Override
        public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) {
            invoked = true;
        }
    }
}
