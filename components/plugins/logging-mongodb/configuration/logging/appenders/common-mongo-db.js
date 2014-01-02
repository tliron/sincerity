
document.require('/mongo-db/')

var client = MongoDB.connect('localhost:27017')

var appender = configuration.noSqlAppender({
	name: 'mongoDb:common',
	provider: {
		client: client,
		databaseName: 'logs',
		collectionName: 'common'
	}
}) 

/*
var appender = configuration.rewriteAppender({
	name: 'rewrite:mongoDb:common',
	appenders: ['mongoDb:common'],
	policy: {
		values: {
			test: 'Hello!!!'
		}
	}
})
*/

// This is a high-priority root appender
configuration.rootAppenders.unshift(appender)
