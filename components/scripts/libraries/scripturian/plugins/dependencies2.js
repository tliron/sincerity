
document.require(
	'/sincerity/dependencies/maven/',
	'/sincerity/objects/')

importClass(
	com.threecrickets.sincerity.plugin.console.CommandCompleter,
	com.threecrickets.sincerity.util.ClassUtil)

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['install2']
}

function run(command) {
	switch (String(command.name)) {
		case 'install2':
			test(command)
			break
	}
}

function test(command) {
	command.parse = true

	var sincerity = command.sincerity
	
	var d = [
		{group: 'org.jsoup', name: 'jsoup', version: '1.8.1'},
		{group: 'com.github.sommeri', name: 'less4j', version: '1.15.2'}
	]
	
	
	var repository = new Sincerity.Dependencies.Maven.Repository('file:/Depot/DevRepository/')
	var id = new Sincerity.Dependencies.Maven.ModuleIdentifier('org.jsoup', 'jsoup', '1.8.1')
	var id2 = new Sincerity.Dependencies.Maven.ModuleIdentifier('org.jsoup', 'jsoup', '1.8.1')
	var constraints = new Sincerity.Dependencies.Maven.ModuleConstraints('org.jsoup', 'jsoup', '1.8.1')
	var id3 = new Sincerity.Dependencies.Maven.ModuleIdentifier('com.github.sommeri:less4j:1.15.2')
	var resolver = new Sincerity.Dependencies.Resolver()

	resolver.rules = [
  		{type: 'rewriteVersion', group: 'com.beust', name: 'jcommander', newVersion: '1.35+'},
  		{type: 'rewriteGroupName'}
  	]

	// toString
	sincerity.out.println(repository.toString())
	sincerity.out.println(id.toString())
	sincerity.out.println(id.isEqual(id2))
	sincerity.out.println(id3.toString())
	sincerity.out.println(constraints.toString())
	sincerity.out.println(constraints.isSuitable(id))
	sincerity.out.println()

	// Versions
	sincerity.out.println('0=' +  Sincerity.Dependencies.compareVersions('', ''))
	sincerity.out.println('1=' +  Sincerity.Dependencies.compareVersions('2', ''))
	sincerity.out.println('1=' +  Sincerity.Dependencies.compareVersions('2', '1'))
	sincerity.out.println('-1=' + Sincerity.Dependencies.compareVersions('1', '2'))
	sincerity.out.println('1=' +  Sincerity.Dependencies.compareVersions('2.2', '2'))
	sincerity.out.println('1=' +  Sincerity.Dependencies.compareVersions('2.2', '2.1'))
	sincerity.out.println('1=' +  Sincerity.Dependencies.compareVersions('2.2', '2.2-b1'))
	sincerity.out.println('-1=' + Sincerity.Dependencies.compareVersions('2.2-b1', '2.2-b2'))
	sincerity.out.println('-1=' + Sincerity.Dependencies.compareVersions('2.2-alpha2', '2.2-beta1'))
	sincerity.out.println('1=' +  Sincerity.Dependencies.compareVersions('2.2-2', '2.2-1'))
	sincerity.out.println('0=' +  Sincerity.Dependencies.compareVersions('2.2-', '2.2'))
	sincerity.out.println()

	// URI
	sincerity.out.println('URI:')
	var uri = repository.getUri(id, 'pom')
	sincerity.out.println(uri)
	sincerity.out.println()
	
	// Fetch
	sincerity.out.println('Fetch:')
	repository.fetchModule(id, 'jsoup.jar')
	
	// POM
	sincerity.out.println('POM:')
	var pom = repository.getPom(id)
	sincerity.out.println(pom.getModuleIdentifier().toString())
	
	var pom = repository.getPom(id3)
	var dependencies = pom.getDependencyModuleConstraints()
	sincerity.out.println(pom.getModuleIdentifier().toString())
	for (var d in dependencies) {
		sincerity.out.println('| ' + dependencies[d].toString())
	}
	sincerity.out.println()
	
	// Metadata
	sincerity.out.println('MetaData:')
	var metadata = repository.getMetaData('com.github.sommeri', 'less4j')
	sincerity.out.println(metadata.getModuleIdentifier().toString())
	var metadataIds = metadata.getModuleIdentifiers()
	for (var i in metadataIds) {
		var metadataId = metadataIds[i]
		sincerity.out.println(metadataId.toString())
	}
	sincerity.out.println()
	
	// Resolve
	sincerity.out.println('Resolve:')
	var module = new Sincerity.Dependencies.Module()
	module.constraints = new Sincerity.Dependencies.Maven.ModuleConstraints('com.github.sommeri:less4j:1.15.2')
	resolver.resolveModule(module, [repository])
	module.dump(sincerity.out, true)
}
