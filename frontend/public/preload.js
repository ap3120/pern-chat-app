const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('electron', {
  openFileDialog: async (filters) => {
    return await ipcRenderer.invoke("open-file-dialog", filters);
  },
  readFile: async (filePath) => {
    return await ipcRenderer.invoke("file--read", filePath);
  },
})
