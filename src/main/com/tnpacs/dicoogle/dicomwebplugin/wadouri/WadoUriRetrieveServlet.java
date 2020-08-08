package com.tnpacs.dicoogle.dicomwebplugin.wadouri;

import com.tnpacs.dicoogle.dicomwebplugin.utils.RequestHandler;
import com.tnpacs.dicoogle.dicomwebplugin.wadouri.handlers.WadoUriRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class WadoUriRetrieveServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(WadoUriRetrieveServlet.class);

    private static final RequestHandler[] handlers = new RequestHandler[]{
            new WadoUriRequestHandler()
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("WADO-URI: {}", req.getQueryString());

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
