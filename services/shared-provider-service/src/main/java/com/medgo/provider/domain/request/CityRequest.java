package com.medgo.provider.domain.request;

import jakarta.validation.constraints.Size;
import java.util.List;

public class CityRequest {

    @Size(max = 1000)
    private List<Long> provinceIds;

    @Size(max = 1000)
    private List<String> codes;

    public List<Long> getProvinceIds() { return provinceIds; }
    public void setProvinceIds(List<Long> provinceIds) { this.provinceIds = provinceIds; }

    public List<String> getCodes() { return codes; }
    public void setCodes(List<String> codes) { this.codes = codes; }
}

























