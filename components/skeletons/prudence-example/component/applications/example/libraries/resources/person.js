
document.executeOnce('/sincerity/classes/')

Person = Sincerity.Classes.define(function() {
	var Public = {}
	
	Public.handleInit = function(conversation) {
		conversation.addMediaTypeByName('text/plain')
	}

	Public.handleGet = function(conversation) {
		return 'I am person ' + conversation.locals.get('name')
	}

	return Public
}())