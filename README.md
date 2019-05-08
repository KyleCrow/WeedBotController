# WeedBotController
Application Android permettant de contrôler le robot du projet WeedBot. <br/>
Architecture:
+ fichier source de l'application:<br/>
https://github.com/KyleCrow/WeedBotController/blob/master/app/src/main/java/com/sti/weedbotcontroller/MainActivity.java <br/>
+ Threads <br/>
Thread de recherche de l'appareil appairé:<br/>
https://github.com/KyleCrow/WeedBotController/blob/master/app/src/main/java/com/sti/weedbotcontroller/bluetooth/SearchThread.java <br/>
Thread de connexion à l'appareil:<br/>
https://github.com/KyleCrow/WeedBotController/blob/master/app/src/main/java/com/sti/weedbotcontroller/bluetooth/ConnectThread.java <br/>
Thread de communication avec l'appareil:<br/>
https://github.com/KyleCrow/WeedBotController/blob/master/app/src/main/java/com/sti/weedbotcontroller/bluetooth/ConnectedThread.java <br/>
Thread de mise à jour du temps:<br/>
https://github.com/KyleCrow/WeedBotController/blob/master/app/src/main/java/com/sti/weedbotcontroller/time/TimeThread.java
