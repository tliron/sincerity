
document.require(
	'/sincerity/objects/')

importClass(
	com.threecrickets.creel.Manager,
	com.threecrickets.creel.event.ConsoleEventHandler)

	
function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['creel']
}

function run(command) {
	switch (String(command.name)) {
		case 'creel':
			creel(command)
			break
	}
}

function creel(command) {
	command.parse = true

	var sincerity = command.sincerity
	
	var manager = new Manager()
	manager.eventHandler.add(new ConsoleEventHandler(sincerity.out, sincerity.terminalAnsi))

	var local = true

	var modules = [
   		{group: 'com.github.sommeri', name: 'less4j', version: '(,1.15.2)'},
		{group: 'org.jsoup', name: 'jsoup', version: '1.8.1'},
		{group: 'com.fasterxml.jackson', name: 'jackson'},
		{group: 'com.threecrickets.prudence', name: 'prudence'},
		{group: 'jsslutils', name: 'jsslutils'} // only in restlet
	]
   	
   	var repositories = [
   		local ? {id: '3c', url: 'file:/Depot/DevRepository/'} : {id: '3c', url: 'http://repository.threecrickets.com/maven'},
   		{id: 'restlet', url: 'http://maven.restlet.com', all: false},
   		{id: 'central', url: 'https://repo1.maven.org/maven2/'}
   	]

	var rules = [
  		{type: 'exclude', name: '*annotations*'},
 		{type: 'excludeDependencies', group: 'org.apache.commons', name: 'commons-beanutils'},
		//{type: 'rewrite'},
   		{type: 'rewriteVersion', group: 'com.beust', name: '*c?mmand*', newVersion: '1.35+'},
   		//{type: 'repositories', name: 'less4j', repositories: ['3c']},
   		{type: 'repositories', group: 'jsslutils', repositories: ['restlet']}
   	]

	manager.setExplicitModules(modules)
	manager.setRepositories(repositories)
	
	manager.identify()
	manager.install('zzz/jars', true, false)
}
