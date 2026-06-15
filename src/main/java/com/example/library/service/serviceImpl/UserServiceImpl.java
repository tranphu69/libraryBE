package com.example.library.service.serviceImpl;

import com.example.library.constant.AppConstant;
import com.example.library.constant.PermissionConstant;
import com.example.library.constant.RoleConstant;
import com.example.library.constant.UserConstant;
import com.example.library.domain.Role;
import com.example.library.domain.User;
import com.example.library.dto.request.UserPageRequest;
import com.example.library.dto.request.UserRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.PaginationMeta;
import com.example.library.dto.response.UserResponse;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.UserMapper;
import com.example.library.repository.RoleRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.UserService;
import com.example.library.service.serviceImportImpl.UserImportImpl;
import com.example.library.util.DataUtils;
import com.example.library.util.ExcelUtils;
import com.example.library.util.ResponseUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class UserServiceImpl implements UserService {
    private final UserImportImpl userImport;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final String TEMPLATE_USER_IMPORT = "template/Template_user_import.xlsx";
    private final int NUMBER_RESULT = 4;

    private void validate(UserRequest request, Set<String> listRoleDB) {
        if(DataUtils.isBlank(request.getCode())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, UserConstant.CODE);
        } else if(DataUtils.maxLength(request.getCode(), UserConstant.MAX_LENGTH_CODE)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, UserConstant.CODE, UserConstant.CODE_LENGTH);
        } else if(!request.getCode().matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessException(ErrorCode.CODE_CHARACTER, UserConstant.CODE);
        } else if(userRepository.existsActiveEmail(request.getCode().trim().toUpperCase())) {
            throw new BusinessException(ErrorCode.NOT_DUPLICATE, UserConstant.CODE);
        }
        if(DataUtils.isBlank(request.getFullName())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, UserConstant.FULL_NAME);
        } else if(DataUtils.maxLength(request.getFullName(), UserConstant.MAX_LENGTH)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, UserConstant.FULL_NAME, UserConstant.COMMON_LENGTH);
        }
        if(DataUtils.isEmptyList(request.getListRole())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, UserConstant.LIST_ROLE);
        } else if(!listRoleDB.containsAll(request.getListRole())) {
            throw new BusinessException(ErrorCode.NOT_EXIST, UserConstant.NOT_EXIST_ROLE);
        }
    }

    @Override
    public ApiResponse<UserResponse> create(UserRequest request) {
        Set<String> listRoleDB = roleRepository.findAllPublicId();
        validate(request, listRoleDB);
        if(DataUtils.isBlank(request.getPassword())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, UserConstant.PASSWORD);
        } else if(DataUtils.maxLength(request.getPassword(), UserConstant.MAX_LENGTH_PASSWORD)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, UserConstant.PASSWORD, UserConstant.PASSWORD_LENGTH);
        }
        Set<Role> roles = new HashSet<>(
                roleRepository.findAllOfPermissionPublicId(request.getListRole())
        );
        User user = User.builder()
                .code(request.getCode().trim().toUpperCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        User saved = userRepository.save(user);
        UserResponse response = userMapper.toUserResponse(saved);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    public ApiResponse<UserResponse> update(UserRequest request) {
        User user = userRepository.findByIdAndIsDeletedNot(request.getId(), true)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, UserConstant.USER));
        Set<String> listRoleDB = roleRepository.findAllPublicId();
        validate(request, listRoleDB);
        Set<Role> roles = new HashSet<>(
                roleRepository.findAllOfPermissionPublicId(request.getListRole())
        );
        user.setCode(request.getCode().trim().toUpperCase());
        user.setFullName(request.getFullName().trim());
        user.setRoles(roles);
        user.setUpdatedAt(LocalDateTime.now());
        user.setMfaEnabled(request.getMfaEnabled());
        user.setIsLocked(request.getIsLocked());
        userRepository.save(user);
        UserResponse response = userMapper.toUserResponse(user);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    public ApiResponse<Void> delete(String id) {
        User user = userRepository.findByIdAndIsDeletedNot(id, true)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, UserConstant.USER));
        user.setIsDeleted(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return ResponseUtils.success(null, AppConstant.SUCCESS);
    }

    private void validateSearch(UserPageRequest request) {
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
        if(DataUtils.isEmptyList(request.getListRole())) {
            request.setListRole(null);
        }
    }

    @Override
    public ApiResponse<PageResponse<UserResponse>> search(UserPageRequest request) {
        validateSearch(request);
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<User> page = userRepository.search(request.getCode(), request.getFullName(), request.getEmail(), request.getListRole(), pageable);
        List<UserResponse> content = page.getContent().stream()
                .map(userMapper::toUserResponse).toList();
        PaginationMeta contentPage = PaginationMeta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
        PageResponse<UserResponse> result = PageResponse.<UserResponse>builder()
                .content(content)
                .pagination(contentPage)
                .build();
        return ResponseUtils.success(result, AppConstant.SUCCESS);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        ClassPathResource template = new ClassPathResource(TEMPLATE_USER_IMPORT);
        XSSFWorkbook workbook = new XSSFWorkbook(template.getInputStream());
        ServletOutputStream outputStream = response.getOutputStream();
        List<Role> listStatusActive = roleRepository.getAllStatusActive();
        Sheet sheet = workbook.getSheetAt(1);
        int rowIndex = 1;
        for (Role role : listStatusActive) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(role.getCode());
            row.createCell(1).setCellValue(role.getName());
        }
        workbook.write(outputStream);
        workbook.close();
        outputStream.flush();
    }

    @Override
    public void export(UserPageRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Export_role_" + LocalDate.now() + ".xlsx");
        String TEMPLATE_USER_EXPORT = "template/Template_user_export.xlsx";
        ClassPathResource template = new ClassPathResource(TEMPLATE_USER_EXPORT);
        try (Workbook workbook = new XSSFWorkbook(template.getInputStream());
             ServletOutputStream out = response.getOutputStream()) {
            List<User> users = userRepository.searchExport(request.getCode(), request.getFullName(), request.getEmail(), request.getListRole());
            ExcelUtils.writeSheet(workbook, 0, users,
                    user -> Arrays.asList(
                            user.getCode(),
                            user.getEmail(),
                            user.getFullName(),
                            String.join(", ",
                                    userRepository.getRoleCodesByUserId(user.getId()))
                    ), 1);
            List<Role> roles = roleRepository.getAllStatusActive();
            ExcelUtils.writeSheet(workbook, 1, roles,
                    role -> Arrays.asList(
                            role.getCode(),
                            role.getName()
                    ), 1);
            workbook.write(out);
        }
    }

    private void validateRowError(Set<String> listRoleDB, Set<String> listUserDB, List<String> errorMsg,
                                  String code, String password, String fullName, Set<String> result) {
        if(DataUtils.isBlank(code)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_EMPTY, UserConstant.CODE));
        } else if(DataUtils.maxLength(code, UserConstant.MAX_LENGTH_CODE)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_LENGTH, UserConstant.CODE,
                    UserConstant.CODE_LENGTH));
        } else if(!code.matches("^[a-zA-Z0-9_]+$")) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.CODE_CHARACTER, UserConstant.CODE));
        } else if(listUserDB.contains(code.trim().toUpperCase())) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_DUPLICATE, UserConstant.CODE));
        }
        if(DataUtils.isBlank(password)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_EMPTY, UserConstant.PASSWORD));
        } else if(DataUtils.maxLength(password, UserConstant.MAX_LENGTH_PASSWORD)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_LENGTH, UserConstant.PASSWORD,
                    UserConstant.PASSWORD_LENGTH));
        }
        if(DataUtils.isBlank(fullName)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_EMPTY, UserConstant.FULL_NAME));
        } else if(DataUtils.maxLength(fullName, UserConstant.MAX_LENGTH)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_LENGTH, UserConstant.FULL_NAME,
                    UserConstant.COMMON_LENGTH));
        }
        if(DataUtils.isEmptyList(result)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_EMPTY, UserConstant.LIST_ROLE));
        } else if(!listRoleDB.containsAll(result)) {
            errorMsg.add(DataUtils.strConcatenation(ErrorCode.NOT_EXIST, UserConstant.NOT_EXIST_ROLE));
        }
    }

    private void collectUser(String code, String password, String fullName,
                             List<User> batchInsert, Set<Role> listRole) {
        User user = User.builder()
                .code(code.trim().toUpperCase())
                .password(passwordEncoder.encode(password))
                .fullName(fullName.trim())
                .roles(listRole)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .failedLoginAttempts(0)
                .isActive(false)
                .isDeleted(false)
                .isEmailVerified(false)
                .mfaEnabled(false)
                .isLocked(false)
                .build();
        batchInsert.add(user);
    }

    private void validateRows(Sheet sheet, Set<String> listRoleDB, Set<String> listUserDB) {
        int BATCH_SIZE = 500;
        List<User> batchInsert = new ArrayList<>();
        for(int i = 1; i <= sheet.getLastRowNum(); i++) {
            List<String> errorMsg = new ArrayList<>();
            Row row = sheet.getRow(i);
            if(row == null) continue;
            Cell cellCode = row.getCell(0);
            String code = ExcelUtils.getCellValue(cellCode);
            Cell cellPassword = row.getCell(1);
            String password = ExcelUtils.getCellValue(cellPassword);
            Cell cellFullName = row.getCell(2);
            String fullName = ExcelUtils.getCellValue(cellFullName);
            Cell cellRoles = row.getCell(3);
            String roles = ExcelUtils.getCellValue(cellRoles);
            Set<String> result = Arrays.stream(roles.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
            validateRowError(listRoleDB, listUserDB, errorMsg, code, password, fullName, result);
            Set<Role> listRole = roleRepository.findByCodeInAndStatusNot(result, UserConstant.DELETED);
            if(!errorMsg.isEmpty()) {
                String errorMsgStr = String.join(", ", errorMsg);
                row.createCell(NUMBER_RESULT).setCellValue(AppConstant.ERROR_FILE + errorMsgStr);
            } else {
                collectUser(code, password, fullName, batchInsert, listRole);
                listUserDB.add(code.trim().toUpperCase());
                if(batchInsert.size() >= BATCH_SIZE) {
                    userImport.saveUser(batchInsert);
                    batchInsert.clear();
                }
                String errorMsgStr = AppConstant.SUCCESS_FILE;
                row.createCell(NUMBER_RESULT).setCellValue(errorMsgStr);
            }
        }
        if(!batchInsert.isEmpty()) {
            userImport.saveUser(batchInsert);
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
            Set<String> listRoleDB = roleRepository.findAllCodes();
            Set<String> listUserDB = userRepository.findAllCodes();
            validateRows(sheet, listRoleDB, listUserDB);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Override
    public byte[] importFile(MultipartFile file) {
        if(file == null || file.isEmpty()) throw new BusinessException(ErrorCode.NOT_FILE);
        if(file.getSize() > RoleConstant.MAX_FILE_SIZE) throw new BusinessException(ErrorCode.OVER_CAPACITY, "5");
        if(!ExcelUtils.hasExcelFormat(file)) throw new BusinessException(ErrorCode.NOT_FORMAT_FILE);
        final byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch(IOException e) {
            throw new BusinessException(ErrorCode.FILE_READ_ERROR, e);
        }
        try(InputStream templateIs = new ClassPathResource(TEMPLATE_USER_IMPORT).getInputStream()) {
            ExcelUtils.validateHeaders(templateIs, new ByteArrayInputStream(fileBytes));
            return buildResultWorkbook(fileBytes);
        } catch(IOException e) {
            throw new BusinessException(ErrorCode.FILE_READ_ERROR, e);
        }
    }
}
