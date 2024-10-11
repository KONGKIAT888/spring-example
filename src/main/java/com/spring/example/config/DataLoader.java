package com.spring.example.config;

import com.spring.example.entity.Role;
import com.spring.example.entity.User;
import com.spring.example.repository.RoleRepository;
import com.spring.example.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Transactional
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (roleRepository.count() == 0) {
            String[] roles = {"ADMIN", "USER"};
            List<Role> roleList = new ArrayList<>();
            for (String roleName : roles) {
                Role role = new Role();
                role.setName(roleName);
                roleList.add(role);
            }
            roleRepository.saveAll(roleList);
        }

        if (userRepository.count() == 0) {
            String[] users = {"admin", "user"};
            List<User> userList = new ArrayList<>();
            for (String username : users) {
                User user = new User();
                user.setName(username);
                user.setUsername(username);
                user.setEmail(username + "@example.com");
                user.setPassword(passwordEncoder.encode("12345"));
                if ("admin".equals(username)) {
                    user.setRoles(Set.of(roleRepository.findByName("ADMIN").get()));
                } else if ("user".equals(username)) {
                    user.setRoles(Set.of(roleRepository.findByName("USER").get()));
                }
                userList.add(user);
            }
            userRepository.saveAll(userList);
        }
    }
}
