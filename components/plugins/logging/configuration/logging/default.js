
document.execute('/sincerity/container/')

importClass(
	org.apache.log4j.PatternLayout)

var appenders = {}
var repository = org.apache.log4j.LogManager.loggerRepository

repository.resetConfiguration()

Sincerity.Container.here = sincerity.container.getConfigurationFile('logging')
Sincerity.Container.include('appenders')
Sincerity.Container.include('loggers')

for (var a in appenders) {
	appenders[a].activateOptions()
}
