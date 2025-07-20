const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('electron', {
  openFileDialog: async (filters, defaultPath) => {
    return await ipcRenderer.invoke("open-file-dialog", filters, defaultPath);
  },
  openSaveDialog: async (defaultPath) => {
    return await ipcRenderer.invoke("open-save-dialog", defaultPath);
  },
  readFile: async (filePath) => {
    return await ipcRenderer.invoke("file-read", filePath);
  },
  saveFile: async (filePath, content) => {
    return await ipcRenderer.invoke("file-save", filePath, content);
  }
})
