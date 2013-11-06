
// Jetty uses its own mechanism for the web (NCSA) log, which does *not* go through JVM logging

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
