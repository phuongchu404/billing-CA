package com.rs.subscription.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupListResponse {
    private List<GroupListItemResponse> list;
    private long activeCount;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
