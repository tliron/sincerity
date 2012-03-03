
document.execute('/sincerity/objects/')
document.execute('/sincerity/jvm/')

importClass(
	java.lang.ClassNotFoundException,
	com.threecrickets.sincerity.exception.CommandException,
	com.threecrickets.sincerity.exception.BadArgumentsCommandException)

var WIKITEXT_VERSION = '1.5.0'

var languageNames = {
	confluence: 'Confluence',
	mediawiki: 'MediaWiki',
	twiki: 'TWiki',
	trac: 'TracWiki',
	textile: 'Textile',
	bugzilla: 'Textile Bugzilla Dialect'
}

function getCommands() {
	return ['render']
}

function run(command) {
	switch (String(command.name)) {
		case 'render':
			render(command)
			break
	}
}

function render(command) {
	if (command.arguments.length < 1) {
		throw new BadArgumentsCommandException(command, 'language')
	}

	var name = command.arguments[0]
	
	var fullName = languageNames[name.toLowerCase()]
	if (!Sincerity.Objects.exists(fullName)) {
		throw new CommandException(command, 'Unsupported markup language: ' + name)
	}
	
	var language = getLanguage(fullName)

	if (!Sincerity.Objects.exists(language)) {
		// Install the relevant dependency and try again
		command.sincerity.run('dependencies:add', ['org.eclipse.mylyn', 'wikitext-' + name, WIKITEXT_VERSION])
		command.sincerity.run('dependencies:install')
		language = getLanguage(fullName)
	}

	if (!Sincerity.Objects.exists(language)) {
		throw new CommandException(command, 'Could not load markup language implementation: ' + fullName)
	}

	var parser = new org.eclipse.mylyn.wikitext.core.parser.MarkupParser(language) 
	var writer = new java.io.StringWriter()
	var builder = new org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder(writer)
	parser.builder = builder
	parser.parse('hi', true)
	var rendered = String(writer.toString())
	println(rendered)
}

function getLanguage(name) {
	if (Sincerity.Objects.exists(Sincerity.JVM.getClass('org.eclipse.mylyn.wikitext.core.util.ServiceLocator'))) {
		var serviceLocator = org.eclipse.mylyn.wikitext.core.util.ServiceLocator.instance
		return serviceLocator.getMarkupLanguage(name)
	}
	return null
}
