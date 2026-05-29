package com.example.library.util;

import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
        return Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xls") ||
                file.getOriginalFilename().endsWith(".xlsx");
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

    public static void addDropdownToColumn(XSSFWorkbook workbook, XSSFSheet sheet, int columnIndex,
                                           String[] values, int firstRow, int lastRow) {
        XSSFSheet hiddenSheet = workbook.createSheet("HiddenData");
        workbook.setSheetHidden(workbook.getSheetIndex(hiddenSheet), true);
        for (int i = 0; i < values.length; i++) {
            XSSFRow row = hiddenSheet.createRow(i);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue(values[i]);
        }
        String rangeName = "DropdownData_Col" + columnIndex;
        Name namedRange = workbook.createName();
        namedRange.setNameName(rangeName);
        namedRange.setRefersToFormula("HiddenData!$A$1:$A$" + values.length);
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = validationHelper.createFormulaListConstraint(rangeName);
        CellRangeAddressList addressList = new CellRangeAddressList(
                firstRow, lastRow, columnIndex, columnIndex);
        DataValidation dataValidation = validationHelper.createValidation(
                constraint, addressList);
        dataValidation.setSuppressDropDownArrow(false);
        dataValidation.setShowErrorBox(true);
        sheet.addValidationData(dataValidation);
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

    public static <T> void writeWithTemplate(HttpServletResponse response, String templatePath, String fileName,
                                      List<T> data, Function<T, List<Object>> rowMapper, int startRow) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName + "_" + LocalDate.now() + ".xlsx");
        try (InputStream template = ExcelUtils.class.getClassLoader().getResourceAsStream(templatePath)) {
            assert template != null;
            try (Workbook workbook = new XSSFWorkbook(template);
                 ServletOutputStream out = response.getOutputStream()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = startRow;
                for (T item : data) {
                    Row row = sheet.createRow(rowIndex++);
                    List<Object> values = rowMapper.apply(item);
                    for (int i = 0; i < values.size(); i++) {
                        setCellValue(row.createCell(i), values.get(i));
                    }
                }
                workbook.write(out);
            }
        }
    }
}
