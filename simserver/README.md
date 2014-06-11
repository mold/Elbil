Ändra ip-adressen i ```obd2ev/OBD2Ev.properties``` (fås med ```ifconfig``` på linux eller ```ipconfig``` på windows)

```
socket.host=[ip-adress]
```

Kör sen servern med kommandot

```
java -jar STNSimulator.jar
```

Mappen obd2ev (med configfilerna) ska ligga i root på telefonen som kör appen. Glöm inte att kolla ip-adresserna! :):)
