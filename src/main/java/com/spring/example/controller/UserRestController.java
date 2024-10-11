package com.spring.example.controller;

import com.spring.example.payload.request.SignUpRequest;
import com.spring.example.payload.request.UserRequest;
import com.spring.example.payload.response.PaginationResponse;
import com.spring.example.payload.response.UserResponse;
import com.spring.example.service.implement.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.spring.example.util.AppConstants.*;

@RestController
@RequestMapping("/v1/users")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<String> create(@Valid @RequestBody SignUpRequest signUpRequest) {
        userService.create(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@Valid @RequestBody UserRequest userRequest, @PathVariable Long id) {
        userService.update(userRequest, id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping()
    public PaginationResponse getAll(@RequestParam(value = "page", defaultValue = DEFAULT_PAGE, required = false) int page,
                                     @RequestParam(value = "size", defaultValue = DEFAULT_SIZE, required = false) int size,
                                     @RequestParam(value = "filter", defaultValue = DEFAULT_FILTER, required = false) String filter,
                                     @RequestParam(value = "sort", defaultValue = DEFAULT_SORT, required = false) String sort,
                                     @RequestParam(value = "search", required = false) String keyword) {
        return userService.getAll(page, size, filter, sort, keyword);
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }
}
