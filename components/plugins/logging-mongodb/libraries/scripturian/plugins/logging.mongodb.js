
var TEMPLATE = '{formattedDate} {level} {contextMap.origin} [{loggerName}] {message}'
var TIME_FORMAT = 'yyy-MM-dd HH:mm:ss,SSS'

/*
Available template values:

formattedDate
level
loggerName
message
source.className
source.methodName
source.fileName
source.lineNumber
marker
threadName
millis
date
thrown
contextMap.origin
contextStack
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
	var uri = properties.get('uri') || 'localhost'
	var username = properties.get('username')
	var password = properties.get('password')
	var db = Sincerity.Objects.ensure(properties.get('db'), 'logs')
	var collection = Sincerity.Objects.ensure(properties.get('collection'), 'common')
	
	//println('db: ' + db)
	//println('collection: ' + collection)
	
	// Connect
	var client = Sincerity.Objects.exists(username) ? MongoDB.connect(uri, {username: username, password: password}) : MongoDB.connect(uri)
	collection = new MongoDB.Collection(collection, {client: client, db: db})
	var c = collection.find().addOption('tailable').addOption('awaitData')
	
	var format = new Sincerity.Localization.DateTimeFormat(TIME_FORMAT)
	
	while (true) {
		var record = c.next()
		
		record = Sincerity.Objects.flatten(record)
		
		// Format date
		record.formattedDate = format.format(record.date)
		
		// Right-pad level
		while (record.level.length < 5) {
			record.level += ' '
		}
		
		println(TEMPLATE.cast(record))
	}
}
