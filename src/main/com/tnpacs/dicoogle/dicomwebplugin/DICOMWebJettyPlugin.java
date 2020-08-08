package com.tnpacs.dicoogle.dicomwebplugin;

import com.tnpacs.dicoogle.dicomwebplugin.qidors.QueryServlet;
import com.tnpacs.dicoogle.dicomwebplugin.wadors.RetrieveServlet;
import com.tnpacs.dicoogle.dicomwebplugin.utils.CORSFilter;
import com.tnpacs.dicoogle.dicomwebplugin.wadouri.WadoUriRetrieveServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import pt.ua.dicoogle.sdk.JettyPluginInterface;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServlet;
import java.util.EnumSet;

public class DICOMWebJettyPlugin extends BasePlugin implements JettyPluginInterface {
    private static final String QIDORS_PATH_SPEC = "/qidors/*";
    private static final String WADORS_PATH_SPEC = "/wadors/*";
    private static final String WADOURI_PATH_SPEC = "/wadouri/*";

    @Override
    public HandlerList getJettyHandlers() {
        Handler[] handlers = new Handler[] {
                createHandler(new QueryServlet(), QIDORS_PATH_SPEC),
                createHandler(new RetrieveServlet(), WADORS_PATH_SPEC),
                createHandler(new WadoUriRetrieveServlet(), WADOURI_PATH_SPEC)
        };
        HandlerList handlerList = new HandlerList();
        for (Handler handler : handlers) {
            handlerList.addHandler(handler);
        }
        return handlerList;
    }

    @Override
    public String getName() {
        return "dicomweb-query-plugin";
    }

    private Handler createHandler(HttpServlet servlet, String pathSpec) {
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.setContextPath("/dicomweb");
        addCORSFilter(handler);
        handler.addServlet(new ServletHolder(servlet), pathSpec);
        return handler;
    }

    private void addCORSFilter(ServletContextHandler handler) {
        handler.setDisplayName("cross-origin");
        FilterHolder corsHolder = new FilterHolder(CORSFilter.class);
        corsHolder.setInitParameter(CORSFilter.ALLOWED_ORIGINS_PARAM, "*");
        corsHolder.setInitParameter(CORSFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD,PUT,DELETE");
        corsHolder.setInitParameter(CORSFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin,Authorization,Content-Length");
        handler.addFilter(corsHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
    }
}
