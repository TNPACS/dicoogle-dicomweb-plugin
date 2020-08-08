package com.tnpacs.dicoogle.dicomwebplugin.qidors.handlers;

import com.tnpacs.dicoogle.dicomwebplugin.exceptions.InvalidParametersException;
import com.tnpacs.dicoogle.dicomwebplugin.exceptions.NotFoundException;
import com.tnpacs.dicoogle.dicomwebplugin.qidors.QueryParameters;
import com.tnpacs.dicoogle.dicomwebplugin.utils.*;
import org.dcm4che2.data.Tag;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class QueryInstancesRequestHandler extends RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(QueryInstancesRequestHandler.class);

    @Override
    protected Pattern getPattern() {
        return Patterns.QUERY_INSTANCES;
    }

    @Override
    protected void sendResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String studyInstanceUID = getPathParameter(PathParameters.STUDY_INSTANCE_UID);
            String seriesInstanceUID = getPathParameter(PathParameters.SERIES_INSTANCE_UID);
            QueryParameters queryParams = new QueryParameters(req.getParameterMap());
            JSONArray studies = getInstances(studyInstanceUID, seriesInstanceUID, queryParams);
            resp.getWriter().print(studies);
        } catch (InvalidParametersException e) {
            resp.setStatus(400);
            resp.getWriter().print("Invalid parameters: " + e.getMessage());

        } catch (NotFoundException e) {
            resp.setStatus(404);
            resp.getWriter().print((e.getMessage()));
        } catch (JSONException e) {
            resp.setStatus(500);
            resp.getWriter().print("Error: " + e.getMessage());
        }
    }

    public static JSONArray getInstances(String studyInstanceUID, String seriesInstanceUID, QueryParameters queryParams)
            throws IndexOutOfBoundsException, IllegalArgumentException, JSONException, NotFoundException {
        logger.info("QUERY INSTANCES PARAMS: {}", queryParams.toString());

        Integer limit = queryParams.getLimit();
        if (limit == null) limit = 0;
        Integer offset = queryParams.getOffset();
        if (offset == null) offset = 0;

        String query = String.format("%s:%s AND %s:%s",
                Dictionary.getInstance().getName(Tag.StudyInstanceUID), studyInstanceUID,
                Dictionary.getInstance().getName(Tag.SeriesInstanceUID), seriesInstanceUID);

        List<SearchResult> allInstances = new ArrayList<>();
        Utils.getMongoQueryPlugin().query(query).forEach(allInstances::add);
        if (allInstances.isEmpty()) throw new NotFoundException(
                String.format("Not found: %s/%s", studyInstanceUID, seriesInstanceUID));

        List<SearchResult> instances = limit > 0
                ? allInstances.subList(offset, Math.min(offset + limit, allInstances.size()))
                : allInstances.subList(offset, allInstances.size());

        return new JSONArray(instances.stream().map(Utils::instanceToJSONObject).toArray());
    }
}
