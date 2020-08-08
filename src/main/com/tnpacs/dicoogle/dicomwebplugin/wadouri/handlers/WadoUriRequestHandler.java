package com.tnpacs.dicoogle.dicomwebplugin.wadouri.handlers;

import com.tnpacs.dicoogle.dicomwebplugin.exceptions.InvalidParametersException;
import com.tnpacs.dicoogle.dicomwebplugin.exceptions.NotFoundException;
import com.tnpacs.dicoogle.dicomwebplugin.utils.Dictionary;
import com.tnpacs.dicoogle.dicomwebplugin.utils.RequestHandler;
import com.tnpacs.dicoogle.dicomwebplugin.utils.Utils;
import com.tnpacs.dicoogle.dicomwebplugin.wadouri.WadoUriRetrieveParameters;
import org.dcm4che2.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.sdk.StorageInputStream;
import pt.ua.dicoogle.sdk.StorageInterface;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.regex.Pattern;

public class WadoUriRequestHandler extends RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(WadoUriRequestHandler.class);

    // no pattern for WADO-URI
    @Override
    protected Pattern getPattern() {
        return null;
    }

    @Override
    protected void sendResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            WadoUriRetrieveParameters retrieveParams = new WadoUriRetrieveParameters(req.getParameterMap());
            sendInstance(resp, retrieveParams);
        } catch (InvalidParametersException e) {
            resp.setStatus(400);
            resp.getWriter().print("Invalid parameters: " + e.getMessage());
        } catch (NotFoundException e) {
            resp.setStatus(404);
            resp.getWriter().print(e.getMessage());
        }
    }

    @Override
    public boolean handles(HttpServletRequest req) {
        String path = req.getPathInfo();
        return path == null || path.equals("/");
    }

    private void sendInstance(HttpServletResponse resp, WadoUriRetrieveParameters retrieveParams)
            throws NotFoundException, IOException {
        logger.info("WADO-URI RETRIEVE INSTANCE PARAMS: {}", retrieveParams.toString());

        String studyInstanceUID = retrieveParams.getStudyUID();
        String seriesInstanceUID = retrieveParams.getSeriesUID();
        String sopInstanceUID = retrieveParams.getObjectUID();

        String query = String.format("%s:%s AND %s:%s AND %s:%s",
                Dictionary.getInstance().getName(Tag.StudyInstanceUID), studyInstanceUID,
                Dictionary.getInstance().getName(Tag.SeriesInstanceUID), seriesInstanceUID,
                Dictionary.getInstance().getName(Tag.SOPInstanceUID), sopInstanceUID);
        Iterable<SearchResult> results = Utils.getMongoQueryPlugin().query(query);
        Iterator<SearchResult> iterator = results.iterator();
        if (!iterator.hasNext()) throw new NotFoundException(
                String.format("Not found: %s/%s/%s", studyInstanceUID, seriesInstanceUID, sopInstanceUID));

        SearchResult instance = iterator.next();
        URI uri = instance.getURI();
        StorageInterface storagePlugin = Utils.getPlatform().getStoragePluginForSchema(uri.getScheme());

        try {
            StorageInputStream dicomFile = storagePlugin.at(uri).iterator().next();
            InputStream is = dicomFile.getInputStream();

            byte[] bytes = new byte[(int) dicomFile.getSize()];
            is.read(bytes);

            resp.setContentType("application/dicom");
            resp.setContentLength(bytes.length);
            resp.setHeader("Content-Encoding", "identity");
            resp.setCharacterEncoding(StandardCharsets.ISO_8859_1.toString());
            resp.getWriter().print(new String(bytes, StandardCharsets.ISO_8859_1));
        } catch (IOException e) {
            logger.error(e.getMessage());
            resp.sendError(500, e.getMessage());
        }
    }
}
