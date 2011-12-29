
resources['person'] = {
	handleInit: function(conversation) {
		conversation.addMediaTypeByName('text/plain')
	},
	
	handleGet: function(conversation) {
		return 'magic'
	}
}