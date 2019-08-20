package com.springboot;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelCascade {

    private static final int MAX_LENGTH = 1000;

    public static void main(String[] args) {
        SXSSFWorkbook book = new SXSSFWorkbook();
        book.createSheet("省市");
        Map<String, List<String>> data = new HashMap<>();
        List<String> a = new ArrayList<>();
        a.add("南京市");
        a.add("苏州市");
        data.put("江苏省", a);

        List<String> b = new ArrayList<>();
        b.add("合肥市");
        b.add("安庆市");
        b.add("安庆市1");
        data.put("安徽省", b);
        Cascade(book, data, 9);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream("D:/AAAAA.xlsx");
            book.write(os);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    public static void Cascade(SXSSFWorkbook book, Map<String, List<String>> data, int index) {
        String hiddenSheet = "hidden";
        SXSSFSheet sheetPro = book.getSheetAt(0);
        //创建一个专门用来存放地区信息的隐藏sheet页
        //因此也不能在现实页之前创建，否则无法隐藏。
        Sheet hideSheet = book.createSheet(hiddenSheet);
        //book.setSheetHidden(book.getSheetIndex(hideSheet), true);

        int rowId = 0;
        // 设置第一行，存省的信息
        Row provinceRow = hideSheet.createRow(rowId++);
        provinceRow.createCell(0).setCellValue("目标人群列表");
        String[] provinceArr = data.keySet().toArray(new String[]{});
        for (int i = 0; i < provinceArr.length; i++) {
            Cell provinceCell = provinceRow.createCell(i + 1);
            provinceCell.setCellValue(provinceArr[i]);
        }
        // 将具体的数据写入到每一行中，行开头为父级区域，后面是子区域。
        for (String key : provinceArr) {
            String[] son = data.get(key).toArray(new String[]{});
            Row row = hideSheet.createRow(rowId++);
            row.createCell(0).setCellValue(key);
            for (int j = 0; j < son.length; j++) {
                Cell cell = row.createCell(j + 1);
                cell.setCellValue(son[j]);
            }
            // 添加名称管理器
            String range = getRange(1, rowId, son.length);
            Name name = book.createName();
            //key不可重复
            name.setNameName(key);
            String formula = hiddenSheet + "!" + range;
            name.setRefersToFormula(formula);
        }


        DataValidationHelper dvHelper = sheetPro.getDataValidationHelper();
        // 省规则
        DataValidationConstraint provConstraint = dvHelper.createExplicitListConstraint(provinceArr);
        // 四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList provRangeAddressList = new CellRangeAddressList(1, MAX_LENGTH, index - 1, index - 1);
        DataValidation provinceDataValidation = dvHelper.createValidation(provConstraint, provRangeAddressList);
        provinceDataValidation.setShowErrorBox(true);
        provinceDataValidation.setSuppressDropDownArrow(true);
        sheetPro.addValidationData(provinceDataValidation);

        //对前MAX_LENGTH行设置有效性
        String startColumns = String.valueOf((char) (65 + index - 1));
        for (int i = 2; i < MAX_LENGTH; i++) {
            setDataValidation(startColumns, sheetPro, i, index);
        }
    }


    /**
     * 设置有效性
     *
     * @param offset 主影响单元格所在列，即此单元格由哪个单元格影响联动
     * @param sheet
     * @param rowNum 行数
     * @param colNum 列数
     */
    public static void setDataValidation(String offset, SXSSFSheet sheet, int rowNum, int colNum) {
        DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        DataValidation data_validation_list;
        data_validation_list = getDataValidationByFormula(
                "INDIRECT($" + offset + (rowNum) + ")", rowNum, colNum, dvHelper);
        sheet.addValidationData(data_validation_list);
    }

    /**
     * 加载下拉列表内容
     *
     * @param formulaString
     * @param naturalRowIndex
     * @param naturalColumnIndex
     * @param dvHelper
     * @return
     */
    private static DataValidation getDataValidationByFormula(
            String formulaString, int naturalRowIndex, int naturalColumnIndex, DataValidationHelper dvHelper) {
        // 加载下拉列表内容
        // 举例：若formulaString = "INDIRECT($A$2)" 表示规则数据会从名称管理器中获取key与单元格 A2 值相同的数据，
        //如果A2是江苏省，那么此处就是江苏省下的市信息。
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint(formulaString);
        // 设置数据有效性加载在哪个单元格上。
        // 四个参数分别是：起始行、终止行、起始列、终止列
        int firstRow = naturalRowIndex - 1;
        int lastRow = naturalRowIndex - 1;
        int firstCol = naturalColumnIndex;
        int lastCol = naturalColumnIndex;
        CellRangeAddressList regions = new CellRangeAddressList(firstRow,
                lastRow, firstCol, lastCol);
        // 数据有效性对象
        // 绑定
        XSSFDataValidation data_validation_list = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, regions);
        data_validation_list.setEmptyCellAllowed(false);
        if (data_validation_list instanceof XSSFDataValidation) {
            data_validation_list.setSuppressDropDownArrow(true);
            data_validation_list.setShowErrorBox(true);
        } else {
            data_validation_list.setSuppressDropDownArrow(false);
        }
        data_validation_list.createPromptBox("下拉选择提示", "请使用下拉方式选择合适的值！");
        return data_validation_list;
    }

    /**
     * 计算formula
     *
     * @param offset   偏移量，如果给0，表示从A列开始，1，就是从B列
     * @param rowId    第几行
     * @param colCount 一共多少列
     * @return 如果给入参 1,1,10. 表示从B1-K1。最终返回 $B$1:$K$1
     */
    public static String getRange(int offset, int rowId, int colCount) {
        char start = (char) ('A' + offset);
        if (colCount <= 25) {
            char end = (char) (start + colCount - 1);
            return "$" + start + "$" + rowId + ":$" + end + "$" + rowId;
        } else {
            char endPrefix = 'A';
            char endSuffix = 'A';
            if ((colCount - 25) / 26 == 0 || colCount == 51) {// 26-51之间，包括边界（仅两次字母表计算）
                if ((colCount - 25) % 26 == 0) {// 边界值
                    endSuffix = (char) ('A' + 25);
                } else {
                    endSuffix = (char) ('A' + (colCount - 25) % 26 - 1);
                }
            } else {// 51以上
                if ((colCount - 25) % 26 == 0) {
                    endSuffix = (char) ('A' + 25);
                    endPrefix = (char) (endPrefix + (colCount - 25) / 26 - 1);
                } else {
                    endSuffix = (char) ('A' + (colCount - 25) % 26 - 1);
                    endPrefix = (char) (endPrefix + (colCount - 25) / 26);
                }
            }
            return "$" + start + "$" + rowId + ":$" + endPrefix + endSuffix + "$" + rowId;
        }
    }

}

