package com.example.library.controller;

import com.example.library.dto.request.PermissionRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.PermissionResponse;
import com.example.library.service.PermissionService;
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
class PermissionControllerTest {
    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private PermissionController controller;

    @Test
    void createShouldDelegateToService() {
        PermissionRequest request = PermissionRequest.builder().code("VIEW_USERS").name("View Users").status(1L).build();
        ApiResponse<PermissionResponse> expected = new ApiResponse<>();
        expected.setSuccess(true);
        expected.setData(PermissionResponse.builder().id("1").code("VIEW_USERS").build());

        when(permissionService.create(request)).thenReturn(expected);

        ResponseEntity<ApiResponse<PermissionResponse>> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(permissionService).create(request);
    }

    @Test
    void importFileShouldReturnFileResponse() {
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", "test".getBytes());
        when(permissionService.importFile(any())).thenReturn("ok".getBytes());

        ResponseEntity<Object> response = controller.importFile(file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst("Content-Disposition")).contains("IMPORT_QUYEN_KET_QUA.xlsx");
        assertThat(response.getBody()).isEqualTo("ok".getBytes());
    }
}
