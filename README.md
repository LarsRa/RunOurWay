<h1>RunOurWay</h1>
Das Gruppenprojekt RunOurWay ist eine Mobile Anwendung für Android in Java programmiert. 
Die Anwendung ist in der Lage den Nutzer beim Öffnen der App zu orten und anhand von einer eingegebenen Streckenlänge automatisch eine Laufstrecke der gewünschten Länge zu generieren. Bei dieser Strecke ist der aktuelle Standort der Start und das Ziel des Laufes, so dass ein Rundkurs generiert wird.<br/>
Beim Laufen wird der Nutzer durch Audionavigation unterstützt, um nicht von der generierten Route abzukommen.
Jeweils nach einem absolvierten Kilometer wird der Nutzer über die Leistungen des letzten Kilometers informiert. 
Nach Beendigung des Laufes werden die Ergebnisse lokal auf dem Gerät gespeichert (SQLite).
Zudem besteht die Funktion die Ergebnisse eines Laufes über sozial Media zu teilen.
In der Benutzerverwaltung werden dem Nutzer Statistiken zu den Läufen visuell mit Hilfe von Diagrammen dargestellt. 

<h2>Genrierung des Rundkurses</h2>
Der Nutzer gibt die gewünschte Distanz ein und der Standort wird ermittelt. Um möglichst einen Rundkurs zu generieren werden zwei Punkte brechnet, sodass die zwei Punkte mit dem Standort ein gleichseitiges Dreieck bilden. Die Länge der Seiten entspricht ungefähr 70% der geforderten Strecke, da die Wege der Route nicht liniar verlaufen können wird ein 30% Umweg als durchschnittlicher Mittelwert aufgerechnet. <br/>
Anschließend wird die Route zwischen den drei Punkten berechnet und geprüft, ob die Strecke der geforderten Distanz entspricht (geringe Abweichung möglich). Falls dies der Fall ist, wird die Strecke angezeigt und kann der Lauf gestartet oder eine andere Strecke generiert werden. Falls die generierte Strecke von der Distanz zu stark abweicht, wird die Generierung automatisch erneut gestartet. Falls nach 5 Versuchen kein passendes Ergebnis vorliegt, wird der Nutzer darüber informiert, dass keine Route gefunden wurde. 
