package com.example.library.service.serviceImpl;

import com.example.library.constant.AppConstant;
import com.example.library.constant.PermissionConstant;
import com.example.library.domain.Permission;
import com.example.library.dto.request.PermissionPageRequest;
import com.example.library.dto.request.PermissionRequest;
import com.example.library.dto.response.*;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.PermissionMapper;
import com.example.library.repository.PermissionRepository;
import com.example.library.service.PermissionService;
import com.example.library.util.DataUtils;
import com.example.library.util.ExcelUtils;
import com.example.library.util.ResponseUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final List<Long> listStatus = Arrays.asList(1L, 0L);
    private final String TEMPLATE_PERMISSION = "template/Template_permission.xlsx";

    private void validate(PermissionRequest request, List<String> listPermissionDB) {
        if(DataUtils.isBlank(request.getCode())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, PermissionConstant.CODE);
        } else if(DataUtils.maxLength(request.getCode(), PermissionConstant.MAX_LENGTH_CODE)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, PermissionConstant.CODE, PermissionConstant.CODE_LENGTH);
        } else if(!request.getCode().matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessException(ErrorCode.CODE_CHARACTER, PermissionConstant.CODE);
        } else if(listPermissionDB.contains(request.getCode().trim())) {
            throw new BusinessException(ErrorCode.NOT_DUPLICATE, PermissionConstant.CODE);
        }
        if(DataUtils.isBlank(request.getName())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, PermissionConstant.NAME);
        } else if(DataUtils.maxLength(request.getName(), PermissionConstant.MAX_LENGTH_NAME)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, PermissionConstant.NAME, PermissionConstant.NAME_LENGTH);
        }
        if(DataUtils.maxLengthNotEmpty(request.getDescription(), PermissionConstant.MAX_LENGTH_DESCRIPTION)) {
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
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName().trim())
                .description(request.getDescription().trim())
                .status(request.getStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Permission saved = permissionRepository.save(permission);
        PermissionResponse response = permissionMapper.toPermissionResponse(saved);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    public ApiResponse<PermissionResponse> update(PermissionRequest request) {
        Permission permission = permissionRepository.findByPublicId(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, PermissionConstant.PERMISSION));
        List<String> listPermissionDB = permissionRepository.findAllCodesOtherPublicId(request.getId());
        this.validate(request, listPermissionDB);
        permission.setCode(request.getCode().trim().toUpperCase());
        permission.setName(request.getName().trim());
        permission.setDescription(request.getDescription().trim());
        permission.setStatus(request.getStatus());
        permission.setUpdatedAt(LocalDateTime.now());
        permissionRepository.save(permission);
        PermissionResponse response = permissionMapper.toPermissionResponse(permission);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    public ApiResponse<Void> delete(String id) {
        Permission permission = permissionRepository.findByPublicId(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, PermissionConstant.PERMISSION));
        permission.setStatus(PermissionConstant.DELETED);
        permission.setUpdatedAt(LocalDateTime.now());
        permissionRepository.save(permission);
        return ResponseUtils.success(null, AppConstant.SUCCESS);
    }

    private void validateSearch(PermissionPageRequest request) {
        if(DataUtils.isNull(request.getPage())) {
            request.setPage(0);
        }
        if(DataUtils.isNull(request.getSize())) {
            request.setSize(10);
        }
        if(DataUtils.isBlank(request.getSortBy())) {
            request.setSortBy(PermissionConstant.UPDATED_AT);
        }
        if(DataUtils.isBlank(request.getSortDir())) {
            request.setSortDir(PermissionConstant.DESC);
        }
    }

    @Override
    public ApiResponse<PageResponse<PermissionResponse>> search(PermissionPageRequest request) {
        this.validateSearch(request);
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Permission> page = permissionRepository.search(request.getCode(), request.getName(), request.getStatus(), pageable);
        List<PermissionResponse> content = page.getContent().stream()
                .map(permissionMapper::toPermissionResponse).toList();
        PaginationMeta contentPage = PaginationMeta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
        PageResponse<PermissionResponse> result = PageResponse.<PermissionResponse>builder()
                .content(content)
                .pagination(contentPage)
                .build();
        return ResponseUtils.success(result, AppConstant.SUCCESS);
    }

    @Override
    public ApiResponse<ListResponse<PermissionResponse>> getAllStatusActive() {
        List<Permission> listStatusActive = permissionRepository.getAllStatusActive();
        List<PermissionResponse> content = listStatusActive.stream()
                .map(permissionMapper::toPermissionResponse).toList();
        ListResponse<PermissionResponse> result = ListResponse.<PermissionResponse>builder()
                .content(content)
                .total(content.size())
                .build();
        return ResponseUtils.success(result, AppConstant.SUCCESS);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        ClassPathResource template = new ClassPathResource(TEMPLATE_PERMISSION);
        XSSFWorkbook workbook = new XSSFWorkbook(template.getInputStream());
        String[] names = {"0", "1"};
        XSSFSheet sheet0 = workbook.getSheetAt(0);
        ExcelUtils.addDropdownToColumn(workbook, sheet0, 3, names, 1, 1000);
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.flush();
    }

    @Override
    public void export(PermissionPageRequest request, HttpServletResponse response) throws IOException {
        List<Permission> listExport = permissionRepository.searchExport(request.getCode(), request.getName(), request.getStatus());
        ExcelUtils.writeWithTemplate(
                response,
                TEMPLATE_PERMISSION,
                "Export_permission",
                listExport,
                permission -> List.of(
                        permission.getCode(),
                        permission.getName(),
                        permission.getDescription(),
                        permission.getStatus()
                ),
                1
        );
    }
}
