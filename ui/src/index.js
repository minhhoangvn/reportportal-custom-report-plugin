import { ExamplePluginTab } from 'components/examplePluginTab';
import { IntegrationFormFields } from './components/integrationFormFields';
import { IntegrationSettings } from './components/integrationSettings';
import { SidebarButton } from './components/sidebarComponent';

window.RP.registerPlugin({
  name: 'toilatester',
  extensions: [
    {
      name: 'integrationSettings',
      title: 'Example plugin settings',
      type: 'uiExtension:integrationSettings',
      component: IntegrationSettings,
    },
    {
      name: 'integrationFormFields',
      title: 'Example plugin fields',
      type: 'uiExtension:integrationFormFields',
      component: IntegrationFormFields,
    },
    {
      name: 'toilatester',
      title: 'toilatester plugin',
      type: 'uiExtension:settingsTab',
      component: ExamplePluginTab,
    },
    {
      name: 'sidebarComponent',
      title: 'sidebarComponent toilatester plugin',
      type: 'uiExtension:sidebarComponent',
      component: SidebarButton,
    },
  ],
});
