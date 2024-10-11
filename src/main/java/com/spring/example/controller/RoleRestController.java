package com.spring.example.controller;

import com.spring.example.payload.request.RoleRequest;
import com.spring.example.payload.response.PaginationResponse;
import com.spring.example.payload.response.RoleResponse;
import com.spring.example.service.implement.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.spring.example.util.AppConstants.*;

@RestController
@RequestMapping("/v1/roles")
public class RoleRestController {

    private final RoleService roleService;

    public RoleRestController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping()
    public ResponseEntity<String> create(@Valid @RequestBody RoleRequest roleRequest) {
        roleService.create(roleRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@Valid @RequestBody RoleRequest roleRequest, @PathVariable Long id) {
        roleService.update(roleRequest, id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping()
    public PaginationResponse getAll(@RequestParam(value = "page", defaultValue = DEFAULT_PAGE, required = false) int page,
                                     @RequestParam(value = "size", defaultValue = DEFAULT_SIZE, required = false) int size,
                                     @RequestParam(value = "filter", defaultValue = DEFAULT_FILTER, required = false) String filter,
                                     @RequestParam(value = "sort", defaultValue = DEFAULT_SORT, required = false) String sort,
                                     @RequestParam(value = "search", required = false) String keyword) {
        return roleService.getAll(page, size, filter, sort, keyword);
    }

    @GetMapping("/{id}")
    public RoleResponse getById(@PathVariable Long id) {
        return roleService.getById(id);
    }
}
