# TwitterScanner
Development Assignment Overview
Find out how the number of mentions of a company on Twitter change over time.
Description
Implement a class that follows messages on  Twitter’s Streaming API  and counts how often a given company (e.g. “Facebook”) is mentioned. Every hour, the relative change should be stored.
Use the  “GET statuses/sample” API . This will only return a small sample of all tweets, so we’re not interested in absolute numbers, but rather percentage changes. If you e.g. count 100 mentions of “Facebook” between 12:00am and 01:00am, and 70 mentions between 01:00am and 02:00am, you should report a decrease by 30%.



# Steps to install project :
Project has been built using Maven. 
Clone it and import it in any IDE using POM
run the command : mvn clean install

# Steps to update Configs :
please update the config file with your Twitter developer keys
open the file twitter_config.properties and update with your keys

# Steps to run :
To run the project, open the class TwitterScanner.java and run the main method.

