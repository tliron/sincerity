
document.execute('/savory/objects/')

var logger = repository.getLogger('org.restlet.Component.LogService')
logger.additivity = false

if (Savory.Objects.exists(appenders.webMongoDb)) {
	logger.addAppender(appenders.webMongoDb)
}
else {
	logger.addAppender(appenders.webFile)
}
