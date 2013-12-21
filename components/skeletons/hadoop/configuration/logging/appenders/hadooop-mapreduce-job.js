
// Job Summary

var logFile = sincerity.container.getLogsFile('hadoop-mapreduce-job.log')
logFile.parentFile.mkdirs()

var appender = appenders['file:hadoop.mapreduceJob'] = new org.apache.log4j.DailyRollingFileAppender()
appender.name = 'file:hadoop.mapreduceJob'
appender.file = logFile
appender.layout = new PatternLayout('%d{yy/MM/dd HH:mm:ss} %p %c{2}: %m%n')
appender.datePattern = '.yyyy-MM-dd'
