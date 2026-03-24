package com.medgo.virtualid.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VirtualIdAuthJson {
    private String username;
    private String password;
}


