package com.medgo.bean;//
// Source statusCode recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Generated;

@JsonPropertyOrder({"operationid", "reqtoken", "reqdata"})
public class MB {
    @JsonProperty("operationid")
    private String operationId;
    @JsonProperty("reqtoken")
    private String reqtoken;
    @JsonProperty("reqdata")
    private Object reqdata;

    public MB(String operationId, String reqtoken, Object reqdata) {
        this.operationId = operationId;
        this.reqtoken = reqtoken;
        this.reqdata = reqdata;
    }

    public MB() {
    }

    public String getOperationId() {
        return this.operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getReqtoken() {
        return this.reqtoken;
    }

    public void setReqtoken(String reqtoken) {
        this.reqtoken = reqtoken;
    }

    public Object getReqdata() {
        return this.reqdata;
    }

    public void setReqdata(Object reqdata) {
        this.reqdata = reqdata;
    }

    @Generated
    public static MBBuilder builder() {
        return new MBBuilder();
    }

    @Generated
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof MB)) {
            return false;
        } else {
            MB other = (MB)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$operationId = this.getOperationId();
                Object other$operationId = other.getOperationId();
                if (this$operationId == null) {
                    if (other$operationId != null) {
                        return false;
                    }
                } else if (!this$operationId.equals(other$operationId)) {
                    return false;
                }

                Object this$reqtoken = this.getReqtoken();
                Object other$reqtoken = other.getReqtoken();
                if (this$reqtoken == null) {
                    if (other$reqtoken != null) {
                        return false;
                    }
                } else if (!this$reqtoken.equals(other$reqtoken)) {
                    return false;
                }

                Object this$reqdata = this.getReqdata();
                Object other$reqdata = other.getReqdata();
                if (this$reqdata == null) {
                    if (other$reqdata != null) {
                        return false;
                    }
                } else if (!this$reqdata.equals(other$reqdata)) {
                    return false;
                }

                return true;
            }
        }
    }

    @Generated
    protected boolean canEqual(final Object other) {
        return other instanceof MB;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $operationId = this.getOperationId();
        result = result * 59 + ($operationId == null ? 43 : $operationId.hashCode());
        Object $reqtoken = this.getReqtoken();
        result = result * 59 + ($reqtoken == null ? 43 : $reqtoken.hashCode());
        Object $reqdata = this.getReqdata();
        result = result * 59 + ($reqdata == null ? 43 : $reqdata.hashCode());
        return result;
    }

    @Generated
    public String toString() {
        String var10000 = this.getOperationId();
        return "MB(operationId=" + var10000 + ", reqtoken=" + this.getReqtoken() + ", reqdata=" + String.valueOf(this.getReqdata()) + ")";
    }

    @Generated
    public static class MBBuilder {
        @Generated
        private String operationId;
        @Generated
        private String reqtoken;
        @Generated
        private Object reqdata;

        @Generated
        MBBuilder() {
        }

        @JsonProperty("operationid")
        @Generated
        public MBBuilder operationId(final String operationId) {
            this.operationId = operationId;
            return this;
        }

        @JsonProperty("reqtoken")
        @Generated
        public MBBuilder reqtoken(final String reqtoken) {
            this.reqtoken = reqtoken;
            return this;
        }

        @JsonProperty("reqdata")
        @Generated
        public MBBuilder reqdata(final Object reqdata) {
            this.reqdata = reqdata;
            return this;
        }

        @Generated
        public MB build() {
            return new MB(this.operationId, this.reqtoken, this.reqdata);
        }

        @Generated
        public String toString() {
            String var10000 = this.operationId;
            return "MB.MBBuilder(operationId=" + var10000 + ", reqtoken=" + this.reqtoken + ", reqdata=" + String.valueOf(this.reqdata) + ")";
        }
    }
}
