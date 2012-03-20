
var appender = appenders.hadoopTaskLog = new org.apache.hadoop.mapred.TaskLogAppender()
appender.name = 'hadoopTaskLog'
appender.taskId = null
appender.isCleanup = false
appender.totalLogFileSize = 100
// appender.noKeepSplits = 4
// appender.purgeLogSplits = true
// appender.logsRetainHours = 12
appender.layout = new PatternLayout('%d{ISO8601} %p %c: %m%n')
