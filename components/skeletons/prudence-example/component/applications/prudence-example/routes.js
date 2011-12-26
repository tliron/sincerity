
document.executeOnce('/savory/objects/')
document.executeOnce('/prudence/')

// A dict of URI patterns mapped to Prudence.Resource instances

var staticWeb = new Prudence.StaticWeb({root: 'static'})

Savory.Objects.merge(app.routes, {
	'/static/': staticWeb,
	'=/': staticWeb,
	'/dynamic/': new Prudence.DynamicWeb({root: 'dynamic'})
})
