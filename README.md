# Akka clustering example with native distributed pub-sub

## Background ##

I have been recently developing a distributed, easy to install and easy to use chat-messaging platform with Play Framework and Akka.

The main challenge when developing such a distributed messaging platform, is the synchronization of the data among the instances and in case of Akka, sharing data between actors that are on different instances or JVMs. If you simly develop for a single instance everyhing is easy, beacuse every actor can see other actors without any extra effort. My current version of the platform (backend of Hotchat, read below for more info) solves this by using the pub-sub functionality of Redis. Although Redis solves the problem, in the long run it may be a bottleneck for performance and reliability. This example, my aim is to present a working prototype of **Akka Clustering** on top of **Play Frameork 2** and Akka's native **pub-sub extension** substituting pub-sub of Redis.

I'm still using Redis as a simple data store to hold users but **not** for pub-sub in this example.

## Hotchat ##

I have developed an application called [Hotchat][1] recently. The backend of Hotchat is much more complex than this project (not due to pub-sub but many extra features). It uses pub-sub of Redis for synchronization of messages among instances and actors. In the long run, my goal is to transition the backend of Hotchat thoroughly to support Akka Clustering and its native pub-sub. Then, I will open source the project for developers to integrate with their own platforms. I will also open source its client libraries which is also very important easy integration.

## Akka Cluster Demo ##

This project is built on top of Play Framework 2.3.7 . Play initializes its own Akka system, but it does not natively support Akka Clustering. So I have added features to support clustering. We are using Redis to simply store users. I also developed a javascript client for the purpose of easy testing. My js knowledge is bit outdated, I tried my best to write it clearly and structured but it is not that good. If someone can send a pull-request to improve (using Angular or React would be greayt), it is very welcome!

![Screenshot](https://raw.githubusercontent.com/omergul123/AkkaClusterDemo/master/screenshot.png)

## How to Install

Download or clone the project.

Make sure you have a Redis instance running. You can change the default Redis configuration at **conf/application.conf**.

The initial instance should be a **"seed"** instance. We should start it with a special parameter:

````
./activator -Dseed=true "run 8001"
````

Then, we can start another instance:

````
./activator -Dakka.cluster.seed-nodes.0=akka.tcp://application@127.0.0.1:2501 -Dakka.port=2502 "run 8002"
````

And now, open your browser with two tabs:

http://127.0.0.1:8001
http://127.0.0.1:8002

Enter different usernames and start sending messages between them.

You can start more instances. given seed node can stay the same or you can provide addresses of other nodes that are up or running. Seed node does not imply being a leader node. It's simply a "door" which other nodes can join.

When starting other instances make sure you change the value of **-Dakka.port** and the port Play runs on **"run 8002"**, to prevent port clashes. For those who are not familiar with Play or Akka, it is important to note that Play and Akka are actually different things and thus, they run on different ports. 

Sometimes the default JVM memory parameters are not enough for Play to compile and run. So you may need to execute the following command **before** you try to start the instances:

````
export _JAVA_OPTIONS="-Xms64m -Xmx1024m -Xss2m -XX:MaxPermSize=512m"
````

If you encounter an unexpected error, make sure you execute the following command and try running it again:

````
./activator clean compile
````

## Contribution

Please don't hesitate to send pull requests, however I only accept it on the **develop** branch.

## Contact

[omerfarukgul.com][2]

 [1]: http://gethotchat.com
 [2]: http://www.omerfarukgul.com

