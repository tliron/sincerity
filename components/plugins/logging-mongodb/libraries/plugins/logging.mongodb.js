
var TEMPLATE = '{timestamp} {level} {host.ip} [{loggerName.fullyQualifiedClassName}] {message}'

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
document.executeOnce('/savory/templates/')
document.executeOnce('/savory/objects/')
document.executeOnce('/savory/localization/')

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
	var connection = MongoDB.connect()
	var collection = new MongoDB.Collection('common', {connection: connection, db: 'logs'})
	var c = collection.find().addOption('tailable').addOption('awaitData')
	
	var format = new Savory.Localization.DateTimeFormat('yyy-MM-dd HH:mm:ss.SSS')
	
	while (true) {
		var record = c.next()
		
		record = Savory.Objects.flatten(record)
		
		record.timestamp = format.format(record.timestamp)
		
		while (record.level.length < 5) {
			record.level += ' '
		}
		print(TEMPLATE.cast(record) + '\n')
	}
}
