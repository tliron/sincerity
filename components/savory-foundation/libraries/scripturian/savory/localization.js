//
// This file is part of the Savory Foundation Library for JavaScript
//
// Copyright 2011 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.opensource.org/licenses/lgpl-3.0.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

document.executeOnce('/savory/classes/')
document.executeOnce('/savory/jvm/')
document.executeOnce('/savory/templates/')

var Savory = Savory || {}

/**
 * Utilities for formatting and parsing dates, currency, etc.
 * <p>
 * Note: This library modifies the Date and String prototypes.
 *
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.2
 */
Savory.Localization = Savory.Localization || function() {
	/** @exports Public as Savory.Localization */
    var Public = {}

	Public.round = function(number, decimals) {
		decimals = Math.pow(10, decimals)
		return Math.round(number * decimals) / decimals
	}

	Public.formatNumber = function(number) {
		var d = String(number).split('.')
		var before = d[0]
		var after = d.length > 1 ? '.' + d[1] : ''
		while (formatNumberRegExp.test(before)) {
			before = before.replace(formatNumberRegExp, '$1' + ',' + '$2');
		}
		return before + after;
	}

	Public.formatDuration = function(duration, longFormat) {
		if (duration < 1000) {
			return Public.formatNumber(Public.round(duration, 0)) + (longFormat ? ' milliseconds' : 'ms')
		}
		if (duration < 60000) {
			return Public.formatNumber(Public.round(duration / 1000, 0)) + (longFormat ? ' seconds' : 's')
		}
		if (duration < 60000 * 60) {
			return Public.formatNumber(Public.round(duration / 60000, 2)) + (longFormat ? ' minutes' : 'm')
		}
		return Public.formatNumber(Public.round(duration / (60000 * 60), 2)) + (longFormat ? ' hours' : 'h')
	}
		
	/**
	 * Parses a string into a date.
	 * 
	 * @param {String|Savory.Localization.DateTimeFormat} format See <a href="http://download.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html">pattern rules</a>
	 * @param {String} [timeZone]
	 * @returns {Date}
	 * @see Savory.Localization.DateTimeFormat#parse
	 */
	Public.parseDateTime = function(string, format, timeZone) {
		if (Savory.Objects.isString(format)) {
			return new Public.DateTimeFormat(format).parse(string, timeZone)
		}
		else {
			return format.parse(string, timeZone)
		}
	}
	
	/**
	 * Formats a date as a string.
	 * 
	 * @param {Date} date The date
	 * @param {String|Savory.Localization.DateTimeFormat} format See <a href="http://download.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html">pattern rules</a>
	 * @param {String} [timeZone]
	 * @returns {String}
	 * @see Savory.Localization.DateTimeFormat#format
	 */
	Public.formatDateTime = function(date, format, timeZone) {
		if (Savory.Objects.isString(format)) {
			return new Public.DateTimeFormat(format).format(date, timeZone)
		}
		else {
			return format.format(date, timeZone)
		}
	}
	
	/**
	 * @param {String} [dateStyle] 'short', 'medium', 'long' or 'full'
	 * @param {String} [timeStyle] 'short', 'medium', 'long' or 'full'
	 * @param [locale] See {@link Savory.JVM#getLocale} for format
	 * @returns Savory.Localization.DateTimeFormat
	 */
	Public.getDateTimeFormat = function(dateStyle, timeStyle, locale) {
		if (locale) {
			return new Public.DateTimeFormat(java.text.DateFormat.getDateTimeInstance(dateTimeFormatStyles[dateStyle], dateTimeFormatStyles[timeStyle], Savory.JVM.toLocale(locale)))
		}
		else {
			if (dateStyle) {
				return new Public.DateTimeFormat(java.text.DateFormat.getDateTimeInstance(dateTimeFormatStyles[dateStyle], dateTimeFormatStyles[timeStyle]))
			}
			else {
				return new Public.DateTimeFormat(java.text.DateFormat.getDateTimeInstance())
			}
		}
	}
	
	/**
	 * @param {String} [style] 'short', 'medium', 'long' or 'full'
	 * @param [locale] See {@link Savory.JVM#getLocale} for format
	 * @returns Savory.Localization.DateTimeFormat
	 */
	Public.getDateFormat = function(style, locale) {
		if (locale) {
			return new Public.DateTimeFormat(java.text.DateFormat.getDateInstance(dateTimeFormatStyles[style], Savory.JVM.toLocale(locale)))
		}
		else {
			if (style) {
				return new Public.DateTimeFormat(java.text.DateFormat.getDateInstance(dateTimeFormatStyles[style]))
			}
			else {
				return new Public.DateTimeFormat(java.text.DateFormat.getDateInstance())
			}
		}
	}

	/**
	 * @param {String} [style] 'short', 'medium', 'long' or 'full'
	 * @param [locale] See {@link Savory.JVM#getLocale} for format
	 * @returns Savory.Localization.DateTimeFormat
	 */
	Public.getTimeFormat = function(style, locale) {
		if (locale) {
			return new Public.DateTimeFormat(java.text.DateFormat.getTimeInstance(dateTimeFormatStyles[style], Savory.JVM.toLocale(locale)))
		}
		else {
			if (style) {
				return new Public.DateTimeFormat(java.text.DateFormat.getTimeInstance(dateTimeFormatStyles[style]))
			}
			else {
				return new Public.DateTimeFormat(java.text.DateFormat.getTimeInstance())
			}
		}
	}
	
	/**
	 * @param {String} code The currency code
	 * @param {String} [template='{symbol}{amount}'] Template to cast, filled with 'symbol' and 'amount' 
	 * @returns Savory.Localization.CurrencyFormat
	 */
	Public.getCurrencyFormat = function(code, template) {
		var currency = java.util.Currency.getInstance(code)
		return currency ? new Public.CurrencyFormat(currency, template) : null
	}

	/**
	 * @param locale See {@link Savory.JVM#getLocale} for format
	 * @param {String} [template='{symbol}{amount}'] Template to cast, filled with 'symbol' and 'amount' 
	 * @returns Savory.Localization.CurrencyFormat
	 */
	Public.getCurrencyFormatForLocale = function(locale, template) {
		var currency = java.util.Currency.getInstance(locale ? Savory.JVM.toLocale(locale) : java.util.Locale.getDefault())
		return currency ? new Public.CurrencyFormat(currency, template, locale) : null
	}

	/**
	 * Represents a date and/or time format based on a string pattern (JavaScript wrapper over java.text.SimpleDateFormat).
	 * <p> 
	 * Important: The JVM's SimpleDateFormat is not thread-safe, so make sure not to
	 * share instances between Prudence requests.
	 * 
	 * @class
	 * @name Savory.Localization.DateTimeFormat
	 * @param {String} format See <a href="http://download.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html">pattern rules</a>
	 */
	Public.DateTimeFormat = Savory.Classes.define(function() {
		/** @exports Public as Savory.Localization.DateTimeFormat */
	    var Public = {}
	    
	    /** @ignore */
	    Public._construct = function(format) {
			this.theFormat = Savory.Objects.isString(format) ? new java.text.SimpleDateFormat(format) : format
	    }

	    /**
		 * Parses a string into a date.
		 * 
		 * @param {String} string
		 * @param {String} [timeZone]
		 * @returns {Date}
		 */
	    Public.parse = function(string, timeZone) {
			if (timeZone) {
				this.theFormat.timeZone = java.util.TimeZone.getTimeZone(timeZone)
			}
			else {
				// TODO: reset?
			}
			return new Date(this.theFormat.parse(string).time)
		}

		/**
		 * Formats a date as a string.
		 * 
		 * @param {Date} date
		 * @param {String} [timeZone]
		 * @returns {String}
		 */
	    Public.format = function(date, timeZone) {
			if (timeZone) {
				this.theFormat.timeZone = java.util.TimeZone.getTimeZone(timeZone)
			}
			else {
				// TODO: reset?
			}
			return String(this.theFormat.format(date.getTime()))
		}
		
		return Public
	}())
	
	/**
	 * Represents a currency format based on a template.
	 * 
	 * @class
	 * @name Savory.Localization.CurrencyFormat
	 * 
	 * @param {Currency} currency
	 * @param {String} [template='{symbol}{amount}'] Template to cast, filled with 'symbol' and 'amount' 
	 * @param [locale] Default locale to use (see {@link Savory.JVM#getLocale} for format)
	 * 
	 * @see Savory.Localization#getCurrencyFormat
	 */
	Public.CurrencyFormat = Savory.Classes.define(function() {
		/** @exports Public as Savory.Localization.CurrencyFormat */
	    var Public = {}
	    
	    /** @ignore */
	    Public._construct = function(currency, template, locale) {
	    	this.currency = currency
			this.template = template || '{symbol}{amount}'
			this.digits = currency.defaultFractionDigits
			this.symbol = Savory.Objects.exists(locale) ? currency.getSymbol(Savory.JVM.toLocale(locale)) : currency.symbol
	    }
	    
		/**
		 * @param {Number} amount
		 * @param [locale] See {@link Savory.JVM#getLocale} for format
		 */
		Public.format = function(amount, locale) {
			var symbol = Savory.Objects.exists(locale) ? this.currency.getSymbol(Savory.JVM.toLocale(locale)) : this.symbol
			return this.template.cast({symbol: symbol, amount: amount.toFixed(this.digits)})
		}
		
		return Public
	}())
	
	//
	// Initialization
	//
	
	var dateTimeFormatStyles = {
		'short': java.text.DateFormat.SHORT,
		'medium': java.text.DateFormat.MEDIUM,
		'long': java.text.DateFormat.LONG,
		'full': java.text.DateFormat.FULL
	}

	var formatNumberRegExp = /(\d+)(\d{3})/

	return Public
}()

/**
 * Formats a date as a string.
 * 
 * @methodOf Date#
 * @see Savory.Localization#formatDateTime
 * @param {String|Savory.Localization.DateTimeFormat} format See <a href="http://download.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html">pattern rules</a>
 * @param {String} [timeZone]
 * @returns {String}
 */
Date.prototype.format = Date.prototype.format || function(format, timeZone) {
	return Savory.Localization.formatDateTime(this, format, timeZone)
}

/**
 * Parses a string into a date.
 * 
 * @methodOf String#
 * @see Savory.Localization#parseDateTime
 * @param {String|Savory.Localization.DateTimeFormat} format See <a href="http://download.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html">pattern rules</a>
 * @param {String} [timeZone]
 * @returns {Date}
 */
String.prototype.parseDateTime = String.prototype.parseDateTime || function(format, timeZone) {
	return Savory.Localization.parseDateTime(this, format, timeZone)
}
