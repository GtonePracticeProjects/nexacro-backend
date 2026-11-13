package com.bank.nexacro.adapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 벤더 라이브러리에 의존하지 않는 최소 Dataset 모델.
 * 컬럼(id → type) 과 행(컬럼id → 값) 만 가진다. 벤더 jar(xapi/uiadapter)를 쓸 경우
 * {@link NexacroAdapter} 구현체 안에서 벤더 DataSet 과 이 모델을 상호 변환하면 된다.
 */
public class NexacroDataset {

    public static final String STRING = "STRING";
    public static final String INT = "INT";
    public static final String BIGDECIMAL = "BIGDECIMAL";

    private final String id;
    private final Map<String, String> columns = new LinkedHashMap<String, String>();
    private final List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

    public NexacroDataset(String id) {
        this.id = id;
    }

    public String getId() { return id; }

    public Map<String, String> getColumns() { return columns; }

    public List<Map<String, Object>> getRows() { return rows; }

    public NexacroDataset addColumn(String columnId, String type) {
        columns.put(columnId, type);
        return this;
    }

    public Map<String, Object> addRow() {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        rows.add(row);
        return row;
    }

    /** 첫 번째 행 (없으면 null) */
    public Map<String, Object> firstRow() {
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** 첫 번째 행의 문자열 값 (없거나 빈 값이면 null) */
    public String getString(String columnId) {
        Map<String, Object> row = firstRow();
        if (row == null) {
            return null;
        }
        Object v = row.get(columnId);
        return (v == null || v.toString().length() == 0) ? null : v.toString();
    }
}
