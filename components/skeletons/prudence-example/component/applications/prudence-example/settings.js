
document.executeOnce('/savory/objects/')

Savory.Objects.merge(app.settings, {
	description: {
		name: 'Skeleton',
		description: 'The example application for the Prudence skeleton',
		author: 'Three Crickets',
		owner: 'Free Software'
	},

	hosts: {
		'default' : '/'
	}
})
