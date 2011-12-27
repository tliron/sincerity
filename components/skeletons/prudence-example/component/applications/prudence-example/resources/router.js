
function handleInit(conversation) {
	conversation.addMediaTypeByName('text/plain')
}
​
function handleGet(conversation) {
	return conversation.locals.get('type')
}
