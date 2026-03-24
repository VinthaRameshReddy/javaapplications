package com.medgo.bean;//
// Source statusCode recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.fasterxml.jackson.annotation.JsonProperty;
import com.medgo.bean.MB;
import lombok.Generated;

public class RequestResponse {
    @JsonProperty("mb")
    private MB mb;

    public RequestResponse(MB mb) {
        this.mb = mb;
    }

    public RequestResponse() {
    }

    public MB getMb() {
        return this.mb;
    }

    public void setMb(MB mb) {
        this.mb = mb;
    }

    @Generated
    public static RequestResponseBuilder builder() {
        return new RequestResponseBuilder();
    }

    @Generated
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof RequestResponse)) {
            return false;
        } else {
            RequestResponse other = (RequestResponse)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$mb = this.getMb();
                Object other$mb = other.getMb();
                if (this$mb == null) {
                    if (other$mb != null) {
                        return false;
                    }
                } else if (!this$mb.equals(other$mb)) {
                    return false;
                }

                return true;
            }
        }
    }

    @Generated
    protected boolean canEqual(final Object other) {
        return other instanceof RequestResponse;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $mb = this.getMb();
        result = result * 59 + ($mb == null ? 43 : $mb.hashCode());
        return result;
    }

    @Generated
    public String toString() {
        return "RequestResponse(mb=" + String.valueOf(this.getMb()) + ")";
    }

    @Generated
    public static class RequestResponseBuilder {
        @Generated
        private MB mb;

        @Generated
        RequestResponseBuilder() {
        }

        @JsonProperty("mb")
        @Generated
        public RequestResponseBuilder mb(final MB mb) {
            this.mb = mb;
            return this;
        }

        @Generated
        public RequestResponse build() {
            return new RequestResponse(this.mb);
        }

        @Generated
        public String toString() {
            return "RequestResponse.RequestResponseBuilder(mb=" + String.valueOf(this.mb) + ")";
        }
    }
}
