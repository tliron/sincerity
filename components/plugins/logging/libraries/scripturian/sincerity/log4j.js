//
// This file is part of the Sincerity Foundation Library
//
// Copyright 2011-2014 Three Crickets LLC.
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
		Public.configuration = new com.threecrickets.sincerity.logging.ProgrammableConfiguration('sincerity')

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
			config = Sincerity.Objects.clone(config)
			config.name = Sincerity.Objects.ensure(config.name, '')
			config.async = Sincerity.Objects.ensure(config.async, true)
			config.level = config.level || 'error'
			if (Sincerity.Objects.exists(config.additivity)) {
				config.additivity = config.additivity ? 'true' : 'false'
			}
			if (Sincerity.Objects.exists(config.includeLocation)) {
				config.includeLocation = config.includeLocation ? 'true' : 'false'
			}
			
			var clazz, isRoot = false
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

			var logger
			if (config.name == '') {
				logger = clazz.createLogger(
					Sincerity.Objects.ensure(config.additivity, null), // additivity='true'
					Sincerity.Objects.ensure(config.level, null), // levelName='error'
					Sincerity.Objects.ensure(config.includeLocation, null), // includeLocation
					Sincerity.JVM.newArray(0, 'org.apache.logging.log4j.core.config.AppenderRef'), // refs
					config.properties || null, // properties
					Public.configuration, // config
					config.filter || null // filter
				)
			}
			else {
				logger = clazz.createLogger(
					Sincerity.Objects.ensure(config.additivity, null), // additivity='true'
					Sincerity.Objects.ensure(config.level, null), // levelName='error'
					config.name, // loggerName
					Sincerity.Objects.ensure(config.includeLocation, null), // includeLocation
					Sincerity.JVM.newArray(0, 'org.apache.logging.log4j.core.config.AppenderRef'), // refs
					config.properties || null, // properties
					Public.configuration, // config
					config.filter || null // filter
				)
			}

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
		 * @param {String} [config.layout.type='pattern']
		 * @param config.strategy {@link Sincerity.Log4J.Configuration#defaultRolloverStrategy}
		 * @param {String} [config.strategy.type='defaultRollover']
		 * @param config.policy {@link Sincerity.Log4J.Configuration#sizeBasedTriggeringPolicy}
		 * @param {String} [config.policy.type='sizeBasedTriggering']
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
			config = Sincerity.Objects.clone(config)
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

			var policy = null
			if (config.policy) {
				var policyType = Sincerity.Objects.ensure(config.policy.type, 'sizeBasedTriggering')
				if (policyType == 'sizeBasedTriggering') {
					policy = Public.sizeBasedTriggeringPolicy(config.policy)
				}
			}

			var strategy = null
			if (config.strategy) {
				var strategyType = Sincerity.Objects.ensure(config.strategy.type, 'defaultRollover')
				if (strategyType == 'defaultRollover') {
					strategy = Public.defaultRolloverStrategy(config.strategy)
				}
			}

			var layout = null
			if (config.layout) {
				var layoutType = Sincerity.Objects.ensure(config.layout.type, 'pattern')
				if (layoutType == 'pattern') {
					layout = Public.patternLayout(config.layout)
				}
			}

			var appender = org.apache.logging.log4j.core.appender.RollingFileAppender.createAppender(
				config.fileName || null, // fileName
				config.filePattern || null, // filePattern
				config.append || null, // append='true'
				config.name || null, // name
				config.bufferedIO || null, // bufferedIO='true'
				config.bufferSize || null, // bufferSize
				config.immediateFlush || null, // immediateFlush='true'
				policy, // policy
				strategy, // strategy
				layout, // layout
				config.filter || null, // filter
				config.ignoreExceptions || null, // ignoreExceptions='true'
				config.advertise || null, // advertise
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
		 * @param {String} [config.layout.type='pattern']
		 * @param {String} [config.target='SYSTEM_OUT']
		 * @param {Boolean} [config.follow]
		 * @param {Boolean} [config.ignoreExceptions=true]
		 * @param [config.filter] TODO
		 * @returns {<a href="http://logging.apache.org/log4j/2.x/log4j-core/apidocs/index.html?org/apache/logging/log4j/core/appender/ConsoleAppender.html">org.apache.logging.log4j.core.appender.ConsoleAppender</a>}
		 */
		Public.consoleAppender = function(config) {
			config = Sincerity.Objects.clone(config)
			if (Sincerity.Objects.exists(config.follow)) {
				config.follow = config.follow ? 'true' : 'false'
			}
			if (Sincerity.Objects.exists(config.ignoreExceptions)) {
				config.ignoreExceptions = config.ignoreExceptions ? 'true' : 'false'
			}
	
			var layout = null
			if (config.layout) {
				var layoutType = Sincerity.Objects.ensure(config.layout.type, 'pattern')
				if (layoutType == 'pattern') {
					layout = Public.patternLayout(config.layout)
				}
			}
	
			var appender = org.apache.logging.log4j.core.appender.ConsoleAppender.createAppender(
				layout, // layout
				config.filter || null, // filter
				config.target || null, // target='SYSTEM_OUT'
				config.name || null, // name
				config.follow || null, // follow
				config.ignoreExceptions || null // ignoreExceptions='true'
			)
			
			Public.configuration.addAppender(appender)
	
			return appender
		}
		
		/**
		 * @param config
		 * @param {String} config.name
		 * @param config.layout {@link Sincerity.Log4J.Configuration#patternLayout}
		 * @param {String} [config.layout.type='pattern']
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
			config = Sincerity.Objects.clone(config)
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

			var layout = null
			if (config.layout) {
				var layoutType = Sincerity.Objects.ensure(config.layout.type, 'pattern')
				if (layoutType == 'pattern') {
					layout = Public.patternLayout(config.layout)
				}
			}
	
			var appender = org.apache.logging.log4j.core.appender.SocketAppender.createAppender(
				config.host || null, // host
				Sincerity.Objects.exists(config.port) ? String(config.port) : null, // portNum: the default for log4j server is 4560. The default for Ganymede is 4445.
				config.protocol || null, // protocol
				config.reconnectionDelay || null, // reconnectionDelay
				config.immediateFail || null, // immediateFail
				config.name || null, // name
				config.immediateFlush || null, // immediateFlush
				config.ignoreExceptions || null, // ignoreExceptions='true'
				layout, // layout
				config.filter || null, // filter
				config.advertise || null, // advertise
				Public.configuration // config
			)
			
			Public.configuration.addAppender(appender)
	
			return appender
		}
		
		/**
		 * @param config
		 * @param {String} config.name
		 * @param {String} [config.bufferSize]
		 * @param {Boolean} [config.ignoreExceptions=true]
		 * @param [config.filter] TODO
		 * @param [config.uri]
		 * @param [config.db]
		 * @param [config.collection]
		 * @param [config.writeConcern]
		 * @returns {<a href="">com.threecrickets.sincerity.logging.MongoDbAppender</a>}
		 */
		Public.mongoDbAppender = function(config) {
			config = Sincerity.Objects.clone(config)
			if (Sincerity.Objects.exists(config.ignoreExceptions)) {
				config.ignoreExceptions = config.ignoreExceptions ? 'true' : 'false'
			}

			var appender = com.threecrickets.sincerity.logging.MongoDbAppender.createAppender(
				config.name || null, // name
				config.ignoreExceptions || null, // ignoreExceptions='true'
				config.filter || null, // filter
				Sincerity.Objects.exists(config.bufferSize) ? String(config.bufferSize) : null, // bufferSize
				config.uri || null, // uri
				config.db || null, // db
				config.collection || null, // collection
				config.writeConcern || null // writeConcern
			)
			
			Public.configuration.addAppender(appender)
	
			return appender
		}

		/**
		 * @param config
		 * @param {String} config.name
		 * @param {String[]} config.appenders
		 * @param config.policy {@link Sincerity.Log4J.Configuration#propertiesRewritePolicy}
		 * @param {String} [config.policy.type='propertiesRewrite']
		 * @param {Boolean} [config.ignoreExceptions=true]
		 * @param [config.filter] TODO
		 * @returns {<a href="http://logging.apache.org/log4j/2.x/log4j-core/apidocs/index.html?org/apache/logging/log4j/core/appender/rewrite/RewriteAppender.html">org.apache.logging.log4j.core.appender.rewrite.RewriteAppender</a>}
		 */
		Public.rewriteAppender = function(config) {
			config = Sincerity.Objects.clone(config)
			if (Sincerity.Objects.exists(config.ignoreExceptions)) {
				config.ignoreExceptions = config.ignoreExceptions ? 'true' : 'false'
			}
			
			var policy = null
			if (config.policy) {
				var policyType = Sincerity.Objects.ensure(config.policy.type, 'propertiesRewrite')
				if (policyType == 'propertiesRewrite') {
					policy = Public.propertiesRewritePolicy(config.policy)
				}
				else if (policyType == 'mapRewrite') {
					policy = Public.mapRewritePolicy(config.policy)
				}
			}

			var size = 0
			for (var key in config.appenders) {
				size++
			}
			
			var appenders = Sincerity.JVM.newArray(size, 'org.apache.logging.log4j.core.config.AppenderRef')
			var i  = 0
			for (var a in config.appenders) {
				appenders[a] = org.apache.logging.log4j.core.config.AppenderRef.createAppenderRef(
					config.appenders[a],
					null,
					null
				)
			}

			var appender = org.apache.logging.log4j.core.appender.rewrite.RewriteAppender.createAppender(
				config.name || null, // name
				config.ignoreExceptions || null, // ignoreExceptions='true'
				appenders, // appenderRefs,
				Public.configuration, // configuration
				policy, // rewritePolicy
				config.filter || null // filter
			)
			
			Public.configuration.addAppender(appender)
	
			return appender
		}
		
		/**
		 * @param config
		 * @param {Object} config.properties
		 * @returns {<a href="http://logging.apache.org/log4j/2.x/log4j-core/apidocs/index.html?org/apache/logging/log4j/core/appender/rewrite/PropertiesRewritePolicy.html">org.apache.logging.log4j.core.appender.rewrite.PropertiesRewritePolicy</a>}
		 */
		Public.propertiesRewritePolicy = function(config) {
			var size = 0
			for (var key in config.properties) {
				size++
			}

			var properties = Sincerity.JVM.newArray(size, 'org.apache.logging.log4j.core.config.Property')
			var i = 0
			for (var key in config.properties) {
				properties[i++] = org.apache.logging.log4j.core.config.Property.createProperty(key, config.properties[key])
			}
			
			return org.apache.logging.log4j.core.appender.rewrite.PropertiesRewritePolicy.createPolicy(
				Public.configuration, // config
				properties // properties
			)
		}
		
		/**
		 * @param config
		 * @param {Object} config.values
		 * @param {String} [config.mode='Add'] 'Add' or 'Update'
		 * @returns {<a href="http://logging.apache.org/log4j/2.x/log4j-core/apidocs/index.html?org/apache/logging/log4j/core/appender/rewrite/MapRewritePolicy.html">org.apache.logging.log4j.core.appender.rewrite.MapRewritePolicy</a>}
		 */
		Public.mapRewritePolicy = function(config) {
			var size = 0
			for (var key in config.values) {
				size++
			}

			var keyValuePairs = Sincerity.JVM.newArray(size, 'org.apache.logging.log4j.core.helpers.KeyValuePair')
			var i = 0
			for (var key in config.values) {
				keyValuePairs[i++] = org.apache.logging.log4j.core.helpers.KeyValuePair.createPair(key, config.values[key])
			}
			
			return org.apache.logging.log4j.core.appender.rewrite.MapRewritePolicy.createPolicy(
				config.mode || null, // mode='Add'
				keyValuePairs // keyValuePairs
			)
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
		 * @param {Boolean} [config.noConsoleNoAnsi=false]
		 * @param {String} [config.header]
		 * @param {String} [config.footer]
		 * @param [config.replace] TODO
		 * @returns {<a href="http://logging.apache.org/log4j/2.x/log4j-core/apidocs/index.html?org/apache/logging/log4j/core/layout/PatternLayout.html">org.apache.logging.log4j.core.layout.PatternLayout</a>}
		 */
		Public.patternLayout = function(config) {
			config = Sincerity.Objects.clone(config)
			return org.apache.logging.log4j.core.layout.PatternLayout.createLayout(
				config.pattern || null, // pattern
				Public.configuration, // config
				config.replace || null, // replace TODO
				config.charset || null, // charset
				Sincerity.Objects.exists(config.alwaysWriteExceptions) ? config.alwaysWriteExceptions : true, // alwaysWriteException
				Sincerity.Objects.exists(config.noConsoleNoAnsi) ? config.noConsoleNoAnsi : false, // noConsoleAni
				config.header || null, // header
				config.footer || null // footer
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
				Sincerity.Objects.exists(config.max) ? String(config.max) : null, // max
				Sincerity.Objects.exists(config.min) ? String(config.min) : null, // min
				config.fileIndex || null, // fileIndex='max'
				config.compressionLevel || null, // compressionLevel
				Public.configuration // config
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

