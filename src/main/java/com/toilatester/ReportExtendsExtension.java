package com.toilatester;

import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.ta.reportportal.dao.*;
import com.toilatester.command.binary.GetFileCommand;
import com.toilatester.event.PluginEventHandlerFactory;
import com.toilatester.event.plugin.PluginEventListener;
import com.toilatester.info.PluginInfoProviderImpl;
import com.toilatester.utils.MemoizingSupplier;
import com.toilatester.ws.controller.ExtendsReportController;
import com.toilatester.ws.controller.HealthCheckController;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Extension
public class ReportExtendsExtension implements ReportPortalExtensionPoint, DisposableBean {
    public static final Logger LOGGER = LoggerFactory.getLogger(ReportExtendsExtension.class);

    private static final String PLUGIN_ID = "extends-report";
    public static final String BINARY_DATA_PROPERTIES_FILE_ID = "extends-report.properties";

    private final Supplier<Map<String, PluginCommand<?>>> pluginCommandMapping = new MemoizingSupplier<>(this::getCommands);

    private final Supplier<ApplicationListener<PluginEvent>> pluginLoadedListenerSupplier;

    private final String resourcesDir;

    @Autowired
    private GenericWebApplicationContext genericContext;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private LaunchRepository launchRepository;

    @Autowired
    private TestItemRepository testItemRepository;

    @Autowired
    private ItemAttributeRepository itemAttributeRepository;

    @Autowired
    private IntegrationRepository integrationRepository;

    @Autowired
    private IntegrationTypeRepository integrationTypeRepository;

    public ReportExtendsExtension(Map<String, Object> initParams) {
        resourcesDir = IntegrationTypeProperties.RESOURCES_DIRECTORY.getValue(initParams).map(String::valueOf).orElse("");
        pluginLoadedListenerSupplier = new MemoizingSupplier<>(() -> new PluginEventListener(PLUGIN_ID,
                new PluginEventHandlerFactory(integrationTypeRepository,
                        integrationRepository,
                        new PluginInfoProviderImpl(resourcesDir, BINARY_DATA_PROPERTIES_FILE_ID)
                )
        ));
    }

    // TODO: add logic to reload resource in serverless
    // allow to extend rest apis in ReportPortal.io
    // https://stackoverflow.com/questions/5758504/is-it-possible-to-dynamically-set-requestmappings-in-spring-mvc/5758529#5758529
    @PostConstruct
    public void createIntegration() {
        initListeners();
        initDynamicRestController();
    }

    private void initListeners() {
        ApplicationEventMulticaster applicationEventMulticaster = applicationContext.getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
                ApplicationEventMulticaster.class
        );
        applicationEventMulticaster.addApplicationListener(pluginLoadedListenerSupplier.get());
    }

    private void initDynamicRestController() {
        try {
            ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
            ExtendsReportController extendsReportController = beanFactory.createBean(ExtendsReportController.class);
            HealthCheckController healthCheckController = beanFactory.createBean(HealthCheckController.class);
            beanFactory.registerSingleton(extendsReportController.getClass().getCanonicalName(), extendsReportController);
            beanFactory.registerSingleton(healthCheckController.getClass().getCanonicalName(), healthCheckController);
            beanFactory.autowireBean(extendsReportController);
            beanFactory.autowireBean(healthCheckController);
        } catch (Exception e) {
            LOGGER.error("Error in init dynamic RestController", e);
        }
    }

    @Override
    public void destroy() {
        removeListeners();
    }

    private void removeListeners() {
        ApplicationEventMulticaster applicationEventMulticaster = applicationContext.getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
                ApplicationEventMulticaster.class
        );
        applicationEventMulticaster.removeApplicationListener(pluginLoadedListenerSupplier.get());
    }

    @Override
    public Map<String, ?> getPluginParams() {
        Map<String, Object> params = new HashMap<>();
        params.put(ALLOWED_COMMANDS, new ArrayList<>(pluginCommandMapping.get().keySet()));
        return params;
    }

    @Override
    public PluginCommand<?> getCommandToExecute(String commandName) {
        return pluginCommandMapping.get().get(commandName);
    }

    private Map<String, PluginCommand<?>> getCommands() {
        Map<String, PluginCommand<?>> pluginCommandMapping = new HashMap<>();
        pluginCommandMapping.put("getFile", new GetFileCommand(resourcesDir, BINARY_DATA_PROPERTIES_FILE_ID));
        pluginCommandMapping.put("testConnection", (integration, params) -> true);
        return pluginCommandMapping;
    }
}
