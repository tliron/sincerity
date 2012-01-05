
var TEMPLATE = '{timestamp} {level} {host.ip} [{loggerName.fullyQualifiedClassName}] {message}'
var TIME_FORMAT = 'yyy-MM-dd HH:mm:ss,SSS'

/*
Available template values:

timestamp
level
thread
host.process
message
loggerName.fullyQualifiedClassName
filename
lineNumber
method
class.fullyQualifiedClassName
host.ip
host.name
*/

document.executeOnce('/mongo-db/')
document.executeOnce('/sincerity/templates/')
document.executeOnce('/sincerity/objects/')
document.executeOnce('/sincerity/localization/')

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
	var connection = properties.get('connection')
	var username = properties.get('username')
	var password = properties.get('password')
	var db = Sincerity.Objects.ensure(properties.get('db'), 'logs')
	var collection = Sincerity.Objects.ensure(properties.get('collection'), 'common')
	
	//print('db: ' + db + '\n')
	//print('collection: ' + collection + '\n')
	
	// Connect
	var connection = MongoDB.connect(connection, null, username, password)
	collection = new MongoDB.Collection(collection, {connection: connection, db: db})
	var c = collection.find().addOption('tailable').addOption('awaitData')
	
	var format = new Sincerity.Localization.DateTimeFormat(TIME_FORMAT)
	
	while (true) {
		var record = c.next()
		
		record = Sincerity.Objects.flatten(record)
		
		// Format timestamp
		record.timestamp = format.format(record.timestamp)
		
		// Right-pad level
		while (record.level.length < 5) {
			record.level += ' '
		}
		
		print(TEMPLATE.cast(record) + '\n')
	}
}
