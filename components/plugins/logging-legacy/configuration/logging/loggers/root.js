
var logger = repository.rootLogger

// Use the highest-priority root appender
if (rootAppenders.length > 0) {
	logger.addAppender(rootAppenders[0])
}
