
document.execute('/sincerity/container/')

importClass(
	org.apache.log4j.PatternLayout,
	org.apache.log4j.Level)

var appenders = {}
var rootAppenders = []
var repository = org.apache.log4j.LogManager.loggerRepository

repository.resetConfiguration()

Sincerity.Container.here = sincerity.container.getConfigurationFile('logging')
Sincerity.Container.executeAll('appenders')
Sincerity.Container.executeAll('loggers')

for (var a in appenders) {
	appenders[a].activateOptions()
}
