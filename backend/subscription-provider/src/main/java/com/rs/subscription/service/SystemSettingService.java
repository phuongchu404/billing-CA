package com.rs.subscription.service;

import com.rs.subscription.dto.response.SystemSettingResponse;
import com.rs.subscription.entity.SystemSetting;
import com.rs.subscription.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

public interface SystemSettingService {

    List<SystemSettingResponse> getCategory(String category);

    String get(String key, String defaultValue);

    boolean getBoolean(String key);

    int getInt(String key, int defaultValue);

    List<SystemSettingResponse> updateCategory(String category, Map<String, String> updates);
}
