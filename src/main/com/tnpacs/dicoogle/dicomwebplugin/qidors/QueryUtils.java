package com.tnpacs.dicoogle.dicomwebplugin.qidors;

import com.tnpacs.dicoogle.dicomwebplugin.exceptions.NotFoundException;
import com.tnpacs.dicoogle.dicomwebplugin.utils.Constants;
import com.tnpacs.dicoogle.dicomwebplugin.utils.Dictionary;
import com.tnpacs.dicoogle.dicomwebplugin.utils.Utils;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.data.VRMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.sdk.QueryInterface;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;

import java.util.*;
import java.util.stream.Collectors;

public class QueryUtils {
    private static final Logger logger = LoggerFactory.getLogger(QueryUtils.class);
    private static final QueryInterface mongoQueryPlugin = Utils.getPlatform().getQueryPlugins(true).stream()
            .filter(plugin -> plugin.getName().equals("mongo-query-plugin"))
            .findFirst()
            .orElse(null);

    public static JSONArray getStudies(QueryParameters params)
            throws IndexOutOfBoundsException, IllegalArgumentException, JSONException {
        logger.info("QUERY STUDIES PARAMS: {}", params.toString());

        Integer limit = params.getLimit();
        if (limit == null) limit = 0;
        Integer offset = params.getOffset();
        if (offset == null) offset = 0;

        Iterable<SearchResult> results = mongoQueryPlugin.query("*:*");
        HashMap<String, HashMap<String, HashMap<String, HashMap<String, SearchResult>>>> tree = SearchResult.toTree(results);
        List<HashMap<String, HashMap<String, HashMap<String, SearchResult>>>> patients = new ArrayList<>(tree.values());

        List<HashMap<String, HashMap<String, SearchResult>>> allStudies = patients.stream()
                .map(HashMap::values)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        List<HashMap<String, HashMap<String, SearchResult>>> studies = limit > 0
                ? allStudies.subList(offset, Math.min(offset + limit, allStudies.size()))
                : allStudies.subList(offset, allStudies.size());

        return new JSONArray(studies.stream().map(QueryUtils::studyToJSONObject).toArray());
    }

    public static JSONArray getSeries(String studyInstanceUID, QueryParameters params)
            throws IndexOutOfBoundsException, IllegalArgumentException, JSONException, NotFoundException {
        logger.info("QUERY SERIES PARAMS: {}", params.toString());
        Integer limit = params.getLimit();
        if (limit == null) limit = 0;
        Integer offset = params.getOffset();
        if (offset == null) offset = 0;

        String query = String.format("%s:%s", Dictionary.getInstance().getName(Tag.StudyInstanceUID), studyInstanceUID);
        Iterable<SearchResult> results = mongoQueryPlugin.query(query);
        HashMap<String, HashMap<String, HashMap<String, HashMap<String, SearchResult>>>> tree = SearchResult.toTree(results);
        if (tree.isEmpty()) throw new NotFoundException(String.format("Not found: %s", studyInstanceUID));

        List<HashMap<String, HashMap<String, HashMap<String, SearchResult>>>> patients = new ArrayList<>(tree.values());
        HashMap<String, HashMap<String, SearchResult>> study = patients.get(0).get(studyInstanceUID);

        List<HashMap<String, SearchResult>> allSeries = new ArrayList<>(study.values());
        List<HashMap<String, SearchResult>> series = limit > 0
                ? allSeries.subList(offset, Math.min(offset + limit, allSeries.size()))
                : allSeries.subList(offset, allSeries.size());

        return new JSONArray(series.stream().map(QueryUtils::seriesToJSONObject).toArray());
    }

    public static JSONArray getInstances(String studyInstanceUID, String seriesInstanceUID, QueryParameters params)
            throws IndexOutOfBoundsException, IllegalArgumentException, JSONException, NotFoundException {
        logger.info("QUERY INSTANCES PARAMS: {}", params.toString());
        Integer limit = params.getLimit();
        if (limit == null) limit = 0;
        Integer offset = params.getOffset();
        if (offset == null) offset = 0;

        String query = String.format("%s:%s AND %s:%s",
                Dictionary.getInstance().getName(Tag.StudyInstanceUID), studyInstanceUID,
                Dictionary.getInstance().getName(Tag.SeriesInstanceUID), seriesInstanceUID);

        List<SearchResult> allInstances = new ArrayList<>();
        mongoQueryPlugin.query(query).forEach(allInstances::add);
        if (allInstances.isEmpty()) throw new NotFoundException(
                String.format("Not found: %s/%s", studyInstanceUID, seriesInstanceUID));

        List<SearchResult> instances = limit > 0
                ? allInstances.subList(offset, Math.min(offset + limit, allInstances.size()))
                : allInstances.subList(offset, allInstances.size());

        return new JSONArray(instances.stream().map(QueryUtils::instanceToJSONObject).toArray());
    }

