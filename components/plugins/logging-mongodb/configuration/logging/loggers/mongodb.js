
// We must avoid logging the MongoDB driver itself, otherwise we
// would get recursion hangs with the MongoDB appender.
//
// See: ../appenders/common-mongodb.js

configuration.logger({
	name: 'org.mongodb.driver',
	level: 'off'
})
