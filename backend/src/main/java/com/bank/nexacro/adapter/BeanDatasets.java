package com.bank.nexacro.adapter;

import java.beans.PropertyDescriptor;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/** VO ↔ {@link NexacroDataset} 변환 헬퍼 (Service 는 그대로 VO 를 쓰고, 변환은 Controller 계층에서만 한다). */
public final class BeanDatasets {

    /** Date 는 Nexacro 쪽에서 STRING(yyyyMMddHHmmss) 으로 받는다. */
    private static final String DATE_PATTERN = "yyyyMMddHHmmss";

    private BeanDatasets() {
    }

    /** VO 목록 → Dataset. 컬럼은 VO 클래스의 프로퍼티에서 만든다 (목록이 비어도 컬럼은 유지). */
    public static NexacroDataset fromBeans(String datasetId, Class<?> type, List<?> beans) {
        NexacroDataset ds = new NexacroDataset(datasetId);
        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(type);
        for (PropertyDescriptor pd : pds) {
            if (pd.getReadMethod() == null || "class".equals(pd.getName())) {
                continue;
            }
            ds.addColumn(pd.getName(), columnType(pd.getPropertyType()));
        }
        if (beans != null) {
            for (Object bean : beans) {
                fillRow(ds, bean);
            }
        }
        return ds;
    }

    /** VO 한 건 → Dataset (1행) */
    public static NexacroDataset fromBean(String datasetId, Class<?> type, Object bean) {
        return fromBeans(datasetId, type, bean == null ? null : Collections.singletonList(bean));
    }

    private static void fillRow(NexacroDataset ds, Object bean) {
        BeanWrapper bw = new BeanWrapperImpl(bean);
        Map<String, Object> row = ds.addRow();
        for (String col : ds.getColumns().keySet()) {
            Object v = bw.getPropertyValue(col);
            if (v instanceof Date) {
                v = new SimpleDateFormat(DATE_PATTERN).format((Date) v);
            }
            row.put(col, v);
        }
    }

    /** Dataset 첫 행 → VO. 빈 문자열은 건너뛴다(기본형 프로퍼티 변환 오류 방지). */
    public static <T> T toBean(NexacroDataset ds, Class<T> type) {
        T bean = BeanUtils.instantiateClass(type);
        Map<String, Object> row = ds.firstRow();
        if (row == null) {
            return bean;
        }
        BeanWrapper bw = new BeanWrapperImpl(bean);
        for (Map.Entry<String, Object> e : row.entrySet()) {
            Object v = e.getValue();
            if (v == null || v.toString().length() == 0 || !bw.isWritableProperty(e.getKey())) {
                continue;
            }
            bw.setPropertyValue(e.getKey(), v);
        }
        return bean;
    }

    private static String columnType(Class<?> c) {
        if (c == int.class || c == Integer.class || c == short.class || c == Short.class) {
            return NexacroDataset.INT;
        }
        if (c == long.class || c == double.class || c == float.class || Number.class.isAssignableFrom(c)) {
            return NexacroDataset.BIGDECIMAL;
        }
        return NexacroDataset.STRING;
    }
}
