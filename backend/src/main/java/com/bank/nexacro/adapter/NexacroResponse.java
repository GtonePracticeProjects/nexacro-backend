package com.bank.nexacro.adapter;

import java.util.LinkedHashMap;
import java.util.Map;

/** 서버 응답. ErrorCode(0 = 성공, 그 외 = 오류) / ErrorMsg 와 출력 Dataset. */
public class NexacroResponse {

    private int errorCode = 0;
    private String errorMsg = "SUCCESS";
    private final Map<String, String> variables = new LinkedHashMap<String, String>();
    private final Map<String, NexacroDataset> datasets = new LinkedHashMap<String, NexacroDataset>();

    public static NexacroResponse ok() {
        return new NexacroResponse();
    }

    public static NexacroResponse error(int errorCode, String errorMsg) {
        NexacroResponse r = new NexacroResponse();
        r.errorCode = errorCode;
        r.errorMsg = errorMsg;
        return r;
    }

    public NexacroResponse addDataset(NexacroDataset ds) {
        datasets.put(ds.getId(), ds);
        return this;
    }

    public NexacroResponse addVariable(String name, String value) {
        variables.put(name, value);
        return this;
    }

    public int getErrorCode() { return errorCode; }
    public String getErrorMsg() { return errorMsg; }
    public Map<String, String> getVariables() { return variables; }
    public Map<String, NexacroDataset> getDatasets() { return datasets; }
}
