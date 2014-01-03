
document.require('/mongo-db/')

var client = MongoDB.connect('localhost:27017')

configuration.noSqlAppender({
	name: 'raw:mongoDb:common',
	provider: {
		client: client,
		databaseName: 'logs',
		collectionName: 'common'
	}
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
