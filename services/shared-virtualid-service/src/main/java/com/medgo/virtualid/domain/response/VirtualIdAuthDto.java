package com.medgo.virtualid.domain.response;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class VirtualIdAuthDto implements Serializable {

    private String accessToken;
    private Integer status;

}
