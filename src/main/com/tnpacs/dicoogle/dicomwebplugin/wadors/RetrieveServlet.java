package com.tnpacs.dicoogle.dicomwebplugin.wadors;

import com.tnpacs.dicoogle.dicomwebplugin.utils.RequestHandler;
import com.tnpacs.dicoogle.dicomwebplugin.wadors.handlers.RetrieveInstanceFramesRequestHandler;
import com.tnpacs.dicoogle.dicomwebplugin.wadors.handlers.RetrieveInstanceMetadataRequestHandler;
import com.tnpacs.dicoogle.dicomwebplugin.wadors.handlers.RetrieveSeriesMetadataRequestHandler;
import com.tnpacs.dicoogle.dicomwebplugin.wadors.handlers.RetrieveStudyMetadataRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RetrieveServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(RetrieveServlet.class);

    private static final RequestHandler[] handlers = new RequestHandler[]{
            new RetrieveStudyMetadataRequestHandler(),
            new RetrieveSeriesMetadataRequestHandler(),
            new RetrieveInstanceMetadataRequestHandler(),
            new RetrieveInstanceFramesRequestHandler()
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("WADO-RS: {}", req.getPathInfo());

        for (RequestHandler handler : handlers) {
            if (handler.handles(req)) {
                handler.handleRequest(req, resp);
                return;
            }
        }

        // no matching handler
        resp.setStatus(400);
        resp.getWriter().print("Unsupported endpoint");
    }
}
