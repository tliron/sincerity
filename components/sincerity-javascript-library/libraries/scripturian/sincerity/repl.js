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
	'/sincerity/classes/',
	'/sincerity/json/',
	'/sincerity/objects/')

var Sincerity = Sincerity || {}

/**
 * Utilities to create JavaScript REPLs (read-evaluate-print-loops) using <a href="https://github.com/jline/jline2">JLine</a>.
 * 
 * @class
 * 
 * @author Tal Liron
 * @version 1.0
 */
Sincerity.REPL = Sincerity.REPL || Sincerity.Classes.define(function() {
	/** @exports Public as Sincerity.REPL */
    var Public = {}
    
    /** @ignore */
    Public._construct = function(historyFile) {
    	repl = this
		this.out = null
		this.showIndent = false
		this.showStackTrace = false
		this.history = null

		if (Sincerity.Objects.exists(historyFile)) {
			if (!(historyFile instanceof java.io.File)) {
				historyFile = new java.io.File(String(historyFile))
			}
			this.history = new Packages.jline.console.history.FileHistory(historyFile)
		}
    }

	Public.initialize = function() {
		this.console.prompt = '> '
	}
	
	Public.finalize = function() {
	}
	
	Public.toJavaScript = function(line) {
		return line
	}
	
	Public.evaluate = function(line) {
		return eval(line)
	}
    
	Public.run = function() {
		var jline = Packages.jline
		importClass(
			jline.TerminalFactory,
			jline.console.ConsoleReader,
			jline.console.UserInterruptException)
		
		this.terminal = TerminalFactory.create()
		this.console = new ConsoleReader()

		this.console.handleUserInterrupt = true
		this.console.copyPasteDetection = true
		this.console.expandEvents = false
		if (Sincerity.Objects.exists(this.history)) {
			this.console.history = this.history
		}

		this.out = this.console
		
		this.initialize()

		this.isExiting = false
		while (!this.isExiting) {
			try {
				var line = String(this.console.readLine())
				if (Sincerity.Objects.exists(this.history)) {
					try {
						this.history.flush()
					}
					catch (x) {}
				}
				line = this.toJavaScript(line)
				
				var r = this.evaluate(line)
				
				var type = null
				try {
					type = typeof r
				}
				catch (x) {}

				if (type == 'function') {
					// Call all functions (they are commands)
					r()
				}
				else {
					this.show(r)
				}
			}
			catch (x) {
				if ((x instanceof UserInterruptException) || (x.javaException instanceof UserInterruptException)) {
					this.exit()
				}
				else {
					this.onError(x)
				}
			}
		}

		this.terminal.restore()
		this.finalize()
	}

    Public.exit = function() {
    	this.isExiting = true
    }
	
	Public.onError = function(x) {
		if (x.javaException) {
			// Unwrap in Rhino
			x = x.javaException
		}
		if (x instanceof java.lang.Throwable) {
			this.out.println('JVM error:')
			if (this.showStackTrace) {
				var out = new java.io.StringWriter()
				x.printStackTrace(new java.io.PrintWriter(out))
				this.out.println(String(out))
			}
			else {
				this.out.println(x.message)
			}
		}
		else if (x.nashornException) {
			this.out.println('JavaScript error:')
			if (this.showStackTrace) {
				var out = new java.io.StringWriter()
				x.nashornException.printStackTrace(new java.io.PrintWriter(out))
				this.out.println(String(out))
			}
			else {
				this.out.println(x.nashornException.message)
			}
		}
		else {
			this.out.println("Error:")
			this.out.println(String(x))
		}
	}
	
	Public.show = function(o, indent) {
		var type = null
		try {
			type = typeof o
		}
		catch (x) {}
		
		if (Sincerity.Objects.isString(o) || (type == 'boolean') || (type == 'number') || (null === o)) {
			// Print all primitives
			this.out.println(String(o))
		}
		else if (Sincerity.Objects.exists(o)) {
			if (!Sincerity.Objects.exists(indent)) {
				indent = this.showIndent
			}
			var out = String(Sincerity.JSON.to(o, indent))
			if (out != 'null') {
				this.out.println(out)
			}
			else {
				this.out.println(String(o))
			}
		}
	}
	
	Public.reset = function() {
		if (Sincerity.Objects.exists(this.history)) {
			try {
				this.history.purge()
				this.out.println('History reset!')
			}
			catch (x) {}
		}
	}
	
	// Private
	
	var repl
	
	function exit() {
		repl.exit()
	}
	
	function show(o, indent) {
		repl.show(o, indent)
	}
	
	function reset() {
		repl.reset()
	}
	
	return Public
}())
