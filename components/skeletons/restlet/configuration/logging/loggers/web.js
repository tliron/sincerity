
document.execute('/sincerity/objects/')

var logger = repository.getLogger('org.restlet.Component.LogService')
logger.additivity = false

if (Sincerity.Objects.exists(appenders.webMongoDb)) {
	logger.addAppender(appenders.webMongoDb)
}
else {
	logger.addAppender(appenders.webFile)
}
