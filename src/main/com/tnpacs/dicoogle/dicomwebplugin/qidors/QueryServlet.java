package com.tnpacs.dicoogle.dicomwebplugin.qidors;

import com.tnpacs.dicoogle.dicomwebplugin.qidors.handlers.QuerySeriesRequestHandler;
import com.tnpacs.dicoogle.dicomwebplugin.qidors.handlers.QueryStudiesRequestHandler;
import com.tnpacs.dicoogle.dicomwebplugin.utils.RequestHandler;
import com.tnpacs.dicoogle.dicomwebplugin.wadors.handlers.RetrieveSeriesMetadataRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class QueryServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(QueryServlet.class);

    private static final RequestHandler[] handlers = new RequestHandler[]{
            new QueryStudiesRequestHandler(),
            new QuerySeriesRequestHandler(),
            new QueryStudiesRequestHandler(),
            new RetrieveSeriesMetadataRequestHandler() // should be in WADO-RS, but OHIF uses QIDO
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("QIDO-RS: {}", req.getPathInfo());

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
