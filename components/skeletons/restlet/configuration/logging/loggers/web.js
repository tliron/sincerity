
configuration.logger({
	name: 'web', // 'org.restlet.Component.LogService'
	level: 'info',
	appenders: ['mongoDb:web', 'file:web'],
	additivity: false
})
