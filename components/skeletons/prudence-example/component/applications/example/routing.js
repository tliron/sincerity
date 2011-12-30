
document.executeOnce('/savory/objects/')
document.executeOnce('/prudence/')

//var staticWeb = new Prudence.StaticWeb({root: 'static'})

app.routes = {
	'/*': [
		'explicit',
		'dynamicWeb',
		[
			'staticWeb',
			{type: 'staticWeb', root: sincerity.container.getLibrariesFile('web')}
		]
	],
	/*'/*': [
		{type: 'explicit', root: 'mapped', passThroughs: ['/prudence/fish/'], implicit: {routerDocumentName: '/prudence/implicit/', resourcesDocumentName: '/resources/'}},
		{type: 'dynamicWeb', root: 'mapped', fragmentsRoot: 'fragments'},
		{type: 'staticWeb', root: 'mapped'}
	],*/
	//'/static/*': staticWeb,
	//'/dynamic/*': 'dynamicWeb',
	//'/explicit/*': 'explicit',
	//'/prudence/router/': 'hidden',
	'/person/{name}/': 'person'
}

app.hosts = {
	'default': '/',
	internal: '/skeleton/'
}
