package com.tnpacs.dicoogle.dicomwebplugin.wadors;

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
import pt.ua.dicoogle.sdk.datastructs.SearchResult;

import java.util.*;

public class RetrieveUtils {
    private static final Logger logger = LoggerFactory.getLogger(RetrieveUtils.class);

    public static JSONArray getMetadata(String query) throws NotFoundException {
        List<SearchResult> results = new ArrayList<>();
        Utils.getMongoQueryPlugin().query(query).forEach(results::add);
        if (results.isEmpty()) throw new NotFoundException(String.format("No results for query: %s", query));

        JSONArray metadata = new JSONArray();
        for (SearchResult result : results) {
            metadata.put(RetrieveUtils.instanceToMetadataJSONObject(result));
        }
        return metadata;
    }

    private static JSONObject instanceToMetadataJSONObject(SearchResult instance) {
        HashSet<String> attributes = new HashSet<>(Arrays.asList(Dictionary.getInstance().getNames()));
//        attributes.addAll(Constants.STUDY_LEVEL_ATTRIBUTES);
//        attributes.addAll(Constants.SERIES_LEVEL_ATTRIBUTES);
//        attributes.addAll(Constants.INSTANCE_LEVEL_ATTRIBUTES);
//
//        // Frane Of Reference UID
//        attributes.add(Dictionary.getInstance().getName(Tag.FrameOfReferenceUID));
//
//        // Pixel Data attributes
//        attributes.add(Dictionary.getInstance().getName(Tag.SamplesPerPixel));
//        attributes.add(Dictionary.getInstance().getName(Tag.PhotometricInterpretation));
//        attributes.add(Dictionary.getInstance().getName(Tag.PixelSpacing));
//        attributes.add(Dictionary.getInstance().getName(Tag.Rows));
//        attributes.add(Dictionary.getInstance().getName(Tag.Columns));
//        attributes.add(Dictionary.getInstance().getName(Tag.BitsAllocated));
//        attributes.add(Dictionary.getInstance().getName(Tag.BitsStored));
//        attributes.add(Dictionary.getInstance().getName(Tag.HighBit));
//        attributes.add(Dictionary.getInstance().getName(Tag.PixelRepresentation));
//        attributes.add(Dictionary.getInstance().getName(Tag.WindowCenter));
//        attributes.add(Dictionary.getInstance().getName(Tag.WindowWidth));
//        attributes.add(Dictionary.getInstance().getName(Tag.RescaleIntercept));
//        attributes.add(Dictionary.getInstance().getName(Tag.RescaleSlope));
//        attributes.add(Dictionary.getInstance().getName(Tag.RescaleType));
//        attributes.add(Dictionary.getInstance().getName(Tag.WindowCenterWidthExplanation));

        JSONObject instanceMetadataObj = Utils.searchResultToJSONObject(instance, attributes);

        HashMap<String, Object> data = instance.getExtraData();
        String studyInstanceUID = data.get(Dictionary.getInstance().getName(Tag.StudyInstanceUID)).toString();
        String seriesInstanceUID = data.get(Dictionary.getInstance().getName(Tag.SeriesInstanceUID)).toString();
        String sopInstanceUID = data.get(Dictionary.getInstance().getName(Tag.SOPInstanceUID)).toString();
        String retrieveURL = String.format("http://localhost:8080/dicomweb/wado/studies/%s/series/%s/instances/%s",
                studyInstanceUID, seriesInstanceUID, sopInstanceUID);

        // add Retrieve URL
        if (!instanceMetadataObj.has(Utils.getTagString(Constants.RETRIEVE_URL_TAG))) {
            JSONArray retrieveURLArr = new JSONArray();
            retrieveURLArr.put(retrieveURL);
            try {
                JSONObject retrieveURLObj = new JSONObject();
                retrieveURLObj.put(Constants.TAG_VR, Constants.RETRIEVE_URL_VR);
                retrieveURLObj.put(Constants.TAG_VALUE, retrieveURLArr);
                instanceMetadataObj.put(Utils.getTagString(Constants.RETRIEVE_URL_TAG), retrieveURLObj);
            } catch (JSONException e) {
                logger.error("Failed to add Retrieve URL", e);
            }
        }

        // add Frame Of Reference UID
        try {
            String tagString = Utils.getTagString(Tag.FrameOfReferenceUID);
            if (!instanceMetadataObj.has(tagString)
                    || !instanceMetadataObj.getJSONObject(tagString).has(Constants.TAG_VALUE)
                    || instanceMetadataObj.getJSONObject(tagString).getJSONArray(Constants.TAG_VALUE).length() == 0
            ) {
                JSONArray frameOfReferenceUIDArr = new JSONArray();
                frameOfReferenceUIDArr.put("1.2.3.4");
                JSONObject frameOfReferenceUIDObj = new JSONObject();
                frameOfReferenceUIDObj.put(Constants.TAG_VR, VRMap.getVRMap().vrOf(Tag.FrameOfReferenceUID).toString());
                frameOfReferenceUIDObj.put(Constants.TAG_VALUE, frameOfReferenceUIDArr);
                instanceMetadataObj.put(Utils.getTagString(Tag.FrameOfReferenceUID), frameOfReferenceUIDObj);
            }
        } catch (JSONException e) {
            logger.error("Failed to add Frame of Reference UID", e);
        }


        // add Number Of Frames
        try {
            String tagString = Utils.getTagString(Tag.NumberOfFrames);
            if (!instanceMetadataObj.has(tagString)
                    || !instanceMetadataObj.getJSONObject(tagString).has(Constants.TAG_VALUE)
                    || instanceMetadataObj.getJSONObject(tagString).getJSONArray(Constants.TAG_VALUE).length() == 0
            ) {
                JSONArray numberOfFramesArr = new JSONArray();
                numberOfFramesArr.put(1);
                JSONObject numberOfFramesObj = new JSONObject();
                numberOfFramesObj.put(Constants.TAG_VR, VRMap.getVRMap().vrOf(Tag.NumberOfFrames).toString());
                numberOfFramesObj.put(Constants.TAG_VALUE, numberOfFramesArr);
                instanceMetadataObj.put(Utils.getTagString(Tag.NumberOfFrames), numberOfFramesObj);
            }
        } catch (JSONException e) {
            logger.error("Failed to add Number Of Frames", e);
        }

        return instanceMetadataObj;
    }
}
