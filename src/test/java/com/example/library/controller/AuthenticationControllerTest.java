package com.example.library.controller;

import com.example.library.dto.request.AuthenticationRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.AuthenticationResponse;
import com.example.library.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {
    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController controller;

    @Test
    void loginShouldDelegateToService() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername("admin");
        request.setPassword("Password123!");
        ApiResponse<AuthenticationResponse> expected = new ApiResponse<>();
        expected.setSuccess(true);
        expected.setData(AuthenticationResponse.builder().accessToken("access").refreshToken("refresh").build());

        when(authenticationService.logIn(request)).thenReturn(expected);

        ResponseEntity<ApiResponse<AuthenticationResponse>> response = controller.logIn(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(authenticationService).logIn(request);
    }
}
