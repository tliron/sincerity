
var appender = appenders.console = new org.apache.log4j.ConsoleAppender()
appender.name = 'console'
appender.layout = new PatternLayout('%d: %-5p [%c] %m%n')
