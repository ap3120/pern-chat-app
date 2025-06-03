let globalVariable = null;

export const getGlobalVariable = () => {
  return globalVariable;
}

export const setGlobalVariable = value => {
  globalVariable = value;
}
