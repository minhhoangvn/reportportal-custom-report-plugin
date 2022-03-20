package com.toilatester.event;

import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.toilatester.event.plugin.PluginLoadedEventHandler;
import com.toilatester.info.PluginInfoProvider;

import java.util.HashMap;
import java.util.Map;

public class PluginEventHandlerFactory implements EventHandlerFactory<PluginEvent> {

    public static final String LOAD_KEY = "load";

    private final Map<String, EventHandler<PluginEvent>> eventHandlerMapping;

    public PluginEventHandlerFactory(IntegrationTypeRepository integrationTypeRepository, IntegrationRepository integrationRepository,
                                     PluginInfoProvider pluginInfoProvider) {
        this.eventHandlerMapping = new HashMap<>();
        this.eventHandlerMapping.put(LOAD_KEY,
                new PluginLoadedEventHandler(integrationTypeRepository, integrationRepository, pluginInfoProvider)
        );
    }

    @Override
    public EventHandler<PluginEvent> getEventHandler(String key) {
        return eventHandlerMapping.get(key);
    }
}
