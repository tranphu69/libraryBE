package com.example.library.service;

import com.example.library.domain.Permission;
import com.example.library.domain.Role;
import com.example.library.dto.request.RoleRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.RoleResponse;
import com.example.library.exception.BusinessException;
import com.example.library.mapper.RoleMapper;
import com.example.library.repository.PermissionRepository;
import com.example.library.repository.RoleRepository;
import com.example.library.service.service_impl.RoleServiceImpl;
import com.example.library.service.service_import_impl.RoleImportImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private RoleImportImpl roleImport;

    @InjectMocks
    private RoleServiceImpl service;

    @Test
    void createShouldSaveRoleWhenPermissionsExist() {
        RoleRequest request = RoleRequest.builder().code("ADMIN").name("Admin").description("Desc").status(1L).listPermission(Set.of("perm-1")).build();
        Permission permission = Permission.builder().id(1L).publicId("perm-1").build();
        Role role = Role.builder().id(1L).code("ADMIN").name("Admin").status(1L).build();
        RoleResponse response = RoleResponse.builder().id("1").code("ADMIN").name("Admin").build();

        when(permissionRepository.findAllPublicId()).thenReturn(Set.of("perm-1"));
        when(permissionRepository.findAllOfPermissionPublicId(Set.of("perm-1"))).thenReturn(Set.of(permission));
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(roleMapper.toRoleResponse(role)).thenReturn(response);

        ApiResponse<RoleResponse> result = service.create(request);

        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getData().getCode()).isEqualTo("ADMIN");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void createShouldFailWhenPermissionDoesNotExist() {
        RoleRequest request = RoleRequest.builder().code("ADMIN").name("Admin").description("Desc").status(1L).listPermission(Set.of("perm-1")).build();
        when(permissionRepository.findAllPublicId()).thenReturn(Set.of());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deleteShouldMarkRoleDeleted() {
        Role role = Role.builder().id(1L).publicId("role-1").status(1L).build();
        when(roleRepository.findByPublicIdAndStatusNot("role-1", -1L)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        ApiResponse<Void> result = service.delete("role-1");

        assertThat(result.getSuccess()).isTrue();
        assertThat(role.getStatus()).isEqualTo(-1L);
    }
}
