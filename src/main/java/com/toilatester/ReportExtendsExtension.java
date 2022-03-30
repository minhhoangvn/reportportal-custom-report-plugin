package com.toilatester;

import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toilatester.command.binary.GetFileCommand;
import com.toilatester.event.PluginEventHandlerFactory;
import com.toilatester.event.plugin.PluginEventListener;
import com.toilatester.info.PluginInfoProviderImpl;
import com.toilatester.utils.MemoizingSupplier;
import com.toilatester.ws.controller.DummyController;
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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Extension
public class ReportExtendsExtension implements ReportPortalExtensionPoint, DisposableBean {
    public static final Logger LOGGER = LoggerFactory.getLogger(ReportExtendsExtension.class);
    private static final DummyController dummyController = new DummyController();
    // This must match with the plugin-id
    // when we build with MANIFEST.MF after building the plugin
    private static final String PLUGIN_ID = "toilatester";
    public static final String BINARY_DATA_PROPERTIES_FILE_ID = "extends-report.properties";

    private final Supplier<Map<String, PluginCommand<?>>> pluginCommandMapping = new MemoizingSupplier<>(this::getCommands);

    private final Supplier<ApplicationListener<PluginEvent>> pluginLoadedListenerSupplier;

    private final String resourcesDir;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    private SimpleUrlHandlerMapping simpleUrlHandlerMapping;

    @Autowired
    private ApplicationContext applicationContext;

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
        initDummyController();
    }

    private void initDummyController() {
        try {
            RequestMappingInfo requestMappingInfo = RequestMappingInfo
                    .paths("/dummyPath")
                    .methods(RequestMethod.GET)
                    .produces(MediaType.TEXT_PLAIN_VALUE)
                    .build();

            requestMappingHandlerMapping.
                    registerMapping(requestMappingInfo, dummyController,
                            DummyController.class.getDeclaredMethod("handleRequests", ReportPortalUser.class)
                    );

            Map<String, Object> urlMap = (Map<String, Object>) simpleUrlHandlerMapping.getUrlMap();
            LOGGER.info("URL map before add new controller {}", urlMap);
            urlMap.put("/dummyPath", dummyController);
            LOGGER.info("URL map after add new controller {}", urlMap);
            simpleUrlHandlerMapping.setUrlMap(urlMap);
        } catch (NoSuchMethodException e) {
            LOGGER.error("Error in init dynamic DummyController", e);
        }
    }

    @Override
    public void destroy() {
        removeListeners();
        removeController();
    }

    private void removeListeners() {
        ApplicationEventMulticaster applicationEventMulticaster = applicationContext.getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
                ApplicationEventMulticaster.class
        );
        applicationEventMulticaster.removeApplicationListener(pluginLoadedListenerSupplier.get());
    }

    private void removeController() {
        RequestMappingInfo requestMappingInfo = RequestMappingInfo
                .paths("/dummyPath")
                .methods(RequestMethod.GET)
                .produces(MediaType.TEXT_PLAIN_VALUE)
                .build();

        requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);

        Map<String, Object> urlMap = (Map<String, Object>) simpleUrlHandlerMapping.getUrlMap();
        LOGGER.info("URL map before remove new controller {}", urlMap);
        urlMap.remove("/dummyPath");
        LOGGER.info("URL map after remove new controller {}", urlMap);
        simpleUrlHandlerMapping.setUrlMap(urlMap);
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
        final GetFileCommand getFileCommand = new GetFileCommand(resourcesDir, BINARY_DATA_PROPERTIES_FILE_ID);
        pluginCommandMapping.put(getFileCommand.getName(), getFileCommand);
        pluginCommandMapping.put("testConnection", (integration, params) -> true);
        pluginCommandMapping.put("dummyCommand", (integration, params) -> convertParamsToString(params));
        return pluginCommandMapping;
    }

    private String convertParamsToString(Map<String, Object> params) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }
}
