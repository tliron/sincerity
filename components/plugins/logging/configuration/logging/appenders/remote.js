
// The remote appender is useful in conjunction with the Ganymede plugin for Eclipse:
// http://ganymede.sourceforge.net/

var appender = appenders.remote = new org.apache.log4j.net.SocketAppender()
appender.name = 'remote'
appender.remoteHost = 'localhost'
appender.port = 4445
appender.locationInfo = true
