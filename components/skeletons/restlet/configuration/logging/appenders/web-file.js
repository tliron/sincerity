
var logFile = sincerity.container.getLogsFile('web.log')
logFile.parentFile.mkdirs()

configuration.rollingFileAppender({
	name: 'file:web',
	layout: {
		pattern: '%m%n'
	},
	fileName: String(logFile),
	filePattern: String(logFile) + '.%i',
	policy: {
		size: '5MB'
	},
	strategy: {
		minIndex: '1',
		maxIndex: '9'
	}
})
