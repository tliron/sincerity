
document.executeOnce('/savory/objects/')

Savory.Objects.merge(app.settings, {
	application: {
		name: 'Skeleton',
		description: 'The example application for the Restlet skeleton',
		author: 'Three Crickets',
		owner: 'Free Software'
	},

	hosts: {
		'default' : '/'
	}
})
