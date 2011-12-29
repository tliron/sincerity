
document.executeOnce('/savory/objects/')
document.executeOnce('/prudence/')

var staticWeb = new Prudence.StaticWeb({root: 'static'})

app.uris = {
	'/': staticWeb,
	'/static/*': staticWeb,
	'/dynamic/*': 'dynamicWeb',
	'/explicit/*': 'explicit',
	//'/prudence/router/': 'hidden',
	'/person/{name}/': 'person'
}

app.hosts = {
	'default': '/',
	internal: '/skeleton/'
}

app.resources = {
	dynamicWeb: {
		root: 'dynamic',
		fragmentsRoot: 'fragments'
	},
	explicit: {
		root: 'explicit',
		implicitRouterDocumentName: '/prudence/implicit/',
		passThroughs: ['/prudence/fish/'],
	},
	implicit: {
		resourcesDocumentName: '/resources/'
	}
}
