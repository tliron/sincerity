
document.execute('/sincerity/objects/')
document.execute('/sincerity/files/')
document.execute('/sincerity/jvm/')
document.execute('/sincerity/container/')

importClass(
	com.threecrickets.sincerity.exception.CommandException,
	com.threecrickets.sincerity.exception.BadArgumentsCommandException)

var pegdownVersion = '1.1.0'
var wikiTextVersion = '1.5.0'

var languageNames = {
	confluence: 'Confluence',
	mediawiki: 'MediaWiki',
	twiki: 'TWiki',
	tracwiki: 'TracWiki',
	textile: 'Textile',
	bugzilla: 'Textile Bugzilla Dialect'
}

function getInterfaceVersion() {
	return 1
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
	command.parse = true
	if (command.arguments.length < 3) {
		throw new BadArgumentsCommandException(command, 'language', 'marked up source path', 'rendered output path')
	}

	var name = command.arguments[0]
	var sourceFile = command.arguments[1]
	var renderedFile = command.arguments[2]
	var complete = command.properties.get('complete') != 'false'
		
	if (name.toLowerCase() == 'markdown') {
		renderMarkdown(command, sourceFile, renderedFile)
	}
	else {
		renderWikiText(command, sourceFile, renderedFile, name, complete)
	}
}

function renderMarkdown(command, sourceFile, renderedFile) {
	if (null == Sincerity.Container.ensureClass('org.pegdown.PegDownProcessor', ['org.pegdown', 'pegdown', pegdownVersion])) {
		return
	}
	
	var parser = new org.pegdown.PegDownProcessor()
	var source = Sincerity.Files.loadText(sourceFile).array()
	var rendered = parser.markdownToHtml(source)
	write(rendered, renderedFile)
}

function renderWikiText(command, sourceFile, renderedFile, name, complete) {
	var fullName = languageNames[name.toLowerCase()]
	if (!Sincerity.Objects.exists(fullName)) {
		throw new CommandException(command, 'Unsupported markup language: ' + name)
	}
	
	var language = getLanguage(fullName)

	if (!Sincerity.Objects.exists(language)) {
		// Install the relevant dependency and try again
		command.sincerity.run(['dependencies:add', 'org.eclipse.mylyn', 'wikitext-' + name, wikiTextVersion])
		command.sincerity.run(['artifacts:install'])
		return
	}

	if (!Sincerity.Objects.exists(language)) {
		throw new CommandException(command, 'Could not load markup language implementation: ' + fullName)
	}
	
	var source = Sincerity.Files.loadText(sourceFile)

	var parser = new org.eclipse.mylyn.wikitext.core.parser.MarkupParser(language) 
	var writer = new java.io.StringWriter()
	var builder = new org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder(writer)
	parser.builder = builder
	parser.parse(source, complete)
	var rendered = writer.toString()
	write(rendered, renderedFile)
}

function getLanguage(name) {
	if (Sincerity.Objects.exists(Sincerity.JVM.getClass('org.eclipse.mylyn.wikitext.core.util.ServiceLocator'))) {
		var serviceLocator = org.eclipse.mylyn.wikitext.core.util.ServiceLocator.instance
		return serviceLocator.getMarkupLanguage(name)
	}
	return null
}

function write(rendered, renderedFile) {
	var writer = Sincerity.Files.openForTextWriting(renderedFile)
	try {
		writer.write(rendered)
	}
	finally {
		writer.close()
	}
}