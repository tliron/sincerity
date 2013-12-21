
configuration.logger({
	name: 'web',
	level: 'all',
	appenders: ['mongoDb:web', 'file:web'],
	additivity: false
})
