
document.require('/sincerity/objects/')

var logger = repository.getLogger('web')
logger.additivity = false

if (Sincerity.Objects.exists(appenders.webMongoDb)) {
	logger.addAppender(appenders.webMongoDb)
}
else {
	logger.addAppender(appenders.webFile)
}
