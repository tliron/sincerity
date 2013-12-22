
document.require('/mongo-db/')

var client = MongoDB.connect('localhost:27017')

var appender = configuration.noSqlAppender({
	name: 'mongoDb:common',
	provider: {
		client: client,
		db: 'logs',
		collection: 'common'
	}
}) 

// This is a high-priority root appender
configuration.rootAppenders.unshift(appender)
