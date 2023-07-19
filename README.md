



Task:

Old version - NotificationService -> v1
New version - EventBus            -> v2
Different events payload in v2 
Cache  - Ehcache

two servers - active | passive

when v2 is deployed, it must take all cache entries for v1,
and convert them into v2
But v1 must be still operational in case of rollback



Test flow:
1. Create a code to convert old events into new events
Example of v1 payload:
   transactionId=22222222, Notification= Notification{dateCreated=1689693884005, dateReceived=1689693987201, eventNotification=NotificationTestEvent(eventData=DEFAULT_EVENT)}



vm options:

-Dserver.port=8081 -Djava.net.preferIPv4Stack=true -Xmx2048m --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.util.concurrent=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/java.math=ALL-UNNAMED --add-opens java.base/java.time=ALL-UNNAMED --add-opens java.base/java.time.zone=ALL-UNNAMED --add-opens java.base/sun.util.calendar=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/sun.reflect.annotation=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED


start:
two applications, one on port 8081, second on port 8082
ehcache must be in sync. can be tested by adding one element using server1 and get element server2