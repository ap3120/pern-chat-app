const { app, BrowserWindow, ipcMain, dialog } = require('electron');
const path = require('node:path');
const fs = require('fs');

const createWindow = () => {
  const win = new BrowserWindow({
    width: 1200,
    height: 800,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
    },
    autoHideMenuBar: true,
  })

  const isDev = !app.isPackaged;
  if (isDev) {
    win.loadURL('http://localhost:3000');
  } else {
    win.loadFile(path.join(__dirname, 'index.html'));
  }

}

app.whenReady().then(() => {
  createWindow()

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })

  app.on('window-all-closed', () => {
    app.quit()
  })
})

ipcMain.handle("open-file-dialog", async (_event, filters) => {
  const { canceled, filePaths } = await dialog.showOpenDialog({ properties: ["openFile"], filters: filters });
  return canceled ? null : filePaths[0];
});



ipcMain.handle("file--read", async (_event, filePath) => {
  const content = fs.readFileSync(path.join(__dirname, filePath), "utf-8");
  console.log("content is " + content);
  return content;
});
