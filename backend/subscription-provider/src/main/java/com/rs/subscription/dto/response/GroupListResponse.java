package com.rs.subscription.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GroupListResponse {
    private List<GroupListItemResponse> list;
    private long activeCount;
}
