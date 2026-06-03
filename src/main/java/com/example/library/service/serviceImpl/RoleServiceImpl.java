package com.example.library.service.serviceImpl;

import com.example.library.constant.AppConstant;
import com.example.library.constant.PermissionConstant;
import com.example.library.constant.RoleConstant;
import com.example.library.domain.Permission;
import com.example.library.domain.Role;
import com.example.library.dto.request.RolePageRequest;
import com.example.library.dto.request.RoleRequest;
import com.example.library.dto.response.*;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.RoleMapper;
import com.example.library.repository.PermissionRepository;
import com.example.library.repository.RoleRepository;
import com.example.library.service.RoleService;
import com.example.library.util.DataUtils;
import com.example.library.util.ExcelUtils;
import com.example.library.util.ResponseUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Slf4j
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionRepository permissionRepository;
    private final String TEMPLATE_ROLE = "template/Template_role.xlsx";

    private final List<Long> listStatus = Arrays.asList(1L, 0L);

    private void validate(RoleRequest request, Set<String> listRoleDB, Set<Long> listIdPermissionDB) {
        if(DataUtils.isBlank(request.getCode())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, RoleConstant.CODE);
        } else if(DataUtils.maxLength(request.getCode(), RoleConstant.MAX_LENGTH_CODE)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, RoleConstant.CODE, RoleConstant.CODE_LENGTH);
        } else if(!request.getCode().matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessException(ErrorCode.CODE_CHARACTER, RoleConstant.CODE);
        } else if(listRoleDB.contains(request.getCode().trim().toUpperCase())) {
            throw new BusinessException(ErrorCode.NOT_DUPLICATE, RoleConstant.CODE);
        }
        if(DataUtils.isBlank(request.getName())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, RoleConstant.NAME);
        } else if(DataUtils.maxLength(request.getName(), RoleConstant.MAX_LENGTH_NAME)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, RoleConstant.NAME, RoleConstant.NAME_LENGTH);
        }
        if(DataUtils.maxLengthNotEmpty(request.getDescription(), RoleConstant.MAX_LENGTH_DESCRIPTION)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, RoleConstant.DESCRIPTION, RoleConstant.DESCRIPTION_LENGTH);
        }
        if(DataUtils.isNull(request.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, RoleConstant.STATUS);
        } else if(!listStatus.contains(request.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_VALID, RoleConstant.STATUS);
        }
        if(DataUtils.isEmptyList(request.getListPermission())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, RoleConstant.LIST_PERMISSION);
        } else if(!listIdPermissionDB.containsAll(request.getListPermission())) {
            throw new BusinessException(ErrorCode.NOT_EXIST, RoleConstant.NOT_EXIST_PERMISSION);
        }
    }

    @Override
    public ApiResponse<RoleResponse> create(RoleRequest request) {
        Set<String> listRoleDB = roleRepository.findAllCodes();
        Set<Long> listIdPermissionDB = permissionRepository.findAllId();
        validate(request, listRoleDB, listIdPermissionDB);
        Set<Permission> permissions = new HashSet<>(
                permissionRepository.findAllById(request.getListPermission())
        );
        Role role = Role.builder()
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName().trim())
                .description(request.getDescription().trim())
                .status(request.getStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .permissions(permissions)
                .build();
        Role saved = roleRepository.save(role);
        RoleResponse response = roleMapper.toRoleResponse(saved);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    public ApiResponse<RoleResponse> update(RoleRequest request) {
        Role role = roleRepository.findByPublicIdAndStatusNot(request.getId(), RoleConstant.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, RoleConstant.ROLE));
        Set<String> listRoleDB = roleRepository.findAllCodesOtherPublicId(request.getId());
        Set<Long> listIdPermissionDB = permissionRepository.findAllId();
        validate(request, listRoleDB, listIdPermissionDB);
        Set<Permission> permissions = new HashSet<>(
                permissionRepository.findAllById(request.getListPermission())
        );
        role.setCode(request.getCode().trim().toUpperCase());
        role.setName(request.getName().trim());
        role.setDescription(request.getDescription().trim());
        role.setStatus(request.getStatus());
        role.setUpdatedAt(LocalDateTime.now());
        role.setPermissions(permissions);
        roleRepository.save(role);
        RoleResponse response = roleMapper.toRoleResponse(role);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    public ApiResponse<Void> delete(String id) {
        Role role = roleRepository.findByPublicIdAndStatusNot(id, RoleConstant.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, RoleConstant.ROLE));
        role.setStatus(RoleConstant.DELETED);
        role.setUpdatedAt(LocalDateTime.now());
        roleRepository.save(role);
        return ResponseUtils.success(null, AppConstant.SUCCESS);
    }

    private void validateSearch(RolePageRequest request) {
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
        if(DataUtils.isEmptyList(request.getListPermission())) {
            request.setListPermission(null);
        }
    }

    @Override
    public ApiResponse<PageResponse<RoleResponse>> search(RolePageRequest request) {
        validateSearch(request);
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Role> page = roleRepository.search(request.getCode(), request.getName(), request.getListPermission(), request.getStatus(), pageable);
        List<RoleResponse> content = page.getContent().stream()
                .map(roleMapper::toRoleResponse).toList();
        PaginationMeta contentPage = PaginationMeta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
        PageResponse<RoleResponse> result = PageResponse.<RoleResponse>builder()
                .content(content)
                .pagination(contentPage)
                .build();
        return ResponseUtils.success(result, AppConstant.SUCCESS);
    }

    @Override
    public ApiResponse<ListResponse<SimpleResponse>> getAllStatusActive() {
        List<Role> listStatusActive = roleRepository.getAllStatusActive();
        List<SimpleResponse> content = listStatusActive.stream()
                .map(roleMapper::toSimpleResponse).toList();
        ListResponse<SimpleResponse> result = ListResponse.<SimpleResponse>builder()
                .content(content)
                .total(content.size())
                .build();
        return ResponseUtils.success(result, AppConstant.SUCCESS);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        ClassPathResource template = new ClassPathResource(TEMPLATE_ROLE);
        XSSFWorkbook workbook = new XSSFWorkbook(template.getInputStream());
        ServletOutputStream outputStream = response.getOutputStream();
        List<Permission> listStatusActive = permissionRepository.getAllStatusActive();
        Sheet sheet = workbook.getSheetAt(1);
        int rowIndex = 1;
        for (Permission permission : listStatusActive) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(permission.getPublicId());
            row.createCell(1).setCellValue(permission.getCode());
            row.createCell(2).setCellValue(permission.getName());
        }
        workbook.write(outputStream);
        workbook.close();
        outputStream.flush();
    }

    @Override
    public void export(RolePageRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Export_role_" + LocalDate.now() + ".xlsx");
        ClassPathResource template = new ClassPathResource(TEMPLATE_ROLE);
        try (Workbook workbook = new XSSFWorkbook(template.getInputStream());
             ServletOutputStream out = response.getOutputStream()) {
            List<Role> roles = roleRepository.searchExport(request.getCode(), request.getName(),
                    request.getListPermission(), request.getStatus());
            ExcelUtils.writeSheet(workbook, 0, roles,
                    role -> List.of(
                            role.getCode(),
                            role.getName(),
                            role.getDescription(),
                            role.getStatus(),
                            String.join(", ",
                                    roleRepository.getPermissionCodesByRoleId(role.getId()))
                    ), 1);
            List<Permission> permissions = permissionRepository.getAllStatusActive();
            ExcelUtils.writeSheet(workbook, 1, permissions,
                    permission -> List.of(
                            permission.getPublicId(),
                            permission.getCode(),
                            permission.getName()
                    ), 1);
            workbook.write(out);
        }
    }
}
