
document.executeOnce('/savory/objects/')
document.executeOnce('/prudence/')

// A dict of URI patterns mapped to Prudence.Resource instances

var staticWeb = new Prudence.StaticWeb({root: 'static'})

Savory.Objects.merge(app.routes, {
	'/': staticWeb,
	'/static/*': staticWeb,
	'/dynamic/*': new Prudence.DynamicWeb({root: 'dynamic'}),
	'/resources/*': new Prudence.Resources({root: 'resources'}),
	'/person/{person}/': new Prudence.Custom({type: 'person'})
})
