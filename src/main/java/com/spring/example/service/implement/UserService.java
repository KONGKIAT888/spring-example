package com.spring.example.service.implement;

import com.spring.example.entity.Role;
import com.spring.example.entity.User;
import com.spring.example.exception.DataExistsException;
import com.spring.example.exception.NotFoundException;
import com.spring.example.payload.request.SignUpRequest;
import com.spring.example.payload.request.UserRequest;
import com.spring.example.payload.response.PaginationResponse;
import com.spring.example.payload.response.UserResponse;
import com.spring.example.repository.RoleRepository;
import com.spring.example.repository.UserRepository;
import com.spring.example.service.IUserService;
import com.spring.example.util.EntityMapper;
import com.spring.example.util.GenericSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void create(SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new DataExistsException("username", signUpRequest.getUsername());
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new DataExistsException("email", signUpRequest.getEmail());
        }

        User user = EntityMapper.mapToEntity(signUpRequest, User.class);
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new NotFoundException("Role", "name", "user"));
        roles.add(userRole);

        user.setRoles(roles);

        userRepository.save(user);
    }

    @Override
    public void update(UserRequest userRequest, Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", "id", id.toString()));

        user.setName(userRequest.getName());

        userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", "id", id.toString()));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public PaginationResponse getAll(int page, int size, String filter, String sort, String keyword) {
        Sort sortDir = sort.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(filter).ascending() : Sort.by(filter).descending();
        Pageable pageable = PageRequest.of(page, size, sortDir);

        GenericSpecification<User> genericSpec = new GenericSpecification<>();
        List<String> fields = List.of("id", "name", "username", "email");

        Specification<User> spec = genericSpec.getSpecification(keyword, fields);
        Page<User> pages = userRepository.findAll(spec, pageable);
        List<UserResponse> content = pages.stream()
                .map(user -> EntityMapper.mapToResponse(user, UserResponse.class))
                .collect(Collectors.toList());

        return new PaginationResponse(content, page, size, pages.getTotalElements(), pages.getTotalPages(), pages.isLast());
    }


    @Override
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", "id", id.toString()));
        return EntityMapper.mapToResponse(user, UserResponse.class);
    }
}
