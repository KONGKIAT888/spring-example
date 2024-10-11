package com.spring.example.service;

import com.spring.example.payload.request.SignUpRequest;
import com.spring.example.payload.request.UserRequest;
import com.spring.example.payload.response.PaginationResponse;
import com.spring.example.payload.response.UserResponse;

public interface IUserService {
    void create(SignUpRequest signUpRequest);

    void update(UserRequest userRequest, Long id);

    void delete(Long id);

    PaginationResponse getAll(int page, int size, String filter, String sort, String keyword);

    UserResponse getById(Long id);
}
