
function handleInit(conversation) {
	conversation.addMediaTypeByName('text/plain')
}
​
function handleGet(conversation) {
	return 'This is a dynamic page, generated via a scriptlet, at ' + new Date()
}
