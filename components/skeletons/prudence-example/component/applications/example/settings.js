
document.executeOnce('/sincerity/objects/')

Sincerity.Objects.merge(app.settings, {
	description: {
		name: 'Skeleton',
		description: 'The example application for the Prudence skeleton',
		author: 'Three Crickets',
		owner: 'Free Software'
	},

	code: {
		defaultLanguageTag: 'javascript',
		defaultExtension: 'js',
		defaultDocumentName: 'default',
		minimumTimeBetweenValidityChecks: 1000,
		libraries: ['libraries'],
		sourceVisible: true //TODO
	}
})
