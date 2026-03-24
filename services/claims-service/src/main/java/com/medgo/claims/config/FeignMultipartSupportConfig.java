package com.medgo.claims.config;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


@Slf4j
@Configuration
public class FeignMultipartSupportConfig {


    private static final byte[] CONTENT_TYPE_TEXT_PLAIN = "Content-Type: text/plain".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] CONTENT_TYPE_JSON = "Content-Type: application/json".getBytes(StandardCharsets.US_ASCII);





    @Bean
    @Primary
    public Encoder feignFormEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        Encoder delegate = new SpringEncoder(messageConverters);
        return new SpringFormEncoderWithContentTypeCorrection(delegate);
    }


    private static class SpringFormEncoderWithContentTypeCorrection extends SpringFormEncoder {

        public SpringFormEncoderWithContentTypeCorrection(Encoder delegate) {
            super(delegate);
        }

        @Override
        public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
            super.encode(object, bodyType, template);

            // Fix Content-Type for String/JSON parts in multipart using byte-level replacement only.
            // Do NOT decode the full body as UTF-8 – it contains binary file parts; decoding would corrupt images.
            if (template.body() != null && template.body().length > 0) {
                try {
                    byte[] body = template.body();
                    int idx = indexOf(body, CONTENT_TYPE_TEXT_PLAIN);
                    if (idx >= 0) {
                        log.debug("Found Content-Type: text/plain in multipart body, fixing to application/json (byte-level)");
                        byte[] corrected = replaceBytes(body, idx, CONTENT_TYPE_TEXT_PLAIN.length, CONTENT_TYPE_JSON);
                        template.body(corrected, null);
                        log.info("Successfully fixed Content-Type from text/plain to application/json in multipart request");
                    }
                } catch (Exception e) {
                    log.warn("Failed to fix Content-Type in multipart request: {}", e.getMessage());
                }
            }
        }

        /** Find first occurrence of needle in haystack. */
        private static int indexOf(byte[] haystack, byte[] needle) {
            if (needle.length == 0 || needle.length > haystack.length) return -1;
            for (int i = 0; i <= haystack.length - needle.length; i++) {
                if (matches(haystack, i, needle)) return i;
            }
            return -1;
        }

        private static boolean matches(byte[] haystack, int start, byte[] needle) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[start + j] != needle[j]) return false;
            }
            return true;
        }

        /** New array: body[0..idx), replacement, body[idx+len..end). */
        private static byte[] replaceBytes(byte[] body, int idx, int len, byte[] replacement) {
            byte[] out = new byte[body.length - len + replacement.length];
            System.arraycopy(body, 0, out, 0, idx);
            System.arraycopy(replacement, 0, out, idx, replacement.length);
            System.arraycopy(body, idx + len, out, idx + replacement.length, body.length - idx - len);
            return out;
        }
    }
}



