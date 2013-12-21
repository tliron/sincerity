
var logFile = sincerity.container.getLogsFile('web.log')
logFile.parentFile.mkdirs()

var appender = appenders['file:web'] = new org.apache.log4j.RollingFileAppender()
appender.name = 'file:web'
appender.file = String(logFile)
appender.maxFileSize = '5MB'
appender.maxBackupIndex = 9
appender.layout = new PatternLayout('%m%n')
