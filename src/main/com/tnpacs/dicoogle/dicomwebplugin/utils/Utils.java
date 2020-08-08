package com.tnpacs.dicoogle.dicomwebplugin.utils;

import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.data.VRMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.sdk.QueryInterface;
import pt.ua.dicoogle.sdk.StorageInterface;
import pt.ua.dicoogle.sdk.core.DicooglePlatformInterface;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;
import pt.ua.dicoogle.sdk.settings.ConfigurationHolder;

import java.util.*;

public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private static boolean isInitialized = false;
    private static DicooglePlatformInterface platform;

    private static QueryInterface mongoQueryPlugin;
    private static StorageInterface mongoStoragePlugin;

    public static void initialize(ConfigurationHolder settings) {
        // init Dictionary
        Dictionary.getInstance();
        isInitialized = true;
    }

    public static boolean isInitialized() {
        return isInitialized;
    }

    public static String getTagString(int tag) {
        String hexString = Integer.toHexString(tag).toUpperCase();
        return StringUtils.padLeft(hexString, 8, '0');
    }

    public static DicooglePlatformInterface getPlatform() {
        return platform;
    }

    public static void setPlatform(DicooglePlatformInterface platform) {
        Utils.platform = platform;
        mongoQueryPlugin = Utils.getPlatform().getQueryPlugins(true).stream()
                .filter(plugin -> plugin.getName().equals(Constants.MONGO_QUERY_PLUGIN_NAME))
                .findFirst()
                .orElse(null);
        mongoStoragePlugin = Utils.getPlatform().getStoragePlugins(true).stream()
                .filter(plugin -> plugin.getName().equals(Constants.MONGO_STORAGE_PLUGIN_NAME))
                .findFirst()
                .orElse(null);
    }

    public static QueryInterface getMongoQueryPlugin() {
        return mongoQueryPlugin;
    }

    public static StorageInterface getMongoStoragePlugin() {
        return mongoStoragePlugin;
    }

    public static JSONObject studyToJSONObject(HashMap<String, HashMap<String, SearchResult>> study) {
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
            logger.error("JSONException: {}", e.getMessage(), e);
        }

        return studyObj;
    }

    public static JSONObject seriesToJSONObject(HashMap<String, SearchResult> series) {
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
            logger.error("JSONException: {}", e.getMessage(), e);
        }

        return seriesObj;
    }

    public static JSONObject searchResultToJSONObject(SearchResult result, Set<String> attributes) {
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
            String value = resultData.get(tagName) != null
                    ? resultData.get(tagName).toString()
                    : null;

            try {
                // get VR
                String vr = (String) vrMap.get(tagName);
                if (vr == null) vr = VRMap.getVRMap().vrOf(tag).toString();
                tagValue.put(Constants.TAG_VR, vr);

                JSONArray arr = new JSONArray();

                // handle some special cases
                if (value == null) {
                    tagValue.put(Constants.TAG_VALUE, arr);
                    jsonObject.put(tagKey, tagValue);
                    continue;
                }

                if (tag == Tag.PixelSpacing) {
                    String[] spacings = value.split("\\\\");
                    arr.put(spacings[0]);
                    arr.put(spacings[1]);
                    tagValue.put(Constants.TAG_VALUE, arr);
                    jsonObject.put(tagKey, tagValue);
                    continue;
                }

                if (tag == Tag.RedPaletteColorLookupTableData
                        || tag == Tag.GreenPaletteColorLookupTableData
                        || tag == Tag.BluePaletteColorLookupTableData) {
                    String[] lutData = value.split("\\\\");
                    for (String lutDataItem : lutData) {
                        arr.put(Integer.parseInt(lutDataItem));
                    }
                    tagValue.put(Constants.TAG_VALUE, arr);
                    jsonObject.put(tagKey, tagValue);
                    continue;
                }

                if (value.contains("\\")) {
                    String[] items = value.split("\\\\");
                    for (String item : items) {
                        arr.put(parseValue(item, vr));
                    }
                    tagValue.put(Constants.TAG_VALUE, arr);
                    jsonObject.put(tagKey, tagValue);
                    continue;
                }

                arr.put(parseValue(value, vr));
                tagValue.put(Constants.TAG_VALUE, arr);
                jsonObject.put(tagKey, tagValue);
            } catch (JSONException e) {
                logger.error("JSONException: {}", e.getMessage(), e);
            }
        }

        return jsonObject;
    }

    private static Object parseValue(String value, String vr) throws JSONException {
        if (value == null) return null;
        try {
            if (vr.equals(VR.PN.toString())) {
                // Person Name
                JSONObject alphabeticObj = new JSONObject();
                alphabeticObj.put(Constants.TAG_PN_ALPHABETIC, value);
                return alphabeticObj;
            }
            if (vr.equals(VR.FL.toString())) {
                // Floating Point Single
                return Float.parseFloat(value);
            }
            if (vr.equals(VR.FD.toString())) {
                // Floating Point Double
                return Double.parseDouble(value);
            }
            if (vr.equals(VR.SL.toString())) {
                // Signed Long
                // 4 bytes --> Integer
                return Integer.parseInt(value);
            }
            if (vr.equals(VR.SS.toString())) {
                // Signed Short
                return Short.parseShort(value);
            }
            if (vr.equals(VR.UL.toString())) {
                // Unsigned Long
                // 4 bytes, but unsigned --> Long
                return Long.parseLong(value);
            }
            if (vr.equals(VR.US.toString())) {
                // Unsigned Short
                // 2 bytes, but unsigned --> Integer
                return Integer.parseInt(value);
            }

            // these are supposed to be String, but OHIF requires Number
            if (vr.equals(VR.DS.toString())) {
                // Decimal String
                // max 16 bytes --> no suitable primitive data type --> Double
                return Double.parseDouble(value);
            }
            if (vr.equals(VR.IS.toString())) {
                // Integer String
                // max 12 bytes --> no suitable primitive data type --> Long
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            return value;
        }
        return value;
    }

    public static JSONObject instanceToJSONObject(SearchResult instance) {
        HashSet<String> attributes = new HashSet<>();
        attributes.addAll(Constants.STUDY_LEVEL_ATTRIBUTES);
        attributes.addAll(Constants.SERIES_LEVEL_ATTRIBUTES);
        attributes.addAll(Constants.INSTANCE_LEVEL_ATTRIBUTES);
        return searchResultToJSONObject(instance, attributes);
    }

    private static SearchResult getSearchResultFromStudy(HashMap<String, HashMap<String, SearchResult>> study) {
        List<HashMap<String, SearchResult>> series = new ArrayList<>(study.values());
        return getSearchResultFromSeries(series.get(0));
    }

    private static SearchResult getSearchResultFromSeries(HashMap<String, SearchResult> series) {
        List<SearchResult> instances = new ArrayList<>(series.values());
        return instances.get(0);
    }
}
