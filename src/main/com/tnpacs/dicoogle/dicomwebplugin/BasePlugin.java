package com.tnpacs.dicoogle.dicomwebplugin;

import com.tnpacs.dicoogle.dicomwebplugin.utils.Utils;
import pt.ua.dicoogle.sdk.DicooglePlugin;
import pt.ua.dicoogle.sdk.settings.ConfigurationHolder;

public abstract class BasePlugin implements DicooglePlugin {
    protected ConfigurationHolder settings;
    protected boolean isEnabled = true;

    @Override
    public abstract String getName();

    @Override
    public boolean enable() {
        isEnabled = true;
        return true;
    }

    @Override
    public boolean disable() {
        isEnabled = false;
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void setSettings(ConfigurationHolder configurationHolder) {
        settings = configurationHolder;
        if (!Utils.isInitialized()) Utils.initialize(settings);
    }

    @Override
    public ConfigurationHolder getSettings() {
        return settings;
    }
}
