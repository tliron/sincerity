
importClass(
	org.eclipse.jetty.server.handler.RequestLogHandler,
	org.eclipse.jetty.server.Slf4jRequestLog)

var requestLog = new Slf4jRequestLog()
requestLog.loggerName = 'web'

var requestLogHandler = new RequestLogHandler()
requestLogHandler.requestLog = requestLog

handlers.addHandler(requestLogHandler)

/*
// Jetty can also use its own mechanism for the web log, which does *not* go through SLF4J

importClass(
	org.eclipse.jetty.server.handler.RequestLogHandler,
	org.eclipse.jetty.server.NCSARequestLog)

var logFile = sincerity.container.getFile('logs', 'web', 'yyyy_mm_dd.log')
logFile.parentFile.mkdirs()

var requestLog = new NCSARequestLog(logFile)
requestLog.retainDays = 90
requestLog.append = true
requestLog.extended = false
requestLog.logTimeZone = 'GMT'

var requestLogHandler = new RequestLogHandler()
requestLogHandler.requestLog = requestLog

handlers.addHandler(requestLogHandler)
*/