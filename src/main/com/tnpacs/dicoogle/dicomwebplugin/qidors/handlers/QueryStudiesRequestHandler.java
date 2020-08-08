package com.tnpacs.dicoogle.dicomwebplugin.qidors.handlers;

import com.tnpacs.dicoogle.dicomwebplugin.exceptions.InvalidParametersException;
import com.tnpacs.dicoogle.dicomwebplugin.qidors.QueryParameters;
import com.tnpacs.dicoogle.dicomwebplugin.utils.Patterns;
import com.tnpacs.dicoogle.dicomwebplugin.utils.RequestHandler;
import com.tnpacs.dicoogle.dicomwebplugin.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QueryStudiesRequestHandler extends RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(QueryStudiesRequestHandler.class);

    @Override
    protected Pattern getPattern() {
        return Patterns.QUERY_STUDIES;
    }

    @Override
    protected void sendResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            QueryParameters queryParams = new QueryParameters(req.getParameterMap());
            JSONArray studies = getStudies(queryParams);
            resp.getWriter().print(studies);
        } catch (InvalidParametersException e) {
            resp.setStatus(400);
            resp.getWriter().print("Invalid parameters: " + e.getMessage());
        } catch (JSONException e) {
            resp.setStatus(500);
            resp.getWriter().print("Error: " + e.getMessage());
        }
    }

    public static JSONArray getStudies(QueryParameters queryParams)
            throws IndexOutOfBoundsException, IllegalArgumentException, JSONException {
        logger.info("QUERY STUDIES PARAMS: {}", queryParams.toString());

        Integer limit = queryParams.getLimit();
        if (limit == null) limit = 0;
        Integer offset = queryParams.getOffset();
        if (offset == null) offset = 0;

        Iterable<SearchResult> results = Utils.getMongoQueryPlugin().query("*:*");
        HashMap<String, HashMap<String, HashMap<String, HashMap<String, SearchResult>>>> tree = SearchResult.toTree(results);
        List<HashMap<String, HashMap<String, HashMap<String, SearchResult>>>> patients = new ArrayList<>(tree.values());

        List<HashMap<String, HashMap<String, SearchResult>>> allStudies = patients.stream()
                .map(HashMap::values)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        List<HashMap<String, HashMap<String, SearchResult>>> studies = limit > 0
                ? allStudies.subList(offset, Math.min(offset + limit, allStudies.size()))
                : allStudies.subList(offset, allStudies.size());

        return new JSONArray(studies.stream().map(Utils::studyToJSONObject).toArray());
    }
}
