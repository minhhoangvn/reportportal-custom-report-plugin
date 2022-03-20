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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
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
    @PostConstruct
    public void createIntegration() {
        initListeners();
        System.err.println("==========");
        System.err.println("==========");
        System.err.println("==========");
        System.err.println("==========");
        System.out.println("Check instance application context " + applicationContext);
        System.out.println("Check instance LaunchRepository context " + launchRepository);
        System.err.println("==========");
        System.err.println("==========");
        System.err.println("==========");
        System.err.println("==========");
        try {
            ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
            AutowireCapableBeanFactory bf = applicationContext
                    .getAutowireCapableBeanFactory();
            ExtendsReportController extendsReportController = bf.createBean(ExtendsReportController.class);
            HealthCheckController healthCheckController = bf.createBean(HealthCheckController.class);
            bf.autowireBean(extendsReportController);
            bf.autowireBean(healthCheckController);
            beanFactory.registerSingleton(extendsReportController.getClass().getCanonicalName(), extendsReportController);
            beanFactory.registerSingleton(healthCheckController.getClass().getCanonicalName(), healthCheckController);
            genericContext.registerBean(ExtendsReportController.class, () -> extendsReportController);
            ExtendsReportController bean = applicationContext.getBean(ExtendsReportController.class);
            //((ConfigurableApplicationContext) applicationContext).refresh();
            System.out.println("================");
            System.out.println("================");
            System.out.println("Reload");
            System.out.println("================");
            System.out.println("================");
        } catch (Exception e) {
            System.err.println("====== Error");
            e.printStackTrace();
        }

    }

    private void initListeners() {
        ApplicationEventMulticaster applicationEventMulticaster = applicationContext.getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
                ApplicationEventMulticaster.class
        );
        applicationEventMulticaster.addApplicationListener(pluginLoadedListenerSupplier.get());
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
