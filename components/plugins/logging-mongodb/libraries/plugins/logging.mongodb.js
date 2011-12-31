
document.executeOnce('/mongo-db/')
document.executeOnce('/savory/templates/')
document.executeOnce('/savory/objects/')
document.executeOnce('/savory/localization/')

function getCommands() {
	return ['mtail']
}

function run(command) {
	switch (String(command.name)) {
		case 'mtail':
			mtail(command)
			break
	}
}

function mtail(command) {
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
		/*
		record.timestamp
		record.level
		record.thread
		record.host.process
		record.message
		record.loggerName.fullyQualifiedClassName
		record.filename
		recrod.lineNumber
		record.method
		record['class'].fullyQualifiedClassName
		*/
		var line = '{timestamp}: {level} [{loggerName.fullyQualifiedClassName}] {message}'.cast(record)
		print(line + '\n')
	}
}
