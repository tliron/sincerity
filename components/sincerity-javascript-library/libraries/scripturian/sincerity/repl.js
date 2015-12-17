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
	'/sincerity/jvm/',
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

		this.ansi = sincerity.terminalAnsi
		this.defaultGraphics = '34'
		this.promptGraphics = '32'
		this.errorGraphics = '31'

		if (Sincerity.Objects.exists(historyFile)) {
			if (!(historyFile instanceof java.io.File)) {
				historyFile = new java.io.File(String(historyFile))
			}
			this.history = new Packages.jline.console.history.FileHistory(historyFile)
		}
    }

	Public.initialize = function() {
		this.console.prompt = this.ansi ? 
			CSI + this.promptGraphics + 'm' + '>' + CSI + '0m' + ' ' :
			'> '
	}
	
	Public.finalize = function() {
	}
	
	Public.toJavaScript = function(line) {
		return line
	}

	Public.controlSequence = function() {
    	if (!this.ansi) {
    		return
    	}
		for (var i in arguments) {
			this.out.print(CSI)
			this.out.print(arguments[i])
		}
	}

	Public.evaluate = function(line) {
		return eval(line)
	}
    
	Public.run = function() {
		this.console = new Packages.jline.console.ConsoleReader()
		try {
			this.console.handleUserInterrupt = true
			this.console.copyPasteDetection = true
			this.console.expandEvents = false
			if (Sincerity.Objects.exists(this.history)) {
				this.console.history = this.history
			}
	
			this.out = new java.io.PrintWriter(this.console.output, true)
			
			this.initialize()
	
			this.isExiting = false
			while (!this.isExiting) {
				try {
					var line = this.console.readLine()
					if (!Sincerity.Objects.exists(line)) {
						break
					}
					if (Sincerity.Objects.exists(this.history)) {
						try {
							this.history.flush()
						}
						catch (x) {}
					}
					line = this.toJavaScript(String(line))
					
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
					if (Sincerity.JVM.isException(x, Packages.jline.console.UserInterruptException)) {
						this.exit()
					}
					else {
						this.onError(x)
					}
				}
			}
		}
		finally {
			this.finalize()
			if (Sincerity.Objects.exists(this.out)) {
				this.out.flush()
			}
			if (Sincerity.Objects.exists(this.console)) {
				this.console.terminal.restore()
			}
		}
	}

    Public.exit = function() {
    	this.isExiting = true
    }
	
	Public.onError = function(x) {
		this.controlSequence(this.errorGraphics + 'm')
		if (Sincerity.JVM.isException(x, java.lang.Throwable)) {
			this.out.println('JVM error')
			if (this.showStackTrace) {
				x.printStackTrace(out)
			}
			else {
				this.out.println(x.message)
			}
		}
		else if (Sincerity.Objects.exists(x.nashornException)) {
			this.out.println('JavaScript error')
			if (this.showStackTrace) {
				x.nashornException.printStackTrace(out)
			}
			else {
				this.out.println(x.nashornException.message)
			}
		}
		else {
			this.out.println('JavaScript error')
			this.out.println(String(x))
		}
		this.controlSequence('0m')
	}
	
	Public.show = function(o, indent) {
		if (!Sincerity.Objects.exists(o)) {
			return
		}
		
		var type = null
		try {
			type = typeof o
		}
		catch (x) {}

		this.controlSequence(this.defaultGraphics + 'm')
		if (Sincerity.Objects.isString(o) || (type == 'boolean') || (type == 'number') || (null === o)) {
			// Print all primitives
			this.out.println(String(o))
		}
		else {
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
		this.controlSequence('0m')
	}
	
	Public.reset = function() {
		if (Sincerity.Objects.exists(this.history)) {
			try {
				this.history.purge()
				this.controlSequence(this.defaultGraphics + 'm')
				this.out.println('History reset!')
				this.controlSequence('0m')
			}
			catch (x) {}
		}
	}

	// Private

    var CSI = '\x1b[' // ANSI CSI (Control Sequence Introducer)

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
