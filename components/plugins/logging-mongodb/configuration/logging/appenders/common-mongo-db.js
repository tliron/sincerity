
// MongoDB:
// http://log4mongo.org/display/PUB/Log4mongo+for+Java

var appender = appenders.commonMongoDb = new org.log4mongo.MongoDbAppender()
appender.name = 'commonMongoDb'
appender.hostname = 'localhost'
appender.databaseName = 'logs'
appender.collectionName = 'common'

// This is a high-priority root appender
rootAppenders.unshift(appender)
