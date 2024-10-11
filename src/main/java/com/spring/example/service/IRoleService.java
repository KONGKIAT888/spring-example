package com.spring.example.service;

import com.spring.example.payload.request.RoleRequest;
import com.spring.example.payload.response.PaginationResponse;
import com.spring.example.payload.response.RoleResponse;

public interface IRoleService {
    void create(RoleRequest roleRequest);

    void update(RoleRequest roleRequest, Long id);

    void delete(Long id);

    PaginationResponse getAll(int page, int size, String filter, String sort, String keyword);

    RoleResponse getById(Long id);
}
