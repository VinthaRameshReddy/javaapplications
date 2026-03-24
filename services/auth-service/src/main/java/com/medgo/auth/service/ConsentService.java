package com.medgo.auth.service;

import com.medgo.auth.domain.request.ListConsentsRequest;
import com.medgo.auth.domain.request.StoreUserConsentRequest;
import com.medgo.commons.CommonResponse;

public interface ConsentService {
    
    CommonResponse listConsents(ListConsentsRequest request);
    
    CommonResponse storeUserConsent(StoreUserConsentRequest request);
    
    boolean isUserConsented(Integer userId);

    void revalidateUserConsentFlag(Integer userId);
}

