package com.example.library.util;

import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtils {
    public static void addDropdownToColumn(XSSFWorkbook workbook, XSSFSheet sheet,
                                           int columnIndex, String[] values,
                                           int firstRow, int lastRow) {
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
}
