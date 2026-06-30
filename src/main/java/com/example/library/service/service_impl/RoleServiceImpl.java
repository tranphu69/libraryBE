package com.example.library.service.service_impl;

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
import com.example.library.service.service_import_impl.RoleImportImpl;
import com.example.library.util.DataUtils;
import com.example.library.util.ExcelUtils;
import com.example.library.util.ResponseUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionRepository permissionRepository;
    private final RoleImportImpl roleImport;
    private static final String TEMPLATE_ROLE = "template/Template_role.xlsx";
    private final List<Long> listStatus = Arrays.asList(1L, 0L);
    private static final int NUMBER_RESULT = 5;
    private static final String REGEX = "\\w+";

    private void validate(RoleRequest request, Set<String> listIdPermissionDB) {
        if(DataUtils.isBlank(request.getCode())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, RoleConstant.CODE);
        } else if(DataUtils.maxLength(request.getCode(), RoleConstant.MAX_LENGTH_CODE)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, RoleConstant.CODE, RoleConstant.CODE_LENGTH);
        } else if(!request.getCode().matches(REGEX)) {
            throw new BusinessException(ErrorCode.CODE_CHARACTER, RoleConstant.CODE);
        } else if(roleRepository.existsActiveCode(request.getCode().trim().toUpperCase())) {
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
    @Transactional
    public ApiResponse<RoleResponse> create(RoleRequest request) {
        log.info("Creating new role with code: {}", request.getCode());
        Set<String> listIdPermissionDB = permissionRepository.findAllPublicId();
        validate(request, listIdPermissionDB);
        Set<Permission> permissions = new HashSet<>(
                permissionRepository.findAllOfPermissionPublicId(request.getListPermission())
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
        log.info("Role created successfully with id: {}", saved.getId());
        RoleResponse response = roleMapper.toRoleResponse(saved);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    @Transactional
    public ApiResponse<RoleResponse> update(RoleRequest request) {
        log.info("Updating role with id: {}", request.getId());
        Role role = roleRepository.findByPublicIdAndStatusNot(request.getId(), RoleConstant.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, RoleConstant.ROLE));
        Set<String> listIdPermissionDB = permissionRepository.findAllPublicId();
        validate(request, listIdPermissionDB);
        Set<Permission> permissions = new HashSet<>(
                permissionRepository.findAllOfPermissionPublicId(request.getListPermission())
        );
        role.setCode(request.getCode().trim().toUpperCase());
        role.setName(request.getName().trim());
        role.setDescription(request.getDescription().trim());
        role.setStatus(request.getStatus());
        role.setUpdatedAt(LocalDateTime.now());
        role.setPermissions(permissions);
        roleRepository.save(role);
        log.info("Role updated successfully with id: {}", role.getId());
        RoleResponse response = roleMapper.toRoleResponse(role);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    @Transactional
    public ApiResponse<Void> delete(String id) {
        log.info("Deleting role with id: {}", id);
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
            row.createCell(0).setCellValue(permission.getCode());
            row.createCell(1).setCellValue(permission.getName());
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
                    role -> Arrays.asList(
                            role.getCode(),
                            role.getName(),
                            role.getDescription(),
                            role.getStatus(),
                            String.join(", ",
                                    roleRepository.getPermissionCodesByRoleId(role.getId()))
                    ), 1);
            List<Permission> permissions = permissionRepository.getAllStatusActive();
            ExcelUtils.writeSheet(workbook, 1, permissions,
                    permission -> Arrays.asList(
                            permission.getCode(),
                            permission.getName()
                    ), 1);
            workbook.write(out);
        }
    }

    private void validateName(String name, List<String> errorMsg, String description) {
        if(DataUtils.isBlank(name)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_EMPTY, RoleConstant.NAME));
        } else if(DataUtils.maxLength(name, RoleConstant.MAX_LENGTH_NAME)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_LENGTH,
                    RoleConstant.NAME, RoleConstant.NAME_LENGTH));
        }
        if(DataUtils.maxLengthNotEmpty(description, RoleConstant.MAX_LENGTH_DESCRIPTION)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_LENGTH,
                    RoleConstant.DESCRIPTION, RoleConstant.DESCRIPTION_LENGTH));
        }
    }

    private void validateRowError(Set<String> listPermissionDB, Set<String> listRoleDB, List<String> errorMsg,
                                  String code, String status, Set<String> result) {
        if(DataUtils.isBlank(code)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_EMPTY, RoleConstant.CODE));
        } else if(DataUtils.maxLength(code, RoleConstant.MAX_LENGTH_CODE)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_LENGTH,
                    RoleConstant.CODE, RoleConstant.CODE_LENGTH));
        } else if(!code.matches(REGEX)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.CODE_CHARACTER, RoleConstant.CODE));
        } else if(listRoleDB.contains(code.trim().toUpperCase())) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_DUPLICATE, RoleConstant.CODE));
        }
        if(DataUtils.isBlank(status)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_EMPTY, RoleConstant.STATUS));
        } else if(DataUtils.isNumber(status)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_VALID, RoleConstant.STATUS));
        } else if(!listStatus.contains(Long.parseLong(status))) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_VALID, RoleConstant.STATUS));
        }
        if(DataUtils.isEmptyList(result)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_EMPTY, RoleConstant.LIST_PERMISSION));
        } else if(!listPermissionDB.containsAll(result)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_EXIST, RoleConstant.NOT_EXIST_PERMISSION));
        }
    }

    private void collectRole(String code, String name, String description,
                             Long status, List<Role> batchInsert, Set<Permission> permissions) {
        Role role = Role.builder()
                .code(code.toUpperCase())
                .name(name)
                .description(description)
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .permissions(permissions)
                .build();
        batchInsert.add(role);
    }

    private void validateRows(Sheet sheet, Set<String> listPermissionDB, Set<String> listRoleDB) {
        List<Role> batchInsert = new ArrayList<>();
        for(int i = 1; i <= sheet.getLastRowNum(); i++) {
            List<String> errorMsg = new ArrayList<>();
            Row row = sheet.getRow(i);
            if(row == null) continue;
            Cell cellCode = row.getCell(0);
            String code = ExcelUtils.getCellValue(cellCode);
            Cell cellName = row.getCell(1);
            String name = ExcelUtils.getCellValue(cellName);
            Cell cellDescription = row.getCell(2);
            String description = ExcelUtils.getCellValue(cellDescription);
            Cell cellStatus = row.getCell(3);
            String status = ExcelUtils.getCellValue(cellStatus);
            Cell cellPermissions = row.getCell(4);
            String permissions = ExcelUtils.getCellValue(cellPermissions);
            Set<String> result = Arrays.stream(permissions.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
            validateName(name, errorMsg, description);
            validateRowError(listPermissionDB, listRoleDB, errorMsg, code, status, result);
            Set<Permission> listPermission = permissionRepository.findByCodeInAndStatusNot(result, RoleConstant.DELETED);
            if(!errorMsg.isEmpty()) {
                String errorMsgStr = String.join(", ", errorMsg);
                row.createCell(NUMBER_RESULT).setCellValue(AppConstant.ERROR_FILE + errorMsgStr);
            } else {
                collectRole(code, name, description, Long.parseLong(status), batchInsert, listPermission);
                listRoleDB.add(code.trim().toUpperCase());
                if(batchInsert.size() >= 500) {
                    roleImport.saveRole(batchInsert);
                    batchInsert.clear();
                }
                String errorMsgStr = AppConstant.SUCCESS_FILE;
                row.createCell(NUMBER_RESULT).setCellValue(errorMsgStr);
            }
        }
        if(!batchInsert.isEmpty()) {
            roleImport.saveRole(batchInsert);
            batchInsert.clear();
        }
    }

    private byte[] buildResultWorkbook(byte[] fileBytes) throws IOException {
        try (
                Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileBytes));
                ByteArrayOutputStream out = new ByteArrayOutputStream();
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            sheet.setColumnWidth(NUMBER_RESULT, 5000);
            Row header = sheet.getRow(0);
            Cell errorCell = header.createCell(NUMBER_RESULT);
            errorCell.setCellValue("Kết quả trả về");
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            errorCell.setCellStyle(style);
            Set<String> listPermissionDB = permissionRepository.findAllCodes();
            Set<String> listRoleDB = roleRepository.findAllCodes();
            validateRows(sheet, listPermissionDB, listRoleDB);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Override
    @Transactional
    public byte[] importFile(MultipartFile file) {
        log.info("Starting import roles from file: {}", file != null ? file.getOriginalFilename() : "null");
        if(file == null || file.isEmpty()) throw new BusinessException(ErrorCode.NOT_FILE);
        if(file.getSize() > RoleConstant.MAX_FILE_SIZE) throw new BusinessException(ErrorCode.OVER_CAPACITY, "5");
        if(ExcelUtils.hasExcelFormat(file)) throw new BusinessException(ErrorCode.NOT_FORMAT_FILE);
        final byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch(IOException e) {
            throw new BusinessException(ErrorCode.FILE_READ_ERROR, e);
        }
        try(InputStream templateIs = new ClassPathResource(TEMPLATE_ROLE).getInputStream()) {
            ExcelUtils.validateHeaders(templateIs, new ByteArrayInputStream(fileBytes));
            byte[] result = buildResultWorkbook(fileBytes);
            log.info("Import roles completed successfully");
            return result;
        } catch(IOException e) {
            log.error("Error importing roles file", e);
            throw new BusinessException(ErrorCode.FILE_READ_ERROR, e);
        }
    }
}
