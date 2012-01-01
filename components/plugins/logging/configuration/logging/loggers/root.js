
document.execute('/sincerity/objects/')

var logger = repository.rootLogger

if (Sincerity.Objects.exists(appenders.commonMongoDb)) {
	logger.addAppender(appenders.commonMongoDb)
}
else {
	logger.addAppender(appenders.commonFile)
}
