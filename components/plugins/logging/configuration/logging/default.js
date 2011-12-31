
document.execute('/savory/sincerity/')

importClass(
	org.apache.log4j.PatternLayout)

var appenders = {}
var repository = org.apache.log4j.LogManager.loggerRepository

repository.resetConfiguration()

Savory.Sincerity.here = sincerity.container.getConfigurationFile('logging')
Savory.Sincerity.include('appenders')
Savory.Sincerity.include('loggers')

for (var a in appenders) {
	appenders[a].activateOptions()
}
