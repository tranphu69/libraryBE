package com.example.library.service;

import com.example.library.constant.AppConstant;
import com.example.library.domain.Permission;
import com.example.library.dto.request.PermissionRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.PermissionResponse;
import com.example.library.exception.BusinessException;
import com.example.library.mapper.PermissionMapper;
import com.example.library.repository.PermissionRepository;
import com.example.library.repository.RoleRepository;
import com.example.library.service.service_impl.PermissionServiceImpl;
import com.example.library.service.service_import_impl.PermissionImportImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private PermissionMapper permissionMapper;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionImportImpl permissionImport;

    @InjectMocks
    private PermissionServiceImpl service;

    @Test
    void createShouldSaveAndReturnResponse() {
        PermissionRequest request = PermissionRequest.builder().code("VIEW_USERS").name("View Users").description("Desc").status(1L).build();
        Permission permission = Permission.builder().id(1L).code("VIEW_USERS").name("View Users").description("Desc").status(1L).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        PermissionResponse response = PermissionResponse.builder().id("1").code("VIEW_USERS").name("View Users").status(1L).build();

        when(permissionRepository.existsActiveCode("VIEW_USERS")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(permission);
        when(permissionMapper.toPermissionResponse(permission)).thenReturn(response);

        ApiResponse<PermissionResponse> result = service.create(request);

        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getData().getCode()).isEqualTo("VIEW_USERS");
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    void createShouldFailWhenCodeIsDuplicate() {
        PermissionRequest request = PermissionRequest.builder().code("VIEW_USERS").name("View Users").status(1L).build();
        when(permissionRepository.existsActiveCode("VIEW_USERS")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deleteShouldMarkPermissionDeletedWhenNoRoleUsesIt() {
        Permission permission = Permission.builder().id(1L).publicId("abc").status(1L).build();
        when(permissionRepository.findByPublicIdAndStatusNot("abc", -1L)).thenReturn(Optional.of(permission));
        when(roleRepository.existsByPermissionId(1L)).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(permission);

        ApiResponse<Void> result = service.delete("abc");

        assertThat(result.getSuccess()).isTrue();
        assertThat(permission.getStatus()).isEqualTo(-1L);
        verify(permissionRepository).save(permission);
    }
}
