
document.require(
	'/sincerity/container/',
	'/sincerity/log4j/')

var configuration = new Sincerity.Log4j.Configuration()
configuration.rootAppenders = []

Sincerity.Container.here = sincerity.container.getConfigurationFile('logging')
Sincerity.Container.executeAll('appenders')
Sincerity.Container.executeAll('loggers')

configuration.use()
