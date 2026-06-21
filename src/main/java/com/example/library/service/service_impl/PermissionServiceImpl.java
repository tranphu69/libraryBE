package com.example.library.service.service_impl;

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
import com.example.library.repository.RoleRepository;
import com.example.library.service.PermissionService;
import com.example.library.service.service_import_impl.PermissionImportImpl;
import com.example.library.util.DataUtils;
import com.example.library.util.ExcelUtils;
import com.example.library.util.ResponseUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final RoleRepository roleRepository;
    private final PermissionImportImpl permissionImport;
    private final List<Long> listStatus = Arrays.asList(1L, 0L);
    private static final String TEMPLATE_PERMISSION = "template/Template_permission.xlsx";
    private static final int NUMBER_RESULT = 4;
    private static final String REGEX = "\\w+";

    private void validate(PermissionRequest request) {
        if(DataUtils.isBlank(request.getCode())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, PermissionConstant.CODE);
        } else if(DataUtils.maxLength(request.getCode(), PermissionConstant.MAX_LENGTH_CODE)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, PermissionConstant.CODE, PermissionConstant.CODE_LENGTH);
        } else if(!request.getCode().matches(REGEX)) {
            throw new BusinessException(ErrorCode.CODE_CHARACTER, PermissionConstant.CODE);
        } else if(permissionRepository.existsActiveCode(request.getCode().trim().toUpperCase())) {
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
    @Transactional
    public ApiResponse<PermissionResponse> create(PermissionRequest request) {
        log.info("Creating new permission with code: {}", request.getCode());
        validate(request);
        Permission permission = Permission.builder()
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName().trim())
                .description(request.getDescription().trim())
                .status(request.getStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Permission saved = permissionRepository.save(permission);
        log.info("Permission created successfully with id: {}", saved.getId());
        PermissionResponse response = permissionMapper.toPermissionResponse(saved);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    @Transactional
    public ApiResponse<PermissionResponse> update(PermissionRequest request) {
        log.info("Updating permission with id: {}", request.getId());
        Permission permission = permissionRepository.findByPublicIdAndStatusNot(request.getId(), PermissionConstant.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, PermissionConstant.PERMISSION));
        validate(request);
        permission.setCode(request.getCode().trim().toUpperCase());
        permission.setName(request.getName().trim());
        permission.setDescription(request.getDescription().trim());
        permission.setStatus(request.getStatus());
        permission.setUpdatedAt(LocalDateTime.now());
        permissionRepository.save(permission);
        log.info("Permission updated successfully with id: {}", permission.getId());
        PermissionResponse response = permissionMapper.toPermissionResponse(permission);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    @Transactional
    public ApiResponse<Void> delete(String id) {
        log.info("Deleting permission with id: {}", id);
        Permission permission = permissionRepository.findByPublicIdAndStatusNot(id, PermissionConstant.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, PermissionConstant.PERMISSION));
        if(roleRepository.existsByPermissionId(permission.getId())) {
            throw new BusinessException(ErrorCode.NOT_DELETE, PermissionConstant.PERMISSION);
        }
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
        validateSearch(request);
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
    public ApiResponse<ListResponse<SimpleResponse>> getAllStatusActive() {
        List<Permission> listStatusActive = permissionRepository.getAllStatusActive();
        List<SimpleResponse> content = listStatusActive.stream()
                .map(permissionMapper::toSimpleResponse).toList();
        ListResponse<SimpleResponse> result = ListResponse.<SimpleResponse>builder()
                .content(content)
                .total(content.size())
                .build();
        return ResponseUtils.success(result, AppConstant.SUCCESS);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        ClassPathResource template = new ClassPathResource(TEMPLATE_PERMISSION);
        XSSFWorkbook workbook = new XSSFWorkbook(template.getInputStream());
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.flush();
    }

    @Override
    public void export(PermissionPageRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Export_permission_" + LocalDate.now() + ".xlsx");
        ClassPathResource template = new ClassPathResource(TEMPLATE_PERMISSION);
        try (Workbook workbook = new XSSFWorkbook(template.getInputStream());
             ServletOutputStream out = response.getOutputStream()) {
            List<Permission> listExport = permissionRepository.searchExport(request.getCode(), request.getName(), request.getStatus());
            ExcelUtils.writeSheet(workbook, 0, listExport,
                    permission -> List.of(
                            permission.getCode(),
                            permission.getName(),
                            permission.getDescription(),
                            permission.getStatus()
                    ), 1);
            workbook.write(out);
        }
    }

    private void validateRowError(Set<String> listPermissionDB, List<String> errorMsg,
                                  String code, String name, String description, String status) {
        if(DataUtils.isBlank(code)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_EMPTY, PermissionConstant.CODE));
        } else if(DataUtils.maxLength(code, PermissionConstant.MAX_LENGTH_CODE)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_LENGTH,
                    PermissionConstant.CODE, PermissionConstant.CODE_LENGTH));
        } else if(!code.matches(REGEX)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.CODE_CHARACTER, PermissionConstant.CODE));
        } else if(listPermissionDB.contains(code.trim().toUpperCase())) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_DUPLICATE, PermissionConstant.CODE));
        }
        if(DataUtils.isBlank(name)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_EMPTY, PermissionConstant.NAME));
        } else if(DataUtils.maxLength(name, PermissionConstant.MAX_LENGTH_NAME)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_LENGTH,
                    PermissionConstant.NAME, PermissionConstant.NAME_LENGTH));
        }
        if(DataUtils.maxLengthNotEmpty(description, PermissionConstant.MAX_LENGTH_DESCRIPTION)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_LENGTH,
                    PermissionConstant.DESCRIPTION, PermissionConstant.DESCRIPTION_LENGTH));
        }
        if(DataUtils.isBlank(status)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_EMPTY, PermissionConstant.STATUS));
        } else if(DataUtils.isNumber(status)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_VALID, PermissionConstant.STATUS));
        } else if(!listStatus.contains(Long.parseLong(status))) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_VALID, PermissionConstant.STATUS));
        }
    }

    private void collectPermission(String code, String name, String description,
                                   Long status, List<Permission> batchInsert) {
        Permission permission = Permission.builder()
                .code(code.toUpperCase())
                .name(name)
                .description(description)
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        batchInsert.add(permission);
    }

    private void validateRows(Sheet sheet, Set<String> listPermissionDB) {
        List<Permission> batchInsert = new ArrayList<>();
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
            validateRowError(listPermissionDB, errorMsg, code, name, description, status);
            if(!errorMsg.isEmpty()) {
                String errorMsgStr = String.join(", ", errorMsg);
                row.createCell(NUMBER_RESULT).setCellValue(AppConstant.ERROR_FILE + errorMsgStr);
            } else {
                collectPermission(code, name, description, Long.parseLong(status), batchInsert);
                listPermissionDB.add(code.trim().toUpperCase());
                if(batchInsert.size() >= 500) {
                    permissionImport.savePermission(batchInsert);
                    batchInsert.clear();
                }
                String errorMsgStr = AppConstant.SUCCESS_FILE;
                row.createCell(NUMBER_RESULT).setCellValue(errorMsgStr);
            }
        }
        if(!batchInsert.isEmpty()) {
            permissionImport.savePermission(batchInsert);
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
            validateRows(sheet, listPermissionDB);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Override
    @Transactional
    public byte[] importFile(MultipartFile file) {
        log.info("Starting import permissions from file: {}", file != null ? file.getOriginalFilename() : "null");
        if(file == null || file.isEmpty()) throw new BusinessException(ErrorCode.NOT_FILE);
        if(file.getSize() > PermissionConstant.MAX_FILE_SIZE) throw new BusinessException(ErrorCode.OVER_CAPACITY, "5");
        if(!ExcelUtils.hasExcelFormat(file)) throw new BusinessException(ErrorCode.NOT_FORMAT_FILE);
        final byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch(IOException e) {
            throw new BusinessException(ErrorCode.FILE_READ_ERROR, e);
        }
        try(InputStream templateIs = new ClassPathResource(TEMPLATE_PERMISSION).getInputStream()) {
            ExcelUtils.validateHeaders(templateIs, new ByteArrayInputStream(fileBytes));
            byte[] result = buildResultWorkbook(fileBytes);
            log.info("Import permissions completed successfully");
            return result;
        } catch(IOException e) {
            log.error("Error importing permissions file", e);
            throw new BusinessException(ErrorCode.FILE_READ_ERROR, e);
        }
    }
}
