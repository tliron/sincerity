
document.executeOnce('/savory/objects/')
document.executeOnce('/prudence/')

// A dict of URI patterns mapped to Prudence.Resource instances

var staticWeb = new Prudence.StaticWeb({root: 'static'})

app.routes = {
	'/': staticWeb,
	'/static/*': staticWeb,
	'/dynamic/*': 'dynamicWeb',
	'/explicit/*': 'explicit',
	//'/prudence/router/': 'hidden',
	'/person/{person}/': 'person'
}

app.resources = {
	dynamicWeb: {
		root: 'dynamic',
		fragmentsRoot: 'fragments'
	},
	explicit: {
		root: 'explicit',
		passThroughs: ['/prudence/fish/']
	},
	internal: {
		resources: '/resources/',
		explicit: '/explicit/'
	}
}
