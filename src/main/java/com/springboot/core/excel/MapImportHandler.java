package com.springboot.core.excel;

import cn.afterturn.easypoi.handler.impl.ExcelDataHandlerDefaultImpl;

import java.util.Map;

/**
 * https://gitee.com/lemur/easypoi-test/blob/master/src/test/java/cn/afterturn/easypoi/test/excel/read/MapImportHandler.java
 */

public class MapImportHandler extends ExcelDataHandlerDefaultImpl<Map<String, String>> {
    @Override
    public void setMapValue(Map<String, Object> map, String originKey, Object value) {
        super.setMapValue(map, originKey, value);
    }

    private String getRealKey(String originKey) {
        if (originKey.equals("交易账户")) {
            return "accountNo";
        }
        if (originKey.equals("姓名")) {
            return "name";
        }
        if (originKey.equals("客户类型")) {
            return "type";
        }
        return originKey;
    }
}
