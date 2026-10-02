Set WshShell = CreateObject("WScript.Shell")
WshShell.CurrentDirectory = "C:\Users\Administrator\AndroidStudioProjects\ResQTap"
WshShell.Run "node ResQTap-Website/server.js", 0, False
