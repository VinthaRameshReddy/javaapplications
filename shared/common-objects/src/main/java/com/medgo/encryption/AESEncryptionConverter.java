package com.medgo.encryption;

import com.medgo.constant.AppConstants;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JPA converter that transparently encrypts / decrypts PII fields.
 *
 * <p>Marks the class both as a Spring bean and as a JPA converter.
 * Using constructor injection guarantees that the {@code piiEncryption}
 * flag is set even when Hibernate, not Spring, instantiates the class.</p>
 */
@Converter(autoApply = true)        // let Hibernate pick it up
@Component                           // still a Spring bean
public class AESEncryptionConverter
        implements AttributeConverter<String, String> {

    private final String piiEncryption;

    public AESEncryptionConverter(
            @Value("${app.service.piiEncryption}") String piiEncryption) {
        this.piiEncryption = piiEncryption;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;

        return AppConstants.YES.equalsIgnoreCase(piiEncryption)
                ? AESEncryptionUtil.encrypt(attribute)   // static call – no new object
                : attribute;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;

        return AppConstants.YES.equalsIgnoreCase(piiEncryption)
                ? AESEncryptionUtil.decrypt(dbData)      // static call – no new object
                : dbData;
    }
}