    private static SearchResult getSearchResultFromStudy(HashMap<String, HashMap<String, SearchResult>> study) {
        List<HashMap<String, SearchResult>> series = new ArrayList<>(study.values());
        return getSearchResultFromSeries(series.get(0));
    }

    private static SearchResult getSearchResultFromSeries(HashMap<String, SearchResult> series) {
        List<SearchResult> instances = new ArrayList<>(series.values());
        return instances.get(0);
    }

    private static JSONObject studyToJSONObject(HashMap<String, HashMap<String, SearchResult>> study) {
        SearchResult result = getSearchResultFromStudy(study);
        JSONObject studyObj = searchResultToJSONObject(result, Constants.STUDY_LEVEL_ATTRIBUTES);

        // add Retrieve URL
        HashMap<String, Object> data = result.getExtraData();
        String studyInstanceUID = data.get(Dictionary.getInstance().getName(Tag.StudyInstanceUID)).toString();
        String retrieveURL = String.format("http://localhost:8080/dicomweb/wado/studies/%s", studyInstanceUID);
        JSONArray arr = new JSONArray();
        arr.put(retrieveURL);
        try {
            JSONObject retrieveURLObj = new JSONObject();
            retrieveURLObj.put(Constants.TAG_VR, Constants.RETRIEVE_URL_VR);
            retrieveURLObj.put(Constants.TAG_VALUE, arr);
            studyObj.put(Utils.getTagString(Constants.RETRIEVE_URL_TAG), retrieveURLObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return studyObj;
    }

    private static JSONObject seriesToJSONObject(HashMap<String, SearchResult> series) {
        SearchResult result = getSearchResultFromSeries(series);
        HashSet<String> attributes = new HashSet<>();
        attributes.addAll(Constants.STUDY_LEVEL_ATTRIBUTES);
        attributes.addAll(Constants.SERIES_LEVEL_ATTRIBUTES);
        JSONObject seriesObj = searchResultToJSONObject(result, attributes);

        // add Retrieve URL
        HashMap<String, Object> data = result.getExtraData();
        String studyInstanceUID = data.get(Dictionary.getInstance().getName(Tag.StudyInstanceUID)).toString();
        String seriesInstanceUID = data.get(Dictionary.getInstance().getName(Tag.SeriesInstanceUID)).toString();
        String retrieveURL = String.format("http://localhost:8080/dicomweb/wado/studies/%s/series/%s", studyInstanceUID, seriesInstanceUID);
        JSONArray arr = new JSONArray();
        arr.put(retrieveURL);
        try {
            JSONObject retrieveURLObj = new JSONObject();
            retrieveURLObj.put(Constants.TAG_VR, Constants.RETRIEVE_URL_VR);
            retrieveURLObj.put(Constants.TAG_VALUE, arr);
            seriesObj.put(Utils.getTagString(Constants.RETRIEVE_URL_TAG), retrieveURLObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return seriesObj;
    }

    private static JSONObject instanceToJSONObject(SearchResult instance) {
        HashSet<String> attributes = new HashSet<>();
        attributes.addAll(Constants.STUDY_LEVEL_ATTRIBUTES);
        attributes.addAll(Constants.SERIES_LEVEL_ATTRIBUTES);
        attributes.addAll(Constants.INSTANCE_LEVEL_ATTRIBUTES);
        return searchResultToJSONObject(instance, attributes);
    }

    private static JSONObject searchResultToJSONObject(SearchResult result, Set<String> attributes) {
        JSONObject jsonObject = new JSONObject();
        HashMap<String, Object> resultData = result.getExtraData();

        Object vrMapObj = resultData.get(Constants.METADATA_VR_MAP);
        if (!(vrMapObj instanceof Map)) return new JSONObject();
        Map<String, Object> vrMap = (Map<String, Object>) vrMapObj;

        for (String tagName : attributes) {
            // get tag key
            int tag = Dictionary.getInstance().getTag(tagName);
            String tagKey = Utils.getTagString(tag);

            // get tag value
            JSONObject tagValue = new JSONObject();
            Object value = resultData.get(tagName) != null
                    ? resultData.get(tagName).toString()
                    : null;

            try {
                // get VR
                Object vr = vrMap.get(tagName);
                if (vr != null) tagValue.put(Constants.TAG_VR, vr);
                else tagValue.put(Constants.TAG_VR, VRMap.getVRMap().vrOf(tag));

                // handle a special case for tag value
                JSONArray arr = new JSONArray();
                if (vr != null && vr.equals(VR.PN.toString())) {
                    JSONObject alphabeticObj = new JSONObject();
                    alphabeticObj.put(Constants.TAG_PN_ALPHABETIC, value);
                    arr.put(alphabeticObj);
                } else if (value != null) {
                    arr.put(value);
                }

                tagValue.put(Constants.TAG_VALUE, arr);
                jsonObject.put(tagKey, tagValue);
            } catch (JSONException e) {
                logger.error("JSONException: {}", e.getMessage(), e);
            }
        }

        return jsonObject;
    }
}
