
document.require('/sincerity/repl/')

importClass(
	com.threecrickets.sincerity.plugin.console.CommandCompleter,
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
	// Welcome
	command.sincerity.out.println('Sincerity jsconsole ' + command.sincerity.version.get('version'))
	var adapter = executable.context.adapter.attributes
	command.sincerity.out.println('JavaScript engine: ' + adapter.get('name') + ' ' + adapter.get('version'))
	try {
		command.sincerity.out.println('Container: ' + command.sincerity.container.root)
	}
	catch (x) {}

	// Logging
	try {
		command.sincerity.run(['logging:logging'])
	}
	catch (x) {
		// If logging is not configured, at least avoid annoying log messages to the console
		java.util.logging.Logger.getLogger('').level = java.util.logging.Level.WARNING
	}

	var JSConsole = Sincerity.Classes.define(function() {
	    var Public = {}
	    
	    Public._inherit = Sincerity.REPL
	
	    Public._construct = function() {
	    	try {
	    		arguments.callee.overridden.call(this, command.sincerity.container.getCacheFile(['shell', 'jsconsole.history']))
	    	}
	    	catch (x) {
	    		arguments.callee.overridden.call(this)
	    	}
	    }
	
	    Public.initialize = function() {
	    	arguments.callee.overridden.call(this)
	    	this.console.addCompleter(new CommandCompleter(':'))
	    }
	    
	    Public.finalize = function() {
	    	command.sincerity.out.println('Bye!')
	    }
	
	    Public.toJavaScript = function(line) {
	    	if (line[0] == ':') {
	    		line = Sincerity.Objects.trim(line.substring(1))
	    		return 'sincerity(\'' + Sincerity.Objects.escapeSingleQuotes(line) + '\')'
	    	}
	    	return line
	    }
	    
	    Public.evaluate = function(line) {
	    	return eval(line)
	    }
	
		return Public
	}())

	function sincerity(c) {
		c = Sincerity.Objects.trim(c)
		ClassUtil.main(command.sincerity, 'com.threecrickets.sincerity.Sincerity', c.split(' '))
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
