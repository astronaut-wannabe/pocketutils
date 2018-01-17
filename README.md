# README #
## What
This app pulls a bunch of articles from your [Pocket][0] queue and then shuffles them. 
- *Swipe Left*: deletes the article from your queue
- *Swipe Right*: moves the article to the top of your queue

![gif][1]

## Setup
You will need to get a **consumer key** from the [pocket developer page][2]

You will then need to put that `consumer_key` in [PocketClient.java][4]
![pocketclient.java][3]

## Build

To build the app, you should just be able to clone and run the Gradle build commands.

If you have and Android device connected you should be able to follow these steps:

```bash
git clone https://github.com/astronaut-wannabe/pocketutils.git
cd pocketutils
./gradlew installDebug
```

### What is this repository for? ###

* This Android app provides some utilities for people who use [Pocket][0] heavily.
* version 0.02

### How do I get set up? ###

See above for how to set everything up.

To see all available Gradle tasks go to the project's root directory and run:

```bash
./gradlew tasks
```

[0]:https://getpocket.com/about
[1]:https://raw.githubusercontent.com/astronaut-wannabe/pocketutils/master/doc/pocketutil.gif
[2]:https://getpocket.com/developer/apps/
[3]:https://raw.githubusercontent.com/astronaut-wannabe/pocketutils/master/doc/pocket_auth.png
[4]:https://github.com/astronaut-wannabe/pocketutils/blob/master/pocket/src/main/java/com/astronaut_wannabe/PocketClient.java#L18