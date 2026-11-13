package com.bank.nexacro.adapter;

import java.util.LinkedHashMap;
import java.util.Map;

/** 클라이언트가 보낸 Dataset(inDatasets) 과 Variable(strArgument) 묶음. */
public class NexacroRequest {

    private final Map<String, String> variables = new LinkedHashMap<String, String>();
    private final Map<String, NexacroDataset> datasets = new LinkedHashMap<String, NexacroDataset>();

    public Map<String, String> getVariables() { return variables; }

    public Map<String, NexacroDataset> getDatasets() { return datasets; }

    public String getVariable(String name) { return variables.get(name); }

    /** 없으면 빈 Dataset 반환 (NPE 방지) */
    public NexacroDataset getDataset(String name) {
        NexacroDataset ds = datasets.get(name);
        return ds != null ? ds : new NexacroDataset(name);
    }
}
