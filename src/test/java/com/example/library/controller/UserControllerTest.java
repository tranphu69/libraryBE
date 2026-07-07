package com.example.library.controller;

import com.example.library.dto.request.UserRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.UserResponse;
import com.example.library.service.UserService;
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
class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    @Test
    void createShouldDelegateToService() {
        UserRequest request = UserRequest.builder().code("USER01").fullName("User One").build();
        ApiResponse<UserResponse> expected = new ApiResponse<>();
        expected.setSuccess(true);
        expected.setData(UserResponse.builder().id("1").code("USER01").build());

        when(userService.create(request)).thenReturn(expected);

        ResponseEntity<ApiResponse<UserResponse>> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(userService).create(request);
    }

    @Test
    void importFileShouldReturnFileResponse() {
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", "test".getBytes());
        when(userService.importFile(any())).thenReturn("ok".getBytes());

        ResponseEntity<Object> response = controller.importFile(file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst("Content-Disposition")).contains("IMPORT_VAI_TRO_KET_QUA.xlsx");
        assertThat(response.getBody()).isEqualTo("ok".getBytes());
    }
}
