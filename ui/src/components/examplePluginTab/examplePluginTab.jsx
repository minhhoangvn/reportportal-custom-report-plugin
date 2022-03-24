import styles from './examplePluginTab.scss';

export const ExamplePluginTab = (props) => {
  const {
    lib: { React },
  } = props;

  return <div className={styles.examplePluginTab}>Hello world!</div>;
};
