
configuration.logger({
	name: 'web', // 'org.restlet.Component.LogService'
	level: 'all',
	appenders: ['mongoDb:web', 'file:web'],
	additivity: false
})
