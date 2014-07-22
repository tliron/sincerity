
var TEMPLATE = '{formattedTimestamp} {level} {thread.contextMap.origin} [{logger}] {message}'
var TIME_FORMAT = 'yyy-MM-dd HH:mm:ss,SSS'

/*
Available template values:

formattedTimestamp
level
logger
marker
message
source.class
source.method
source.file
source.line
thread.name
thread.contextMap.origin
*/

document.require(
	'/mongo-db/',
	'/sincerity/templates/',
	'/sincerity/objects/',
	'/sincerity/localization/')

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['logtail']
}

function run(command) {
	switch (String(command.name)) {
		case 'logtail':
			logtail(command)
			break
	}
}

function logtail(command) {
	// Properties
	command.parse = true
	var properties = command.properties
	var uri = properties.get('uri') || 'mongodb://localhost:27017/'
	var username = properties.get('username')
	var password = properties.get('password')
	var db = Sincerity.Objects.ensure(properties.get('db'), 'logs')
	var collection = Sincerity.Objects.ensure(properties.get('collection'), 'common')
	
	// Connect
	var client = Sincerity.Objects.exists(username) ? MongoDB.connect(uri, {username: username, password: password}) : MongoDB.connect(uri)
	collection = new MongoDB.Collection(collection, {client: client, db: db})
	var c = collection.find().addOption('tailable').addOption('awaitData').addOption('noTimeout')
	
	var format = new Sincerity.Localization.DateTimeFormat(TIME_FORMAT)
	
	while (true) {
		var record = c.next()
		
		record = Sincerity.Objects.flatten(record)
		
		// Format date
		record.formattedTimestamp = format.format(record.timestamp)
		
		// Right-pad level
		while (record.level.length < 5) {
			record.level += ' '
		}
		
		println(TEMPLATE.cast(record))
	}
}
