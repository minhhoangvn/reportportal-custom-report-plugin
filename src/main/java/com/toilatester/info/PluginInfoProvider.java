package com.toilatester.info;

import com.epam.ta.reportportal.entity.integration.IntegrationType;

public interface PluginInfoProvider {

    IntegrationType provide(IntegrationType integrationType);
}
