
//
// The socket appender is useful in conjunction with a log4j server:
//
//   sincerity delegate:start log4j-server
//
// And also with the Ganymede plugin for Eclipse:
//
//   http://ganymede.sourceforge.net/
//

/*
var appender = configuration.socketAppender({
	name: 'socket',
	host: 'localhost', // host
	port: '4560' // the default for log4j server is 4560. The default for Ganymede is 4445.
})

// This is a high-priority root appender
configuration.rootAppenders.unshift(appender)
*/
