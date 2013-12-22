
document.require('/mongo-db/')

var appender = configuration.noSqlAppender({
	name: 'mongoDb:common',
	provider: {
		client: MongoDB.connect('localhost'),
		db: 'logs',
		collection: 'common'
	}
}) 

// This is a high-priority root appender
configuration.rootAppenders.unshift(appender)
