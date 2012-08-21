
//
// The socket appender is useful in conjunction with a log4j server:
//
//   sincerity start log4j-server
//
// And also with the Ganymede plugin for Eclipse:
//
//   http://ganymede.sourceforge.net/
//

/*
var appender = appenders.socket = new org.apache.log4j.net.SocketAppender()
appender.name = 'socket'
appender.remoteHost = 'localhost'
appender.port = 4560 // The default for log4j server is 4560. The default for Ganymede is 4445.
appender.locationInfo = true

// This is a low-priority root appender
rootAppenders.push(appender)
*/
