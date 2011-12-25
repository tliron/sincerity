
document.executeOnce('/savory/objects/')

Savory.Objects.merge(routes, {
	'static/': new Static('static')
})
