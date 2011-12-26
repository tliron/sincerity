
document.executeOnce('/savory/objects/')

Savory.Objects.merge(settings.routes, {
	'/static/': new Static('static'),
	'=/': new Static('static')
})
