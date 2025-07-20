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

app.commandLine.appendSwitch('xdg-portal-required-version', '4');

app.whenReady().then(() => {
  createWindow()

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })

  app.on('window-all-closed', () => {
    app.quit()
  })
})

ipcMain.handle("open-file-dialog", async (_event, filters, defaultPath) => {
  try {
    const { canceled, filePaths } = await dialog.showOpenDialog(BrowserWindow.getFocusedWindow(), {
      properties: ["openFile"],
      filters: filters,
      defaultPath: defaultPath
    });
    if (canceled) {
      return null;
    } else {
      return fs.readFileSync(filePaths[0]).toString("base64");
    }
  } catch (error) {
    console.log(error);
    return null;
  }
});

ipcMain.handle("open-save-dialog", async (_event, defaultPath) => {
  try {
    const { canceled, filePath } = await dialog.showSaveDialog({
      defaultPath: defaultPath,
      filters: [
        { name: "All Files", extensions: ["*"] }
      ]
    })
    return canceled ? null : filePath;
  } catch (error) {
    console.log(error);
    return null;
  }
})

ipcMain.handle("file-read", async (_event, filePath) => {
  const content = fs.readFileSync(path.join(__dirname, filePath), "utf-8");
  return content;
});

ipcMain.handle("file-save", async (_event, filePath, content) => {
  try {
    if (filePath.slice(filePath.length - 5, filePath.length) !== ".json") {
      filePath = filePath.concat(".json");
    }
    fs.writeFileSync(filePath, content, "utf-8");
    return { error: null };
  } catch(error) {
    return { error: "Error writing file: " + error };
  }
})
