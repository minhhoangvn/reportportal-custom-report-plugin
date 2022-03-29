import ToilatesterIcon from './images/plugin-icon.svg';

export const SidebarToilatesterButton = (props) => {
  const { components, ...extensionProps } = props;
  const { SidebarButton } = components;
  const {
    lib: { React },
  } = extensionProps; // all other props come from `createImportProps` during plugin registration
  console.log(props);
  console.log(components);
  const item = {
    link: {
      type: 'PROJECT_DASHBOARD_PAGE',
    },
  };
  return (
    <SidebarButton
      onClick={(...args) => {
        console.log('Toilatester Icon Click', args);
      }}
      link={item.link}
      icon={ToilatesterIcon}
    >
      PQE Extend UI
    </SidebarButton>
  );
};
