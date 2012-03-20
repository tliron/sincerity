
var logFile = sincerity.container.getLogsFile('hadoop-security-audit.log')
logFile.parentFile.mkdirs()

var appender = appenders.hadoopSecurityAudit = new org.apache.log4j.DailyRollingFileAppender()
appender.name = 'hadoopSecurityAudit'
appender.file = logFile
appender.layout = new PatternLayout('%d{ISO8601} %p %c: %m%n')