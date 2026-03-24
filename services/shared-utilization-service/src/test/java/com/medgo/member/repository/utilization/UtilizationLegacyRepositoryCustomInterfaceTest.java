package com.medgo.member.repository.utilization;

import com.medgo.member.domain.request.UtilizationRequest;
import com.medgo.member.domain.response.UtilizationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UtilizationLegacyRepositoryCustomInterfaceTest {

    @Test
    @DisplayName("Interface is loaded and method signature present")
    void testInterfaceLoaded() throws Exception {
        Class<?> iface = Class.forName("com.medgo.member.repository.utilization.UtilizationLegacyRepositoryCustom");
        assertTrue(iface.isInterface());
        assertEquals(1, iface.getDeclaredMethods().length);
        assertEquals("findUtilizationDataV6", iface.getDeclaredMethods()[0].getName());
    }



    @Test
    @DisplayName("Dynamic proxy invocation of findUtilizationDataV6 returns stub list")
    void testProxyInvocation() {
        UtilizationLegacyRepositoryCustom proxy = (UtilizationLegacyRepositoryCustom) Proxy.newProxyInstance(
                UtilizationLegacyRepositoryCustom.class.getClassLoader(),
                new Class[]{UtilizationLegacyRepositoryCustom.class},
                (obj, method, args) -> {
                    if (method.getName().equals("findUtilizationDataV6")) {
                        // Return deterministic dummy data to mark execution
                        UtilizationResponse r = new UtilizationResponse();
                        r.setControlCode("PX");
                        return List.of(r);
                    }
                    return null;
                }
        );
        UtilizationRequest req = new UtilizationRequest();
        List<UtilizationResponse> result = proxy.findUtilizationDataV6(req);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PX", result.get(0).getControlCode());
    }

    @Test
    @DisplayName("Proxy equals/hashCode/toString basic behavior")
    void testProxyObjectMethods() {
        UtilizationLegacyRepositoryCustom proxy = (UtilizationLegacyRepositoryCustom) Proxy.newProxyInstance(
                UtilizationLegacyRepositoryCustom.class.getClassLoader(),
                new Class[]{UtilizationLegacyRepositoryCustom.class},
                (obj, method, args) -> {
                    if (method.getName().equals("toString")) return "CustomProxy";
                    if (method.getName().equals("hashCode")) return 42;
                    if (method.getName().equals("equals")) return obj == args[0];
                    return null;
                }
        );
        assertEquals("CustomProxy", proxy.toString());
        assertEquals(42, proxy.hashCode());
        assertTrue(proxy.equals(proxy));
        assertFalse(proxy.equals(new Object()));
    }
}

