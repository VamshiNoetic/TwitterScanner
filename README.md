# TwitterScanner
Development Assignment Overview
Find out how the number of mentions of a company on Twitter change over time.
Description
Implement a class that follows messages on  Twitter’s Streaming API  and counts how often a given company (e.g. “Facebook”) is mentioned. Every hour, the relative change should be stored.
Use the  “GET statuses/sample” API . This will only return a small sample of all tweets, so we’re not interested in absolute numbers, but rather percentage changes. If you e.g. count 100 mentions of “Facebook” between 12:00am and 01:00am, and 70 mentions between 01:00am and 02:00am, you should report a decrease by 30%.
