package com.tnpacs.dicoogle.dicomwebplugin.wadors.handlers;

import com.tnpacs.dicoogle.dicomwebplugin.exceptions.InvalidParametersException;
import com.tnpacs.dicoogle.dicomwebplugin.exceptions.NotFoundException;
import com.tnpacs.dicoogle.dicomwebplugin.utils.*;
import com.tnpacs.dicoogle.dicomwebplugin.wadors.RetrieveParameters;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
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

public class RetrieveInstanceFramesRequestHandler extends RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RetrieveInstanceFramesRequestHandler.class);

    @Override
    protected Pattern getPattern() {
        return Patterns.RETRIEVE_INSTANCE_FRAMES;
    }

    @Override
    protected void sendResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String studyInstanceUID = getPathParameter(PathParameters.STUDY_INSTANCE_UID);
        String seriesInstanceUID = getPathParameter(PathParameters.SERIES_INSTANCE_UID);
        String sopInstanceUID = getPathParameter(PathParameters.SOP_INSTANCE_UID);
        int frameNumber = Integer.parseInt(getPathParameter(PathParameters.FRAME_LIST));

        try {
            RetrieveParameters retrieveParams = new RetrieveParameters(req.getParameterMap());
            sendFramesResponse(resp, studyInstanceUID, seriesInstanceUID, sopInstanceUID, frameNumber, retrieveParams);
        } catch (InvalidParametersException e) {
            resp.setStatus(400);
            resp.getWriter().print("Invalid parameters: " + e.getMessage());
        } catch (NotFoundException e) {
            resp.setStatus(404);
            resp.getWriter().print(e.getMessage());
        }
    }

    public void sendFramesResponse(HttpServletResponse resp, String studyInstanceUID, String seriesInstanceUID, String sopInstanceUID,
                                   int frameNumber, RetrieveParameters retrieveParams) throws NotFoundException, IOException {
        logger.info("RETRIEVE INSTANCE FRAMES PARAMS: {}", retrieveParams.toString());

        String frameData = "";

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
            DicomInputStream dis = new DicomInputStream(is);
            DicomObject dicomObject = dis.readDicomObject();
            byte[] bytes = dicomObject.dataset().getBytes(Tag.PixelData);

            StringBuilder sb = new StringBuilder();
            String retrieveURL = String.format("https://localhost:8080/dicomweb/wado/studies/%s/series/%s/instances/%s",
                    studyInstanceUID, seriesInstanceUID, sopInstanceUID);
            sb.append("--").append(sopInstanceUID); // boundary
            sb.append("\r\n");
            sb.append("Content-Location: ").append(retrieveURL);
            sb.append("\r\n");
            sb.append("Content-Type: application/octet-stream");
            sb.append("\r\n\r\n");
            sb.append(new String(bytes, StandardCharsets.ISO_8859_1));
            sb.append("\r\n");
            sb.append("--").append(sopInstanceUID); // terminal boundary
            frameData = sb.toString();
        } catch (IOException e) {
            logger.error(e.getMessage());
            resp.sendError(500, e.getMessage());
            return;
        }

        String contentType = "multipart/related; type=\"application/octet-stream\"";
        int contentLength = frameData.getBytes(StandardCharsets.ISO_8859_1).length;
        resp.setContentType(contentType);
        resp.setContentLength(contentLength);
        resp.setHeader("Content-Encoding", "identity");
        resp.setCharacterEncoding(StandardCharsets.ISO_8859_1.toString());
        resp.getWriter().print(frameData);
    }
}
