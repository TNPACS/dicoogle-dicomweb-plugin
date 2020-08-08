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
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class QuerySeriesRequestHandler extends RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(QuerySeriesRequestHandler.class);

    @Override
    protected Pattern getPattern() {
        return Patterns.QUERY_SERIES;
    }

    @Override
    protected void sendResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String studyInstanceUID = getPathParameter(PathParameters.STUDY_INSTANCE_UID);
            QueryParameters queryParams = new QueryParameters(req.getParameterMap());
            JSONArray studies = getSeries(studyInstanceUID, queryParams);
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

    public static JSONArray getSeries(String studyInstanceUID, QueryParameters queryParams)
            throws IndexOutOfBoundsException, IllegalArgumentException, JSONException, NotFoundException {
        logger.info("QUERY SERIES PARAMS: {}", queryParams.toString());

        Integer limit = queryParams.getLimit();
        if (limit == null) limit = 0;
        Integer offset = queryParams.getOffset();
        if (offset == null) offset = 0;

        String query = String.format("%s:%s", Dictionary.getInstance().getName(Tag.StudyInstanceUID), studyInstanceUID);
        Iterable<SearchResult> results = Utils.getMongoQueryPlugin().query(query);
        HashMap<String, HashMap<String, HashMap<String, HashMap<String, SearchResult>>>> tree = SearchResult.toTree(results);
        if (tree.isEmpty()) throw new NotFoundException(String.format("Not found: %s", studyInstanceUID));

        List<HashMap<String, HashMap<String, HashMap<String, SearchResult>>>> patients = new ArrayList<>(tree.values());
        HashMap<String, HashMap<String, SearchResult>> study = patients.get(0).get(studyInstanceUID);

        List<HashMap<String, SearchResult>> allSeries = new ArrayList<>(study.values());
        List<HashMap<String, SearchResult>> series = limit > 0
                ? allSeries.subList(offset, Math.min(offset + limit, allSeries.size()))
                : allSeries.subList(offset, allSeries.size());

        return new JSONArray(series.stream().map(Utils::seriesToJSONObject).toArray());
    }
}
