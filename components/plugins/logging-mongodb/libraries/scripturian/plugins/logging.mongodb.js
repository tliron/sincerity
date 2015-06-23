
document.require(
		'/mongodb/',
		'/sincerity/json/',
		'/sincerity/templates/',
		'/sincerity/objects/',
		'/sincerity/localization/')

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

	try {
		// Connect
		//var client = Sincerity.Objects.exists(username) ? MongoDB.connect(uri, {username: username, password: password}) : MongoDB.connect(uri)
		var client = MongoClient.connect(uri)
		db = client.database(db)
		collection = db.collection(collection, db)
		var cursor = collection.find(null, {cursorType: 'tailableAwait'})
		
		var format = new Sincerity.Localization.DateTimeFormat(TIME_FORMAT)
		
		while (true) {
			var record = cursor.next()
			
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
	catch (x) {
		if (x instanceof MongoError) {
			command.sincerity.err.println('MongoError:')
			command.sincerity.err.println(Sincerity.JSON.to(x.clean(), true))
		}
		throw x
	}
}
