package com.tnpacs.dicoogle.dicomwebplugin.qidors;

import com.tnpacs.dicoogle.dicomwebplugin.exceptions.InvalidParametersException;
import com.tnpacs.dicoogle.dicomwebplugin.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryParameters {
    private final Map<Integer, String> filters = new HashMap<>();
    private final boolean includesAllFields = false;
    private final List<String> includeFields = new ArrayList<>();
    private final boolean fuzzyMatching;
    private final Integer limit;
    private final Integer offset;

    public QueryParameters(Map<String, String[]> params) throws InvalidParametersException {
        // TODO: implement handling logic for filters and includefield
        fuzzyMatching = getBooleanParam(Constants.PARAM_FUZZY_MATCHING, params);
        limit = getIntParam(Constants.PARAM_LIMIT, 1, params);
        offset = getIntParam(Constants.PARAM_OFFSET, 0, params);
    }

    private Integer getIntParam(String param, int minValue, Map<String, String[]> params) throws InvalidParametersException {
        Integer paramValue;
        String[] stringValues = params.get(param);
        if (stringValues != null && stringValues.length == 1) {
            try {
                paramValue = Integer.parseInt(stringValues[0]);
                if (paramValue < minValue) throw new InvalidParametersException(
                        String.format("Param \"%s\" must be greater than or equal to %d", param, minValue));
            } catch (NumberFormatException ex) {
                throw new InvalidParametersException(String.format("Param \"%s\" must be integer", param));
            }
        } else if (stringValues == null) {
            // default value
            paramValue = null;
        } else {
            throw new InvalidParametersException(String.format("Param \"%s\" must be a single value", param));
        }
        return paramValue;
    }

    private boolean getBooleanParam(String param, Map<String, String[]> params) throws InvalidParametersException {
        boolean paramValue;
        String[] stringValues = params.get(param);
        if (stringValues != null && stringValues.length == 1) {
            try {
                paramValue = Boolean.parseBoolean(stringValues[0]);
            } catch (NumberFormatException ex) {
                throw new InvalidParametersException(String.format("Param \"%s\" must be boolean", param));
            }
        } else if (stringValues == null) {
            paramValue = false;
        } else {
            throw new InvalidParametersException(String.format("Param \"%s\" is single value", param));
        }
        return paramValue;
    }

    public Map<Integer, String> getFilters() {
        return filters;
    }

    public boolean isIncludesAllFields() {
        return includesAllFields;
    }

    public List<String> getIncludeFields() {
        return includeFields;
    }

    public boolean isFuzzyMatching() {
        return fuzzyMatching;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "QueryParameters{" +
                "filters=" + filters +
                ", includesAllFields=" + includesAllFields +
                ", includeFields=" + includeFields +
                ", fuzzyMatching=" + fuzzyMatching +
                ", limit=" + limit +
                ", offset=" + offset +
                '}';
    }
}
