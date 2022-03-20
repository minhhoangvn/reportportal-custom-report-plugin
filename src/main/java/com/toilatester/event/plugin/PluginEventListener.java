package com.toilatester.event.plugin;

import com.epam.reportportal.extension.event.PluginEvent;
import com.toilatester.event.EventHandlerFactory;
import org.springframework.context.ApplicationListener;

import static java.util.Optional.ofNullable;

public class PluginEventListener implements ApplicationListener<PluginEvent> {
    private final String pluginId;
    private final EventHandlerFactory<PluginEvent> pluginEventEventHandlerFactory;

    public PluginEventListener(String pluginId, EventHandlerFactory<PluginEvent> pluginEventEventHandlerFactory) {
        this.pluginId = pluginId;
        this.pluginEventEventHandlerFactory = pluginEventEventHandlerFactory;
    }

    @Override
    public void onApplicationEvent(PluginEvent event) {
        if (supports(event)) {
            ofNullable(pluginEventEventHandlerFactory.getEventHandler(event.getType())).ifPresent(pluginEventEventHandler -> pluginEventEventHandler
                    .handle(event));
        }
    }

    private boolean supports(PluginEvent event) {
        return pluginId.equals(event.getPluginId());
    }
}
