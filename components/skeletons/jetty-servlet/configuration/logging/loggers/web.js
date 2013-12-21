
document.require('/sincerity/objects/')

var logger = repository.getLogger('web')
logger.additivity = false

if (Sincerity.Objects.exists(appenders['mongoDb:web'])) {
	logger.addAppender(appenders['mongoDb:web'])
}
else if (Sincerity.Objects.exists(appenders['file:web'])) {
	logger.addAppender(appenders['file:web'])
}
