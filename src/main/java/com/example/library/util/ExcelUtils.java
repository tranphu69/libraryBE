package com.example.library.util;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

public class ExcelUtils {
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
        dataValidation.setSuppressDropDownArrow(true);
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
