package com.tnpacs.dicoogle.dicomwebplugin.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RequestHandler {
    private Matcher matcher;

    protected abstract Pattern getPattern();

    public final void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!handles(req)) return;
        sendResponse(req, resp);
    }

    public boolean handles(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null) return false;
        matcher = getPattern().matcher(path);
        return matcher.matches();
    }

    protected final String getPathParameter(String param) {
        return matcher.group(param);
    }

    protected abstract void sendResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException;
}
