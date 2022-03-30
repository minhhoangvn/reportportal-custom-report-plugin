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
  const { useEffect, useState } = React;
  console.log(localStorage);
  const [advice, setAdvice] = useState('');
  useEffect(() => {
    const url = `http://${window.location.host}/api/dummyPath`;
    const fetchData = async () => {
      const token = JSON.parse(localStorage.getItem('token'));
      const authorization = `bearer ${token.value}`;
      const headers = { Authorization: authorization };
      try {
        console.log(headers);
        const response = await fetch(url, {
          method: 'GET',
          headers,
        });
        const json = await response.json();
        console.log(json.slip.advice);
        setAdvice(json.slip.advice);
      } catch (error) {
        console.log('error', error);
      }
    };

    fetchData();
  }, []);

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
      Extend UI
    </SidebarButton>
  );
};
