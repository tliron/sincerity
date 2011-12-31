
var logFile = sincerity.container.getLogsFile('common.log')
logFile.parentFile.mkdirs()

var appender = appenders.commonFile = new org.apache.log4j.RollingFileAppender()
appender.name = 'commonFile'
appender.file = logFile
appender.maxFileSize = '5MB'
appender.maxBackupIndex = 9
appender.layout = new PatternLayout('%d: %-5p [%c] %m%n')
