package com.toilatester.event.plugin;

import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.toilatester.event.EventHandler;
import com.toilatester.info.PluginInfoProvider;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class PluginUnloadedEventHandler implements EventHandler<PluginEvent> {

    private final IntegrationTypeRepository integrationTypeRepository;
    private final IntegrationRepository integrationRepository;
    private final PluginInfoProvider pluginInfoProvider;

    public PluginUnloadedEventHandler(IntegrationTypeRepository integrationTypeRepository, IntegrationRepository integrationRepository,
                                      PluginInfoProvider pluginInfoProvider) {
        this.integrationTypeRepository = integrationTypeRepository;
        this.integrationRepository = integrationRepository;
        this.pluginInfoProvider = pluginInfoProvider;
    }

    @Override
    public void handle(PluginEvent event) {
        integrationTypeRepository.findByName(event.getPluginId()).ifPresent(integrationType -> {
            createIntegration(event.getPluginId(), integrationType);
            integrationTypeRepository.delete(pluginInfoProvider.provide(integrationType));
        });
    }

    private void createIntegration(String name, IntegrationType integrationType) {
        List<Integration> integrations = integrationRepository.findAllGlobalByType(integrationType);
        if (integrations.isEmpty()) {
            Integration integration = new Integration();
            integration.setName(name);
            integration.setType(integrationType);
            integration.setCreationDate(LocalDateTime.now());
            integration.setEnabled(true);
            integration.setCreator("SYSTEM");
            integration.setParams(new IntegrationParams(new HashMap<>()));
            integrationRepository.save(integration);
        }
    }

}
