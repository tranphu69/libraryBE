package com.example.library.controller;

import com.example.library.dto.request.RoleRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.RoleResponse;
import com.example.library.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {
    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController controller;

    @Test
    void createShouldDelegateToService() {
        RoleRequest request = RoleRequest.builder().code("ADMIN").name("Admin").status(1L).build();
        ApiResponse<RoleResponse> expected = new ApiResponse<>();
        expected.setSuccess(true);
        expected.setData(RoleResponse.builder().id("1").code("ADMIN").build());

        when(roleService.create(request)).thenReturn(expected);

        ResponseEntity<ApiResponse<RoleResponse>> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(roleService).create(request);
    }

    @Test
    void importFileShouldReturnFileResponse() {
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", "test".getBytes());
        when(roleService.importFile(any())).thenReturn("ok".getBytes());

        ResponseEntity<Object> response = controller.importFile(file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst("Content-Disposition")).contains("IMPORT_VAI_TRO_KET_QUA.xlsx");
        assertThat(response.getBody()).isEqualTo("ok".getBytes());
    }
}
