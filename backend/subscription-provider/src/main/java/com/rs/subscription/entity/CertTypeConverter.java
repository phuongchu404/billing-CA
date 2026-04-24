package com.rs.subscription.entity;

import com.rs.subscription.entity.CertificateProvisioningRecord.CertType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CertTypeConverter implements AttributeConverter<CertType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(CertType certType) {
        if (certType == null) return CertType.INDIVIDUAL.getValue();
        return certType.getValue();
    }

    @Override
    public CertType convertToEntityAttribute(Integer value) {
        if (value == null) return CertType.INDIVIDUAL;
        for (CertType type : CertType.values()) {
            if (type.getValue() == value) return type;
        }
        return CertType.INDIVIDUAL;
    }
}
