//
// This file is part of the Sincerity Foundation Library
//
// Copyright 2011-2012 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

document.require(
	'/sincerity/classes/',
	'/sincerity/objects/',
	'/sincerity/templates/',
	'/sincerity/jvm/')

var Sincerity = Sincerity || {}

/**
 * JavaScript-friendly wrapper over <a href="http://logging.apache.org/log4j/>Log4j</a>'s configuration API.
 * 
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.0
 */
Sincerity.Log4j = Sincerity.Log4j || function() {
	/** @exports Public as Sincerity.Log4j */
	var Public = {}
	
	//
	// Public
	//

	/**
	 * @class
	 * @name Sincerity.Log4j.Configuration
	 */
	Public.Configuration = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Log4j.Configuration */
	    var Public = {}

	    /**
	     * @field
	     * @returns {<a href="http://logging.apache.org/log4j/2.x/log4j-core/apidocs/index.html?org/apache/logging/log4j/core/config/Configuration.html"> org.apache.logging.log4j.core.config.Configuration</a>}
	     */
		Public.configuration = new com.threecrickets.sincerity.logging.ProgrammableLog4jConfiguration('sincerity')

		Public.use = function() {
			Public.configuration.use()
		}
	
		/**
		 * @param config
		 * @param {String} [config.name='']
		 * @param {Boolean} [config.async=true]
		 * @param {String|org.apache.logging.log4j.Level} [config.level='error']
		 * @param {Boolean} [config.additivity=true]
		 * @param {Boolean} [config.includeLocation]
		 * @param [config.appenders]
		 * @param [config.properties] TODO
		 * @param [config.filter] TODO
		 * @returns {<a href="http://logging.apache.org/log4j/2.x/log4j-core/apidocs/index.html?org/apache/logging/log4j/core/config/LoggerConfig.html">org.apache.logging.log4j.core.config.LoggerConfig</a>}
		 */
		Public.logger = function(config) {
			config.name = Sincerity.Objects.ensure(config.name, '')
			config.async = Sincerity.Objects.ensure(config.async, true)
			config.level = config.level || 'error'
			if (Sincerity.Objects.exists(config.additivity)) {
				config.additivity = config.additivity ? 'true' : 'false'
			}
			if (Sincerity.Objects.exists(config.includeLocation)) {
				config.includeLocation = config.includeLocation ? 'true' : 'false'
			}
			
			var clazz
			if (config.async) {
				if (config.name == '') {
					clazz = org.apache.logging.log4j.core.async.AsyncLoggerConfig.RootLogger
				}
				else {
					clazz = org.apache.logging.log4j.core.async.AsyncLoggerConfig
				}
			}
			else {
				if (config.name == '') {
					clazz = org.apache.logging.log4j.core.config.LoggerConfig.RootLogger
				}
				else {
					clazz = org.apache.logging.log4j.core.config.LoggerConfig
				}
			}

			var logger = clazz.createLogger(
				Sincerity.Objects.ensure(config.additivity, null), // additivity='true'
				Sincerity.Objects.ensure(config.level, null), // levelName='error'
				config.name, // loggerName
				Sincerity.Objects.ensure(config.includeLocation, null), // includeLocation
				Sincerity.JVM.newArray(0, 'org.apache.logging.log4j.core.config.AppenderRef'), // refs
				config.properties || null, // properties
				Public.configuration, // config
				config.filter || null // filter
			)

			Public.configuration.addLogger(logger.name, logger)
			
			if (Sincerity.Objects.isDict(config.appenders)) {
				for (var name in config.appenders) {
					var appender = Public.configuration.getAppender(name)
					if (Sincerity.Objects.exists(appender)) {
						var appenderConfig = config.appenders[name]
						if (Sincerity.Objects.isString(appenderConfig)) {
							appenderConfig = {level: appenderConfig}
						}
						if (Sincerity.Objects.isString(appenderConfig.level)) {
							appenderConfig.level = levels[appenderConfig.level.toLowerCase()]
						}
						appenderConfig.level = appenderConfig.level || org.apache.logging.log4j.Level.ALL
	
						logger.addAppender(
							appender, // appender
							appenderConfig.level, // level
							appenderConfig.filter || null // filter
						)
					}
				}
			}
			else if (Sincerity.Objects.exists(config.appenders)) {
				config.appenders = Sincerity.Objects.array(config.appenders)
				for (var i in config.appenders) {
					var appender = config.appenders[i]
					if (Sincerity.Objects.isString(appender)) {
						appender = Public.configuration.getAppender(appender)
					}
					if (Sincerity.Objects.exists(appender)) {
						logger.addAppender(
							appender, // appender
							null, // level
							null // filter
						)
					}
				}
			}
			
			return logger
		}

		/**
		 * @param config
		 * @param {String} config.name
		 * @param config.layout {@link Sincerity.Log4J.Configuration#patternLayout}
		 * @param config.strategy {@link Sincerity.Log4J.Configuration#defaultRolloverStrategy}
		 * @param config.policy {@link Sincerity.Log4J.Configuration#sizeBasedTriggeringPolicy}
		 * @param {Boolean} [config.append=true]
		 * @param {Boolean} [config.bufferedIO=true]
		 * @param {Boolean} [config.immediateFlush=true]
		 * @param {Boolean} [config.ignoreExceptions=true]
		 * @param {Boolean} [config.advertise]
		 * @param {String} [config.advertiseURI]
		 * @param [config.filter] TODO
		 * @returns {<a href="http://logging.apache.org/log4j/2.x/log4j-core/apidocs/index.html?org/apache/logging/log4j/core/appender/RollingFileAppender.html">org.apache.logging.log4j.core.appender.RollingFileAppender</a>}
		 */
		Public.rollingFileAppender = function(config) {
			if (Sincerity.Objects.exists(config.append)) {
				config.append = config.append ? 'true' : 'false'
			}
			if (Sincerity.Objects.exists(config.bufferedIO)) {
				config.bufferedIO = config.bufferedIO ? 'true' : 'false'
			}
			if (Sincerity.Objects.exists(config.immediateFlush)) {
				config.immediateFlush = config.immediateFlush ? 'true' : 'false'
			}
			if (Sincerity.Objects.exists(config.ignoreExceptions)) {
				config.ignoreExceptions = config.ignoreExceptions ? 'true' : 'false'
			}
			if (Sincerity.Objects.exists(config.advertise)) {
				config.advertise = config.advertise ? 'true' : 'false'
			}
			
			var policy = Public.sizeBasedTriggeringPolicy(config.policy)
			var strategy = Public.defaultRolloverStrategy(config.strategy)
			var layout = Public.patternLayout(config.layout)
			
			var appender = org.apache.logging.log4j.core.appender.RollingFileAppender.createAppender(
				config.fileName || null, // fileName
				config.filePattern || null, // filePattern
				Sincerity.Objects.ensure(config.append, null), // append='true'
				config.name || null, // name
				Sincerity.Objects.ensure(config.bufferedIO, null), // bufferedIO='true'
				Sincerity.Objects.ensure(config.immediateFlush, null), // immediateFlush='true'
				policy, // policy
				strategy, // strategy
				layout, // layout
				config.filter || null, // filter
				Sincerity.Objects.ensure(config.ignoreExceptions, null), // ignoreExceptions='true'
				Sincerity.Objects.ensure(config.advertise, null), // advertise
				config.advertiseURI || null, // advertiseURI
				Public.configuration // config
			)
			
			Public.configuration.addAppender(appender)
	
			return appender
		}
		
		/**
		 * @param config
		 * @param {String} config.name
		 * @param config.layout {@link Sincerity.Log4J.Configuration#patternLayout}
		 * @param {String} [config.target='SYSTEM_OUT']
		 * @param {Boolean} [config.follow]
		 * @param {Boolean} [config.ignoreExceptions=true]
		 * @param [config.filter] TODO
		 * @returns {<a href="http://logging.apache.org/log4j/2.x/log4j-core/apidocs/index.html?org/apache/logging/log4j/core/appender/ConsoleAppender.html">org.apache.logging.log4j.core.appender.ConsoleAppender</a>}
		 */
		Public.consoleAppender = function(config) {
			if (Sincerity.Objects.exists(config.follow)) {
				config.follow = config.follow ? 'true' : 'false'
			}
			if (Sincerity.Objects.exists(config.ignoreExceptions)) {
				config.ignoreExceptions = config.ignoreExceptions ? 'true' : 'false'
			}
	
			var layout = Public.patternLayout(config.layout)
	
			var appender = org.apache.logging.log4j.core.appender.ConsoleAppender.createAppender(
				layout, // layout
				config.filter || null, // filter
				config.target || null, // target='SYSTEM_OUT'
				config.name || null, // name
				Sincerity.Objects.ensure(config.follow, null), // follow
				Sincerity.Objects.ensure(config.ignoreExceptions, null) // ignoreExceptions='true'
			)
			
			Public.configuration.addAppender(appender)
	
			return appender
		}
		
		/**
		 * @param config
		 * @param {String} config.name
		 * @param config.layout {@link Sincerity.Log4J.Configuration#patternLayout}
		 * @param {String} config.host
		 * @param {String} config.port
		 * @param {String} [config.protocol]
		 * @param {String} [config.reconnectionDelay]
		 * @param {Boolean} [config.immediateFail]
		 * @param {Boolean} [config.immediateFlush]
		 * @param {Boolean} [config.ignoreExceptions=true]
		 * @param {Boolean} [config.advertise]
		 * @param [config.filter] TODO
		 * @returns {<a href="http://logging.apache.org/log4j/2.x/log4j-core/apidocs/index.html?org/apache/logging/log4j/core/appender/SocketAppender.html">org.apache.logging.log4j.core.appender.SocketAppender</a>}
		 */
		Public.socketAppender = function(config) {
			if (Sincerity.Objects.exists(config.immediateFail)) {
				config.immediateFail = config.immediateFail ? 'true' : 'false'
			}
			if (Sincerity.Objects.exists(config.immediateFlush)) {
				config.immediateFlush = config.immediateFlush ? 'true' : 'false'
			}
			if (Sincerity.Objects.exists(config.ignoreExceptions)) {
				config.ignoreExceptions = config.ignoreExceptions ? 'true' : 'false'
			}
			if (Sincerity.Objects.exists(config.advertise)) {
				config.advertise = config.advertise ? 'true' : 'false'
			}
	
			var layout = config.layout ? Public.patternLayout(config.layout) : null
	
			var appender = org.apache.logging.log4j.core.appender.SocketAppender.createAppender(
				config.host || null, // host
				config.port ? String(config.port) : null, // portNum: the default for log4j server is 4560. The default for Ganymede is 4445.
				config.protocol || null, // protocol
				config.reconnectionDelay || null, // reconnectionDelay
				Sincerity.Objects.ensure(config.immediateFail, null), // immediateFail
				config.name || null, // name
				Sincerity.Objects.ensure(config.immediateFlush, null), // immediateFlush
				Sincerity.Objects.ensure(config.ignoreExceptions, null), // ignoreExceptions='true'
				layout, // layout
				config.filter || null, // filter
				Sincerity.Objects.ensure(config.advertise, null), // advertise
				Public.configuration // config
			)
			
			Public.configuration.addAppender(appender)
	
			return appender
		}
		
		/**
		 * @param config
		 * @param {String} config.name
		 * @param config.provider {@link Sincerity.Log4J.Configuration#mongoDbProvider}
		 * @param {String} [config.bufferSize] 
		 * @param {Boolean} [config.ignoreExceptions=true]
		 * @param [config.filter] TODO
		 * @returns {<a href="http://logging.apache.org/log4j/2.x/log4j-core/apidocs/index.html?org/apache/logging/log4j/core/appender/db/nosql/NoSQLAppender.html">org.apache.logging.log4j.core.appender.db.nosql.NoSQLAppender</a>}
		 */
		Public.noSqlAppender = function(config) {
			if (Sincerity.Objects.exists(config.ignoreExceptions)) {
				config.ignoreExceptions = config.ignoreExceptions ? 'true' : 'false'
			}
			
			var provider = Public.mongoDbProvider(config.provider)

			var appender = org.apache.logging.log4j.core.appender.db.nosql.NoSQLAppender.createAppender(
				config.name || null, // name
				Sincerity.Objects.ensure(config.ignoreExceptions, null), // ignoreExceptions='true'
				config.filter || null, // filter
				config.bufferSize ? String(config.bufferSize) : null, // bufferSize
				provider // provider
			)
			
			Public.configuration.addAppender(appender)
	
			return appender
		}

		/**
		 * @param config
		 * @param {String} config.size
		 * @returns {<a href="http://logging.apache.org/log4j/2.x/log4j-core/apidocs/index.html?org/apache/logging/log4j/core/appender/rolling/SizeBasedTriggeringPolicy.html">org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy</a>}
		 */
		Public.sizeBasedTriggeringPolicy = function(config) {
			return org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy.createPolicy(String(config.size))
		}

		/**
		 * @param config
		 * @param {String} config.pattern
		 * @param {String} [config.charset]
		 * @param {Boolean} [config.alwaysWriteExceptions=true]
		 * @param [config.replace] TODO
		 * @returns {<a href="http://logging.apache.org/log4j/2.x/log4j-core/apidocs/index.html?org/apache/logging/log4j/core/layout/PatternLayout.html">org.apache.logging.log4j.core.layout.PatternLayout</a>}
		 */
		Public.patternLayout = function(config) {
			if (Sincerity.Objects.exists(config.alwaysWriteExceptions)) {
				config.alwaysWriteExceptions = config.alwaysWriteExceptions ? 'true' : 'false'
			}
			
			return org.apache.logging.log4j.core.layout.PatternLayout.createLayout(
				config.pattern || null,
				Public.configuration, // config
				config.replace || null, // replace
				config.charset || null, // charset
				Sincerity.Objects.ensure(config.alwaysWriteExceptions, null) // always='true'
			)
		}
	
		/**
		 * @param config
		 * @param {String} config.min
		 * @param {String} config.max
		 * @param {String} [config.fileIndex='max'] 'max' or 'min' (fixed window)
		 * @param {String} [config.compressionLevel]
		 * @returns {<a href="http://logging.apache.org/log4j/2.x/log4j-core/apidocs/index.html?org/apache/logging/log4j/core/appender/rolling/DefaultRolloverStrategy.html">org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy</a>}
		 */
		Public.defaultRolloverStrategy = function(config) {
			return org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy.createStrategy(
				config.max ? String(config.max) : null, // max
				config.min ? String(config.min) : null, // min
				config.fileIndex || null, // fileIndex='max'
				config.compressionLevel ? String(config.compressionLevel) : null, // compressionLevel
				Public.configuration // config
			)
		}
		
		/**
		 * @param config
		 * @param {String} config.collectionName
		 * @param {String} [config.databaseName]
		 * @param {String} [config.server]
		 * @param {String} [config.port]
		 * @param {String} [config.username]
		 * @param {String} [config.passwrd]
		 * @param {String} [config.writeConcernConstant]
		 * @param {String} [config.writeConcernConstantClass]
		 * @param {String} [config.factoryClassName]
		 * @param {String} [config.factoryMethodName]
		 * @param {<a href="http://api.mongodb.org/java/current/index.html?com/mongodb/MongoClient.html">com.mongodb.MongoClient</a>} [config.client]
		 * @param {<a href="http://api.mongodb.org/java/current/index.html?com/mongodb/DB.html">com.mongodb.DB</a>} [config.db]
		 * @returns {<a href="http://logging.apache.org/log4j/2.x/log4j-core/apidocs/index.html?org/apache/logging/log4j/core/appender/db/nosql/mongo/MongoDBProvider.html">org.apache.logging.log4j.core.appender.db.nosql.mongo.MongoDBProvider</a>}
		 */
		Public.mongoDbProvider = function(config) {
			if (Sincerity.Objects.exists(config.client)) {
				com.threecrickets.sincerity.logging.MongoDbFactory.client = config.client
				config.factoryClassName = 'com.threecrickets.sincerity.logging.MongoDbFactory'
				config.factoryMethodName = 'getClient'
			}
			else if(Sincerity.Objects.exists(config.db) && (!Sincerity.Objects.isString(config.db))) {
				com.threecrickets.sincerity.logging.MongoDbFactory.db = config.db
				config.factoryClassName = 'com.threecrickets.sincerity.logging.MongoDbFactory'
				config.factoryMethodName = 'getDB'
			}
			
			// See: https://issues.apache.org/jira/browse/LOG4J2-474
			//return org.apache.logging.log4j.core.appender.db.nosql.mongo.MongoDBProvider.createNoSQLProvider(
			return com.threecrickets.sincerity.logging.MongoDbLog4jProvider.createNoSQLProvider(
				config.collectionName || null, // collectionName
				config.writeConcernConstant || null, // writeConcernConstant
				config.writeConcernConstantClass || null, // writeConcernConstantClass
				config.databaseName || null, // databaseName
				config.server || null, // server
				config.port || null, // port
				config.username || null, // username
				config.password || null, // password
				config.factoryClassName || null, // factoryClassName
				config.factoryMethodName || null // factoryMethodName
			)
		}
		
		return Public
	}(Public))
		
	//
	// Private
	//

	var levels = {
		'off': org.apache.logging.log4j.Level.OFF,
		'fatal': org.apache.logging.log4j.Level.FATAL,
		'error': org.apache.logging.log4j.Level.ERROR,
		'warn': org.apache.logging.log4j.Level.WARN,
		'info': org.apache.logging.log4j.Level.INFO,
		'debug': org.apache.logging.log4j.Level.DEBUG,
		'trace': org.apache.logging.log4j.Level.TRACE,
		'all': org.apache.logging.log4j.Level.ALL
	}

	return Public
}()

