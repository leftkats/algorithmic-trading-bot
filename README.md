# Real time stock prediction tool
The application that will be presented supports real time processing and decision making, interacting with the BtcChina websocket API.

## Built With
* [Maven](https://maven.apache.org/) - Dependency Management
## Connection
* [socket.io-java-client](https://github.com/nkzawa/socket.io-java-client)
## Prediction Model
Implemented in R as described [here](https://dspace.mit.edu/openaccess-disseminate/1721.1/101044).
Output signal cases:
```
sig < 0	-> SELL
sig = 0	-> NO POSITION
sig > 0	-> BUY
```

