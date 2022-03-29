import ToilatesterIcon from './images/plugin-icon.svg';

export const SidebarToilatesterButton = (props, ...args) => {
  const { components, selectors, ...extensionProps } = props;
  const { SidebarButton } = components;
  const {
    lib: { React },
  } = extensionProps; // all other props come from `createImportProps` during plugin registration
  console.log(args);
  console.log('props', props);
  console.log('components', components);
  console.log('this', this);
  console.log('window', window);
  console.log('activeProject', activeProject);
  const item = {
    link: {
      type: 'PROJECT_DASHBOARD_PAGE',
      payload: { projectId: activeProject },
    },
  };
  return (
    <SidebarButton
      onClick={(...args) => {
        console.log('Toilatester Icon Click: ', activeProject, args);
      }}
      link={item.link}
      icon={ToilatesterIcon}
    >
      PQE Extend UI
    </SidebarButton>
  );
};
