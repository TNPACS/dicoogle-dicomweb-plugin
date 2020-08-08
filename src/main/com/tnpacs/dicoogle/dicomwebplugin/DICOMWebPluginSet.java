package com.tnpacs.dicoogle.dicomwebplugin;

import com.tnpacs.dicoogle.dicomwebplugin.utils.Utils;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.sdk.JettyPluginInterface;
import pt.ua.dicoogle.sdk.PluginSet;
import pt.ua.dicoogle.sdk.core.DicooglePlatformInterface;
import pt.ua.dicoogle.sdk.core.PlatformCommunicatorInterface;
import pt.ua.dicoogle.sdk.settings.ConfigurationHolder;

import java.util.ArrayList;
import java.util.Collection;

@PluginImplementation
public class DICOMWebPluginSet implements PluginSet, PlatformCommunicatorInterface {
    private static final Logger logger = LoggerFactory.getLogger(DICOMWebPluginSet.class);
    private ConfigurationHolder settings;

    @Override
    public String getName() {
        return "dicomweb-plugin";
    }

    @Override
    public void setSettings(ConfigurationHolder settings) {
        if (!Utils.isInitialized()) Utils.initialize(settings);
        this.settings = settings;
    }

    @Override
    public ConfigurationHolder getSettings() {
        return settings;
    }

    @Override
    public Collection<? extends JettyPluginInterface> getJettyPlugins() {
        ArrayList<JettyPluginInterface> jettyPlugins = new ArrayList<>(1);
        jettyPlugins.add(new DICOMWebJettyPlugin());
        return jettyPlugins;
    }

    @Override
    public void setPlatformProxy(DicooglePlatformInterface dicooglePlatformInterface) {
        Utils.setPlatform(dicooglePlatformInterface);
    }
}
