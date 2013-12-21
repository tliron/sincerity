
// Use the highest-priority (first) root appender
if (configuration.rootAppenders.length > 0) {
	configuration.logger({
		level: 'warn',
		appenders: configuration.rootAppenders[0]
	})
}
