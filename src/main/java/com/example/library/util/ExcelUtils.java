package com.example.library.util;

import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ExcelUtils {
    public static boolean hasExcelFormat(MultipartFile file) {
        return !Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xls") ||
                !file.getOriginalFilename().endsWith(".xlsx");
    }

    public static String getCellValue(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    public static List<String> extractHeaders(InputStream is) {
        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException(ErrorCode.NOT_FORMAT_FILE);
            }
            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                headers.add(getCellValue(cell).trim());
            }
            return headers;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_READ_ERROR, e);
        }
    }

    public static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", "").toLowerCase();
    }

    public static void validateHeaders(List<String> templateHeaders, List<String> uploadHeaders) {
        if (templateHeaders.size() != uploadHeaders.size()) {
            throw new BusinessException(ErrorCode.CHECK_TEMPLATE);
        }
        for (int i = 0; i < templateHeaders.size(); i++) {
            if (!normalize(templateHeaders.get(i)).equals(normalize(uploadHeaders.get(i)))) {
                throw new BusinessException(ErrorCode.CHECK_TEMPLATE);
            }
        }
    }

    public static void validateHeaders(InputStream templateIs, InputStream uploadIs) {
        try {
            List<String> templateHeaders = extractHeaders(templateIs);
            List<String> uploadHeaders = extractHeaders(uploadIs);
            validateHeaders(templateHeaders, uploadHeaders);
        } catch(Exception e) {
            throw new BusinessException(ErrorCode.FILE_READ_ERROR, e);
        }
    }

    private static void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String s) {
            cell.setCellValue(s);
        } else if (value instanceof Integer i) {
            cell.setCellValue(i);
        } else if (value instanceof Long l) {
            cell.setCellValue(l);
        } else if (value instanceof Double d) {
            cell.setCellValue(d);
        } else if (value instanceof Boolean b) {
            cell.setCellValue(b);
        } else if (value instanceof LocalDateTime ldt) {
            cell.setCellValue(ldt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        } else if (value instanceof LocalDate ld) {
            cell.setCellValue(ld.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            cell.setCellValue(value.toString());
        }
    }

    public static <T> void writeSheet(
            Workbook workbook,
            int sheetIndex,
            List<T> data,
            Function<T, List<Object>> rowMapper,
            int startRow
    ) {
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        int rowIndex = startRow;
        for (T item : data) {
            Row row = sheet.createRow(rowIndex++);
            List<Object> values = rowMapper.apply(item);
            for (int i = 0; i < values.size(); i++) {
                setCellValue(row.createCell(i), values.get(i));
            }
        }
    }
}
