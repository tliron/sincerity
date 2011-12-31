
document.execute('/savory/objects/')

var logger = repository.rootLogger

if (Savory.Objects.exists(appenders.commonMongoDb)) {
	logger.addAppender(appenders.commonMongoDb)
}
else {
	logger.addAppender(appenders.commonFile)
}
