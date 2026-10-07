Set WshShell = CreateObject("WScript.Shell")
WshShell.CurrentDirectory = "C:\Users\Administrator\AndroidStudioProjects\ResQTap\ResQTap-Website"
WshShell.Run "node auto_deploy_watcher.js", 0, False
