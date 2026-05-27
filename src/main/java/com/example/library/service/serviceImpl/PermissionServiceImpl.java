package com.example.library.service.serviceImpl;

import com.example.library.constant.AppConstant;
import com.example.library.constant.PermissionConstant;
import com.example.library.domain.Permission;
import com.example.library.dto.request.PermissionRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.PermissionResponse;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.mapper.PermissionMapper;
import com.example.library.repository.PermissionRepository;
import com.example.library.service.PermissionService;
import com.example.library.util.DataUtils;
import com.example.library.util.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final int MAX_LENGTH_CODE = 100;
    private final int MAX_LENGTH_NAME = 255;
    private final int MAX_LENGTH_DESCRIPTION = 1000;
    private final List<Long> listStatus = Arrays.asList(1L, 0L);

    private void validate(PermissionRequest request, List<String> listPermissionDB) {
        if(DataUtils.isBlank(request.getCode())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, PermissionConstant.CODE);
        } else if(DataUtils.maxLength(request.getCode(), MAX_LENGTH_CODE)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, PermissionConstant.CODE, PermissionConstant.CODE_LENGTH);
        } else if(listPermissionDB.contains(request.getCode().trim())) {
            throw new BusinessException(ErrorCode.NOT_DUPLICATE, PermissionConstant.CODE);
        }
        if(DataUtils.isBlank(request.getName())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, PermissionConstant.NAME);
        } else if(DataUtils.maxLength(request.getName(), MAX_LENGTH_NAME)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, PermissionConstant.NAME, PermissionConstant.NAME_LENGTH);
        }
        if(DataUtils.maxLengthNotEmpty(request.getDescription(), MAX_LENGTH_DESCRIPTION)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, PermissionConstant.DESCRIPTION, PermissionConstant.DESCRIPTION_LENGTH);
        }
        if(DataUtils.isNull(request.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, PermissionConstant.STATUS);
        } else if(!listStatus.contains(request.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_VALID, PermissionConstant.STATUS);
        }
    }

    @Override
    public ApiResponse<PermissionResponse> create(PermissionRequest request) {
        List<String> listPermissionDB = permissionRepository.findAllCodes();
        this.validate(request, listPermissionDB);
        Permission permission = Permission.builder()
                .code(request.getCode().trim())
                .name(request.getName().trim())
                .description(request.getDescription().trim())
                .status(request.getStatus())
                .build();
        permissionRepository.save(permission);
        Permission saved = permissionRepository.save(permission);
        PermissionResponse response = permissionMapper.toPermissionResponse(saved);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }
}
