
var logFile = sincerity.container.getLogsFile('hadoop.log')
logFile.parentFile.mkdirs()

var appender = appenders['file:hadoop'] = new org.apache.log4j.DailyRollingFileAppender()
appender.name = 'file:hadoop'
appender.file = logFile
appender.datePattern = '.yyyy-MM-dd'
appender.layout = new PatternLayout('%d{ISO8601} %p %c: %m%n') // debug layout: %d{ISO8601} %-5p %c{2} (%F:%M(%L)) - %m%n
// appender.maxBackupIndex = 30 // 30-day backup

// This is a high-priority root appender
rootAppenders.unshift(appender)
