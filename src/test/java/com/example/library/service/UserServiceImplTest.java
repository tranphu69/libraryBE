package com.example.library.service;

import com.example.library.domain.Role;
import com.example.library.domain.User;
import com.example.library.dto.request.UserRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.UserResponse;
import com.example.library.exception.BusinessException;
import com.example.library.mapper.UserMapper;
import com.example.library.repository.RoleRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.service_impl.UserServiceImpl;
import com.example.library.service.service_import_impl.UserImportImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserImportImpl userImport;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void createShouldSaveUserWhenPasswordIsValid() {
        UserRequest request = UserRequest.builder().code("USER01").fullName("User One").password("Password123!").listRole(Set.of("role-1")).build();
        Role role = Role.builder().id(1L).publicId("role-1").build();
        User user = User.builder().id("u1").code("USER01").fullName("User One").build();
        UserResponse response = UserResponse.builder().id("u1").code("USER01").fullName("User One").build();

        when(roleRepository.findAllPublicId()).thenReturn(Set.of("role-1"));
        when(roleRepository.findAllOfPermissionPublicId(Set.of("role-1"))).thenReturn(Set.of(role));
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(response);
        when(userRepository.existsActiveCode("USER01")).thenReturn(false);

        ApiResponse<UserResponse> result = service.create(request);

        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getData().getCode()).isEqualTo("USER01");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createShouldFailWhenPasswordIsWeak() {
        UserRequest request = UserRequest.builder().code("USER01").fullName("User One").password("weak").listRole(Set.of("role-1")).build();

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class);
    }
}
