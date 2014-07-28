ElBil
=====

# Setup
Enklast är att köra ``git clone`` i Eclipse workspace. Öppna Eclipse och välj **Existing Projects into Workspace** i _Import_-dialogen. Du måste importera följande projekt
```
Google API Utils
google-play-services_lib
PdCore
Project Elvizp
```

# Eclipse errors
Första och sista steget är alltid att köra _Clean..._ på de projekt som strular och annars starta om Eclipse.
Några vanliga fel med fixar visas nedan.

### ".../gen already exists but is not a source folder. Convert to a source folder or rename it."
Högerklicka på projektet och gå till _properties->Java Build Path_. Ta bort alla mappar, klicka på _Add Folder_ och bocka för _gen_ and _src_.
