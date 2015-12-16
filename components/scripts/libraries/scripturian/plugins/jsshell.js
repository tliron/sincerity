
document.require(
	'/sincerity/repl/',
	'/sincerity/files/',
	'/sincerity/objects/')

importClass(
	com.threecrickets.sincerity.console.CommandCompleter,
	com.threecrickets.sincerity.util.ClassUtil)

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['jsconsole']
}

function run(command) {
	switch (String(command.name)) {
		case 'jsconsole':
			jsconsole(command)
			break
	}
}

function jsconsole(command) {
	command.parse = true

	var sincerity = command.sincerity
	
	// Logging
	try {
		sincerity.run(['logging:logging'])
	}
	catch (x) {
		// If logging is not configured, at least avoid annoying log messages to the console
		java.util.logging.Logger.getLogger('').level = java.util.logging.Level.WARNING
	}

	// Script parameter?
	var script = command.properties.get('script')
	if (Sincerity.Objects.exists(script)) {
		script = Sincerity.Files.loadText(script)
		eval(String(script))
		return
	}

	var JSConsole = Sincerity.Classes.define(function() {
	    var Public = {}
	    
	    Public._inherit = Sincerity.REPL
	
	    Public._construct = function() {
	    	try {
	    		arguments.callee.overridden.call(this, sincerity.container.getCacheFile(['jsshell', 'jsconsole.history']))
	    	}
	    	catch (x) {
	    		arguments.callee.overridden.call(this)
	    	}
	    	this.showStackTrace = sincerity.verbosity > 1
	    }
	
	    Public.initialize = function() {
	    	arguments.callee.overridden.call(this)
	    	this.console.addCompleter(new CommandCompleter(':'))

	    	// Welcome
	    	this.controlSequence(this.defaultGraphics + 'm')
	    	this.out.println('Sincerity jsconsole ' + sincerity.version.get('version'))
	    	var adapter = executable.context.adapter.attributes
	    	this.out.println('JavaScript engine: ' + adapter.get('name') + ' ' + adapter.get('version'))
	    	try {
	    		this.out.println('Container: ' + sincerity.container.root)
	    	}
	    	catch (x) {}
	    	this.controlSequence('0m')
	    }
	    
	    Public.finalize = function() {
	    	this.controlSequence(this.defaultGraphics + 'm')
	    	this.out.println('Bye!')
	    	this.controlSequence('0m')
	    }
	
	    Public.toJavaScript = function(line) {
	    	if (line[0] == ':') {
	    		line = Sincerity.Objects.trim(line.substring(1))
	    		return 'doSincerity(\'' + Sincerity.Objects.escapeSingleQuotes(line) + '\')'
	    	}
	    	return line
	    }
	    
	    Public.evaluate = function(line) {
	    	return eval(line)
	    }
	
		return Public
	}())

	function doSincerity(c) {
		c = Sincerity.Objects.trim(c)
		ClassUtil.main(sincerity, 'com.threecrickets.sincerity.Sincerity', c.split(' '))
	}

	function exit() {
		repl.exit()
	}
	
	function show(o, indent) {
		repl.show(o, indent)
	}

	function reset() {
		repl.reset()
	}

	var repl = new JSConsole()
	repl.run()
}
