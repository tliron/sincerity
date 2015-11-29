//
// This file is part of the Sincerity Foundation Library
//
// Copyright 2011-2015 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

document.require(
	'/sincerity/dependencies/',
	'/sincerity/jvm/')

var Sincerity = Sincerity || {}
Sincerity.Dependencies = Sincerity.Dependencies || {}

/**
 * Outputs events to the console, supporting advanced ANSI terminal features.
 * 
 * @namespace
 * 
 * @author Tal Liron
 */
Sincerity.Dependencies.Console = Sincerity.Dependencies.Console || function() {
	/** @exports Public as Sincerity.Dependencies.Console */
	var Public = {}

	/**
	 * A handler that outputs events to the console.
	 * 
	 * @class
	 * @name Sincerity.Dependencies.Console.EventHandler
	 */
	Public.EventHandler = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.Console.EventHandler */
	    var Public = {}

	    /** @ignore */
	    Public._inherit = Sincerity.Dependencies.EventHandler

	    /** @ignore */
	    Public._construct = function(out) {
	    	this.out = out
	    	this.lock = Sincerity.JVM.newLock()
	    	this.ongoingEvents = []
	    	this.progressLength = 10
	    	
	    	//this.ongoing.push({message: 'testing...'})
	    }

	    Public.handleEvent = function(event) {
			Sincerity.JVM.withLock(this.lock, function() {
				var event = this.event
				var handler = this.handler
				var out = handler.out
				var ongoingEvents = handler.ongoingEvents
				var ongoingLength = ongoingEvents.length
				var progressLength = handler.progressLength

				var terminal = Packages.jline.TerminalFactory.create()
				var terminalWidth = terminal.width
				terminal.reset()

				function print(event) {
					var output = ''
					if (Sincerity.Objects.exists(event.progress)) {
						output += '['
						for (var i = 0; i < progressLength; i++) {
							output += ((event.progress * progressLength) > i) ? '=' : ' '
								// TODO: spinner at end
						}
						output += '] '
					}
					if (Sincerity.Objects.exists(event.message)) {
						output += event.message
					}
					
					// We are making sure that we always advance one row only, even if we print a line longer than a row
		    		var length = output.length
		    		if (length >= terminalWidth) {
		    			out.print(output.substring(0, terminalWidth))
		    		}
		    		else {
		    			out.print(output)
		    			out.print(controlSequence('K'))
		    			out.println()
		    		}
				}
				
				// Move up before the ongoing block we printed last time
		    	if (ongoingLength) {
		    		out.print(controlSequence('' + ongoingLength + 'A'))
		    	}
		    	
				if (event.type == 'begin') {
			    	// Add ongoing event
					if (event.id) {
						ongoingEvents.push(event)
					}
				}
				else if (event.type == 'end') {
					// Remove ongoing event
			    	for (var o in ongoingEvents) {
			    		var ongoingEvent = ongoingEvents[o]
			    		if (event.id === ongoingEvent.id) {
			    			ongoingEvents.splice(o, 1)
			    			break
			    		}
			    	}
			    	// This line will take the place of the line we removed
					print(event)
				}
				else if (event.type == 'update') {
					// Update ongoing event
			    	for (var o in ongoingEvents) {
			    		var ongoingEvent = ongoingEvents[o]
			    		if (event.id === ongoingEvent.id) {
			    			Sincerity.Objects.merge(ongoingEvent, event)
			    			break
			    		}
			    	}
				}
				else {
					print(event)
		    	}
		    	
		    	// Print ongoing block after everything else
		    	for (var o in ongoingEvents) {
		    		var ongoingEvent = ongoingEvents[o]
					print(ongoingEvent)
		    	}
		    	out.print(controlSequence('J'))
			}, {event: event, handler: this})
	    	
	    	return false
	    }
	    
	    return Public
	}(Public))

	function controlSequence() {
		var out = ''
		for (var i in arguments) {
			out += '\x1b[' // ANSI CSI (Control Sequence Introducer)
			out += arguments[i]
		}
		return out
	}

	return Public
}()
