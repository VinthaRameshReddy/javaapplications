package com.medgo.facescan.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class FaceScanAspectTest {

    @Test
    void init_and_advices_runWithoutException() {
        FaceScanAspect aspect = new FaceScanAspect();
        // init should not throw
        aspect.init();

        // create a mock join point
        JoinPoint jp = Mockito.mock(JoinPoint.class);
        Signature sig = Mockito.mock(Signature.class);
        Mockito.when(jp.getSignature()).thenReturn(sig);
        Mockito.when(sig.toString()).thenReturn("someSignature");

        // call advices
        assertDoesNotThrow(() -> aspect.beforeAdvice(jp));
        assertDoesNotThrow(() -> aspect.aftereAdvice(jp));
    }
}
