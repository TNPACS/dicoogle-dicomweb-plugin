package com.tnpacs.dicoogle.dicomwebplugin.wadors.handlers;

import com.tnpacs.dicoogle.dicomwebplugin.utils.RequestHandler;
import com.tnpacs.dicoogle.dicomwebplugin.exceptions.InvalidParametersException;
import com.tnpacs.dicoogle.dicomwebplugin.exceptions.NotFoundException;
import com.tnpacs.dicoogle.dicomwebplugin.utils.Dictionary;
import com.tnpacs.dicoogle.dicomwebplugin.utils.PathParameters;
import com.tnpacs.dicoogle.dicomwebplugin.utils.Patterns;
import com.tnpacs.dicoogle.dicomwebplugin.wadors.RetrieveParameters;
import com.tnpacs.dicoogle.dicomwebplugin.wadors.RetrieveUtils;
import org.dcm4che2.data.Tag;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

public class RetrieveInstanceMetadataRequestHandler extends RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RetrieveInstanceMetadataRequestHandler.class);

    @Override
    protected Pattern getPattern() {
        return Patterns.RETRIEVE_INSTANCE_METADATA;
    }

    @Override
    protected void sendResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String studyInstanceUID = getPathParameter(PathParameters.STUDY_INSTANCE_UID);
        String seriesInstanceUID = getPathParameter(PathParameters.SERIES_INSTANCE_UID);
        String sopInstanceUID = getPathParameter(PathParameters.SOP_INSTANCE_UID);

        try {
            RetrieveParameters retrieveParams = new RetrieveParameters(req.getParameterMap());
            JSONArray metadata = getInstanceMetadata(studyInstanceUID, seriesInstanceUID, sopInstanceUID, retrieveParams);
            resp.setContentType("application/json");
            resp.getWriter().print(metadata);
        } catch (InvalidParametersException e) {
            resp.setStatus(400);
            resp.getWriter().print("Invalid parameters: " + e.getMessage());
        } catch (NotFoundException e) {
            resp.setStatus(404);
            resp.getWriter().print(e.getMessage());
        }
    }

    private JSONArray getInstanceMetadata(String studyInstanceUID, String seriesInstanceUID, String sopInstanceUID,
                                          RetrieveParameters retrieveParams) throws NotFoundException {
        logger.info("RETRIEVE INSTANCE METADATA PARAMS: {}", retrieveParams.toString());
        String query = String.format("%s:%s AND %s:%s AND %s:%s",
                Dictionary.getInstance().getName(Tag.StudyInstanceUID), studyInstanceUID,
                Dictionary.getInstance().getName(Tag.SeriesInstanceUID), seriesInstanceUID,
                Dictionary.getInstance().getName(Tag.SOPInstanceUID), sopInstanceUID);
        return RetrieveUtils.getMetadata(query);
    }
}
