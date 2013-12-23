
var logFile = sincerity.container.getLogsFile('common.log')
logFile.parentFile.mkdirs()

var appender = configuration.rollingFileAppender({
	name: 'file:common',
	layout: {
		pattern: '%d: %-5p [%c] %m%n'
	},
	fileName: String(logFile),
	filePattern: String(logFile) + '.%i',
	policy: {
		size: '5MB'
	},
	strategy: {
		min: '1',
		max: '9'
	}
})

// This is a low-priority root appender
configuration.rootAppenders.push(appender)
