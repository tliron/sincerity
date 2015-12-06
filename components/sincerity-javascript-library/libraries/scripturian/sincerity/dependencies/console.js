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

	    	this.endGraphics = '32'
		    this.failGraphics = '33'
	    	this.errorGraphics = '31'
	    	this.ongoingGraphics = '34'

	    	this.progressLength = 16
	    	this.progressStart = '['
		    this.progressEnd = '] '
		    this.progressDone = '='
		    this.progressTodo = ' '
	    }

	    Public.handleEvent = function(event) {
			var terminal = Packages.jline.TerminalFactory.create()
			try {
				this.ansi = terminal.ansiSupported
				this.terminalWidth = terminal.width
			}
			finally {
				terminal.reset()
			}
			
			// Move up before the ongoing block we printed last time
	    	if (this.ongoingEvents.length) {
	    		this.controlSequence('' + this.ongoingEvents.length + 'A') // move cursor up
	    	}
	    	
			if (event.type == 'begin') {
		    	// Add ongoing event
				if (event.id) {
					this.ongoingEvents.push(event)
				}
			}
			else if ((event.type == 'end') || (event.type == 'fail')) {
				// Remove ongoing event
		    	for (var o in this.ongoingEvents) {
		    		var ongoingEvent = this.ongoingEvents[o]
		    		if (event.id === ongoingEvent.id) {
		    			this.ongoingEvents.splice(o, 1)
		    			break
		    		}
		    	}
		    	// This line will take the place of the line we removed
		    	this.controlSequence((event.type == 'fail' ? this.failGraphics : this.endGraphics) + 'm')
		    	this.print(event)
			}
			else if (event.type == 'update') {
				// Update ongoing event
		    	for (var o in this.ongoingEvents) {
		    		var ongoingEvent = this.ongoingEvents[o]
		    		if (event.id === ongoingEvent.id) {
		    			Sincerity.Objects.merge(ongoingEvent, event)
		    			break
		    		}
		    	}
			}
			else if (event.type == 'error') {
		    	this.controlSequence(this.errorGraphics + 'm')
				this.print(event)
			}
			else {
				this.print(event)
	    	}
	    	
	    	// Print ongoing block after everything else
	    	for (var o in this.ongoingEvents) {
	    		var ongoingEvent = this.ongoingEvents[o]
		    	this.controlSequence(this.ongoingGraphics + 'm')
	    		this.print(ongoingEvent)
	    	}
	    	
	    	this.controlSequence('0J') // erase to end of screen
	    	
	    	return false
	    }.withLock('lock')

		Public.controlSequence = function() {
			for (var i in arguments) {
				this.out.print('\x1b[') // ANSI CSI (Control Sequence Introducer)
				this.out.print(arguments[i])
			}
		}

		Public.print = function(event) {
			var output = ''
				
			if (Sincerity.Objects.exists(event.progress)) {
				output += this.progressStart
				for (var i = 0; i < this.progressLength; i++) {
					output += ((event.progress * this.progressLength) > i) ? this.progressDone : this.progressTodo
					// TODO: spinner at end
				}
				output += this.progressEnd
			}
			
			if (Sincerity.Objects.exists(event.message)) {
				output += event.message
			}

			// We are making sure that we always advance one row only, even if we print a line longer than a row
    		var length = output.length
    		if (length >= this.terminalWidth) {
    			// Will automatically advance to the next line
    			this.out.print(output.substring(0, this.terminalWidth))
    		}
    		else {
    			this.out.print(output)
    			this.controlSequence('0m', 'K') // reset graphics and erase to end of line
    			this.out.println()
    		}
		}

	    return Public
	}(Public))

	return Public
}()
