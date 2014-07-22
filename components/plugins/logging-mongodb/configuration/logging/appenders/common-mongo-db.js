
configuration.mongoDbAppender({
	name: 'raw:mongoDb:common',
	uri: 'mongodb://localhost:27017/',
	db: 'logs',
	collection: 'common',
	writeConcern: 'acknowledged'
})

var origin = java.net.InetAddress.localHost.hostAddress

var appender = configuration.rewriteAppender({
	name: 'mongoDb:common',
	appenders: ['raw:mongoDb:common'],
	policy: {
		properties: {
			origin: origin
		}
	}
})

// This is a high-priority root appender
configuration.rootAppenders.unshift(appender)
