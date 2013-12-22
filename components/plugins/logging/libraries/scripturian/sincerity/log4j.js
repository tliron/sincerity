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

		Public.configuration = new com.threecrickets.sincerity.util.ProgrammableLog4jConfiguration('sincerity')

		Public.use = function() {
			Public.configuration.use()
		}
	
		/**
		 * 
		 * @param config
		 * @param {String} [config.name='']
		 * @param {String|Level} [config.level='error']
		 * @param {Boolean} [config.async=true]
		 * @param {Boolean} [config.additivity=true]
		 * @param [config.appenders]
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
			if (Sincerity.Objects.exists(config.ignore)) {
				config.ignore = config.ignore ? 'true' : 'false'
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
				Sincerity.Objects.ensure(config.ignore, null), // ignore='true'
				Sincerity.Objects.ensure(config.advertise, null), // advertise
				config.advertiseUri || null, // advertiseURI
				Public.configuration // config
			)
			
			Public.configuration.addAppender(appender)
	
			return appender
		}
		
		Public.consoleAppender = function(config) {
			if (Sincerity.Objects.exists(config.follow)) {
				config.follow = config.follow ? 'true' : 'false'
			}
			if (Sincerity.Objects.exists(config.ignore)) {
				config.ignore = config.ignore ? 'true' : 'false'
			}
	
			var layout = Public.patternLayout(config.layout)
	
			var appender = org.apache.logging.log4j.core.appender.ConsoleAppender.createAppender(
				layout, // layout
				config.filter || null, // filter
				config.t || null, // t='SYSTEM_OUT'
				config.name || null, // name
				Sincerity.Objects.ensure(config.follow, null), // follow
				Sincerity.Objects.ensure(config.ignore, null) // ignore='true'
			)
			
			Public.configuration.addAppender(appender)
	
			return appender
		}
		
		Public.socketAppender = function(config) {
			if (Sincerity.Objects.exists(config.immediateFail)) {
				config.immediateFail = config.immediateFail ? 'true' : 'false'
			}
			if (Sincerity.Objects.exists(config.immediateFlush)) {
				config.immediateFlush = config.immediateFlush ? 'true' : 'false'
			}
			if (Sincerity.Objects.exists(config.ignore)) {
				config.ignore = config.ignore ? 'true' : 'false'
			}
			if (Sincerity.Objects.exists(config.advertise)) {
				config.advertise = config.advertise ? 'true' : 'false'
			}
	
			var layout = config.layout ? Public.patternLayout(config.layout) : null
	
			var appender = org.apache.logging.log4j.core.appender.SocketAppender.createAppender(
				config.host || null, // host
				config.port ? String(config.port) : null, // portNum: the default for log4j server is 4560. The default for Ganymede is 4445.
				config.protocol || null, // protocol
				config.delay || null, // delay
				Sincerity.Objects.ensure(config.immediateFail, null), // immediateFail
				config.name || null, // name
				Sincerity.Objects.ensure(config.immediateFlush, null), // immediateFlush
				Sincerity.Objects.ensure(config.ignore, null), // ignore='true'
				layout, // layout
				config.filter || null, // filter
				Sincerity.Objects.ensure(config.advertise, null), // advertise
				Public.configuration // config
			)
			
			Public.configuration.addAppender(appender)
	
			return appender
		}
		
		Public.noSqlAppender = function(config) {
			if (Sincerity.Objects.exists(config.ignore)) {
				config.ignore = config.ignore ? 'true' : 'false'
			}
			
			var provider = Public.mongoDbProvider(config.provider)

			var appender = org.apache.logging.log4j.core.appender.db.nosql.NoSQLAppender.createAppender(
				config.name || null, // name
				Sincerity.Objects.ensure(config.ignore, null), // ignore='true'
				config.filter || null, // filter
				config.bufferSize ? String(config.bufferSize) : null, // bufferSize
				provider // provider
			)
			
			Public.configuration.addAppender(appender)
	
			return appender
		}
	
		Public.sizeBasedTriggeringPolicy = function(config) {
			return org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy.createPolicy(String(config.size))
		}
	
		Public.patternLayout = function(config) {
			if (Sincerity.Objects.exists(config.always)) {
				config.always = config.always ? 'true' : 'false'
			}
			
			return org.apache.logging.log4j.core.layout.PatternLayout.createLayout(
				config.pattern || null,
				Public.configuration, // config
				config.replace || null, // replace
				config.charset || null, // charsetName
				Sincerity.Objects.ensure(config.always, null) // always='true'
			)
		}
	
		/**
		 * @param config.fileIndex='max' 'max' or 'min' (fixed window)
		 */
		Public.defaultRolloverStrategy = function(config) {
			return org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy.createStrategy(
				config.maxIndex ? String(config.maxIndex) : null, // maxIndex
				config.minIndex ? String(config.minIndex) : null, // minIndex
				config.fileIndex || null, // fileIndex='max'
				config.compressionLevel ? String(config.compressionLevel) : null, // compressionLevelStr
				Public.configuration // config
			)
		}
		
		Public.mongoDbProvider = function(config) {
			if (Sincerity.Objects.exists(config.client)) {
				com.threecrickets.sincerity.util.MongoDbFactory.client = config.client
				config.factoryClass = 'com.threecrickets.sincerity.util.MongoDbFactory'
				config.factoryMethod = 'getClient'
			}
			else if(Sincerity.Objects.exists(config.db) && (!Sincerity.Objects.isString(config.db))) {
				com.threecrickets.sincerity.util.MongoDbFactory.db = config.db
				config.factoryClass = 'com.threecrickets.sincerity.util.MongoDbFactory'
				config.factoryMethod = 'getDB'
			}
			
			// See: https://issues.apache.org/jira/browse/LOG4J2-474
			//return org.apache.logging.log4j.core.appender.db.nosql.mongo.MongoDBProvider.createNoSQLProvider(
			return com.threecrickets.sincerity.util.MongoDbLog4jProvider.createNoSQLProvider(
				config.collection || null, // collectionName
				config.writeConcern || null, // writeConcernConstant
				config.writeConcernClass || null, // writeConcernConstantClassName
				config.db || null, // databaseName
				config.host || null, // server
				config.port || null, // port
				config.username || null, // username
				config.password || null, // password
				config.factoryClass || null, // factoryClassName
				config.factoryMethod || null // factoryMethodName
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

