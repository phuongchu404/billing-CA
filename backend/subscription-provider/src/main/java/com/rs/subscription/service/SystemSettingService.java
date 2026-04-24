package com.rs.subscription.service;

import com.rs.subscription.dto.response.SystemSettingResponse;
import com.rs.subscription.entity.SystemSetting;
import com.rs.subscription.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemSettingService {

    private final SystemSettingRepository settingRepo;

    public List<SystemSettingResponse> getCategory(String category) {
        return settingRepo.findByCategoryOrderBySettingKey(category.toUpperCase())
            .stream().map(this::toResponse).toList();
    }

    public String get(String key, String defaultValue) {
        return settingRepo.findById(key)
            .map(SystemSetting::getValue)
            .filter(v -> v != null && !v.isBlank())
            .orElse(defaultValue);
    }

    public boolean getBoolean(String key) {
        return "true".equalsIgnoreCase(get(key, "false"));
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Transactional
    public List<SystemSettingResponse> updateCategory(String category, Map<String, String> updates) {
        List<SystemSetting> settings = settingRepo.findByCategoryOrderBySettingKey(category.toUpperCase());
        for (SystemSetting s : settings) {
            if (updates.containsKey(s.getSettingKey())) {
                s.setValue(updates.get(s.getSettingKey()));
                settingRepo.save(s);
            }
        }
        return getCategory(category);
    }

    private SystemSettingResponse toResponse(SystemSetting s) {
        SystemSettingResponse r = new SystemSettingResponse();
        r.setKey(s.getSettingKey());
        r.setValue(s.getValue() != null ? s.getValue() : "");
        r.setType(s.getSettingType() != null ? s.getSettingType() : "STRING");
        r.setCategory(s.getCategory());
        r.setDescription(s.getDescription());
        return r;
    }
}
