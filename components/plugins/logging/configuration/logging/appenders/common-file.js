
var logFile = sincerity.container.getLogsFile('common.log')
logFile.parentFile.mkdirs()

var appender = appenders['file:common'] = new org.apache.log4j.RollingFileAppender()
appender.name = 'file:common'
appender.file = String(logFile)
appender.maxFileSize = '5MB'
appender.maxBackupIndex = 9
appender.layout = new PatternLayout('%d: %-5p [%c] %m%n')

// This is a low-priority root appender
rootAppenders.push(appender)
