
var resources = {}

document.executeOnce(application.globals.get('prudence.implicit.resourcesDocumentName'))

function handle(conversation, method) {
	var id = conversation.locals.get('prudence.id')
	var resource = resources[id]
	if (undefined === resource) {
		conversation.statusCode = 404
		return null
	}
	method = resource[method]
	if (undefined === method) {
		conversation.statusCode = 405
		return null
	}
	return method(conversation)
}

function handleInit(conversation) {
	return handle(conversation, 'handleInit')
}

function handleGet(conversation) {
	return handle(conversation, 'handleGet')
}

function handleGetInfo(conversation) {
	return handle(conversation, 'handleGetInfo')
}

function handlePost(conversation) {
	return handle(conversation, 'handlePost')
}

function handlePut(conversation) {
	return handle(conversation, 'handlePut')
}

function handleDelete(conversation) {
	return handle(conversation, 'handleDelete')
}