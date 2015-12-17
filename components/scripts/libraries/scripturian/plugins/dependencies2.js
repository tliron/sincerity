
document.require(
	'/sincerity/dependencies/maven/',
	'/sincerity/dependencies/console/',
	'/sincerity/objects/')

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['install2', 'test2']
}

function run(command) {
	switch (String(command.name)) {
	case 'install2':
			install2(command)
			break
		case 'test2':
			test2(command)
			break
	}
}

function install2(command) {
	command.parse = true

	var sincerity = command.sincerity
	
	Sincerity.Dependencies.registerHooks(sincerity.out)
	
	var local = false
	
	var modules = [
		{group: 'com.github.sommeri', name: 'less4j', version: '(,1.15.2)'},
 		{group: 'org.jsoup', name: 'jsoup', version: '1.8.1'},
 		{group: 'com.fasterxml.jackson', name: 'jackson'},
 		{group: 'com.threecrickets.prudence', name: 'prudence'},
 		{group: 'jsslutils', name: 'jsslutils'} // only in restlet
 	]
	
	var repositories = [
		local ? {id: '3c', uri: 'file:/Depot/DevRepository/'} : {id: '3c', uri: 'http://repository.threecrickets.com/maven'},
		{id: 'restlet', uri: 'http://maven.restlet.com', all: false},
		{id: 'central', uri: 'https://repo1.maven.org/maven2/'}
	]
	
	var rules = [
 		{type: 'exclude', name: '*annotations*'},
		{type: 'excludeDependencies', group: 'org.apache.commons', name: 'commons-beanutils'},
   		//{type: 'rewrite'},
  		{type: 'rewriteVersion', group: 'com.beust', name: '*c?mmand*', newVersion: '1.35+'},
  		//{type: 'repositories', name: 'less4j', repositories: ['3c']},
  		{type: 'repositories', group: 'jsslutils', repositories: ['restlet']}
  	]

	var manager = new Sincerity.Dependencies.Manager({
		modules: modules,
		repositories: repositories,
		rules: rules,
		conflictPolicy: 'newest' //'oldest'
	})
	
	manager.eventHandler.add(new Sincerity.Dependencies.Console.EventHandler(sincerity))
	manager.eventHandler.add(new Sincerity.Dependencies.LogEventHandler())

	manager.identify()
	manager.install('zzz/libraries/jars', true)
	
	sincerity.out.println('Cache hits: ' + manager.identifiedCacheHits.get())
	sincerity.out.println('Identified: (' + manager.identifiedModules.length + ')')
	for (var m in manager.identifiedModules) {
		manager.identifiedModules[m].dump(sincerity.out, false, 1)
	}
	sincerity.out.println('Unidentified: (' + manager.unidentifiedModules.length + ')')
	for (var m in manager.unidentifiedModules) {
		manager.unidentifiedModules[m].dump(sincerity.out, false, 1)
	}
	sincerity.out.println('Conflicts: (' + manager.conflicts.length + ')')
	for (var c in manager.conflicts) {
		var conflict = manager.conflicts[c]
		for (var m in conflict) {
			var module = conflict[m]
			module.dump(sincerity.out, false, 1)
		}
	}
	sincerity.out.println('Tree:')
	for (var m in manager.explicitModules) {
		manager.explicitModules[m].dump(sincerity.out, true, 1)
	}
}

function test2(command) {
	command.parse = true

	var sincerity = command.sincerity

	Sincerity.Dependencies.registerHooks(sincerity.out)

	/*var f = function(msg) {
		this.out.println('My ' + msg)
	}.toThread('hi', sincerity, 'thread2')
	f.start()*/
	
	var repository = new Sincerity.Dependencies.Maven.Repository({uri: 'file:/Depot/DevRepository/'})
	var id = new Sincerity.Dependencies.Maven.ModuleIdentifier('org.jsoup', 'jsoup', '1.8.1')
	var id2 = new Sincerity.Dependencies.Maven.ModuleIdentifier('org.jsoup', 'jsoup', '1.8.1')
	var specification = new Sincerity.Dependencies.Maven.ModuleSpecification('org.jsoup', 'jsoup', '1.8.1')
	var id3 = new Sincerity.Dependencies.Maven.ModuleIdentifier('com.github.sommeri:less4j:1.15.2')
	
	// ForkJoin
	sincerity.out.println('forkJoin:')
	var pool = new java.util.concurrent.ForkJoinPool()
	
	pool.invoke(function() {
		sincerity.out.println('In task!')
	}.toTask('recursiveAction'))
	
	function sumTask(arr, lo, hi) {
		// Sums in chunks of 1000
		// See: http://homes.cs.washington.edu/~djg/teachingMaterials/spac/grossmanSPAC_forkJoinFramework.html
		return function() {
			if (hi - lo <= 1000) {
				var sum = 0
				for (var i = lo; i < hi; i++) {
					sum += arr[i]
				}
				return {value: sum} // returning dicts to avoid boxing by JavaScript engine
			}
			else {
				var mid = lo + Math.floor((hi - lo) / 2)
				var left = sumTask(arr, lo, mid)
				var right = sumTask(arr, mid, hi)
				left.fork()
				right = right.compute().value
				left = left.join().value
				return {value: left + right}
			}
		}.toTask('recursiveTask')
	}
	
	var arr = []
	for (var i = 0; i < 1000000; i++) {
		arr[i] = i
	}
	
	sincerity.out.println(pool.invoke(sumTask(arr, 0, arr.length)).value)
	sincerity.out.println()
	
	pool.shutdownNow()
	
	// glob
	sincerity.out.println('Glob:')
	sincerity.out.println('true=' + 'This is the ? text'.glob())
	sincerity.out.println('true=' + 'This is the ? text'.glob(''))
	sincerity.out.println('true=' + 'This is the ? text'.glob('*'))
	sincerity.out.println('true=' + 'This is the ? text'.glob('This*'))
	sincerity.out.println('true=' + 'This is the ? text'.glob('*the ? text*'))
	sincerity.out.println('true=' + 'This is the ? text'.glob('*the \\? text*'))
	sincerity.out.println('true=' + 'This is the ! text'.glob('*the ? text*'))
	sincerity.out.println('false=' + 'This is the ! text'.glob('*the \\? text*'))
	sincerity.out.println()
	
	// toString
	sincerity.out.println(repository.toString())
	sincerity.out.println(id.toString())
	sincerity.out.println(id.compare(id2))
	sincerity.out.println(id3.toString())
	sincerity.out.println(specification.toString())
	sincerity.out.println(specification.allowsModuleIdentifier(id))
	sincerity.out.println()
	
	// Versions
	sincerity.out.println('Versions:')
	sincerity.out.println('0=' +  Sincerity.Dependencies.Maven.Versions.compare('', ''))
	sincerity.out.println('1=' +  Sincerity.Dependencies.Maven.Versions.compare('2', ''))
	sincerity.out.println('1=' +  Sincerity.Dependencies.Maven.Versions.compare('2', '1'))
	sincerity.out.println('-1=' + Sincerity.Dependencies.Maven.Versions.compare('1', '2'))
	sincerity.out.println('1=' +  Sincerity.Dependencies.Maven.Versions.compare('2.2', '2'))
	sincerity.out.println('1=' +  Sincerity.Dependencies.Maven.Versions.compare('2.2', '2.1'))
	sincerity.out.println('1=' +  Sincerity.Dependencies.Maven.Versions.compare('2.2', '2.2-b1'))
	sincerity.out.println('-1=' + Sincerity.Dependencies.Maven.Versions.compare('2.2-b1', '2.2-b2'))
	sincerity.out.println('-1=' + Sincerity.Dependencies.Maven.Versions.compare('2.2-alpha2', '2.2-beta1'))
	sincerity.out.println('1=' +  Sincerity.Dependencies.Maven.Versions.compare('2.2-2', '2.2-1'))
	sincerity.out.println('0=' +  Sincerity.Dependencies.Maven.Versions.compare('2.2-', '2.2'))
	sincerity.out.println()
	
	// URI
	sincerity.out.println('URI:')
	var uri = repository.getUri(id, 'pom')
	sincerity.out.println(uri)
	sincerity.out.println()
	
	// Install
	sincerity.out.println('Install:')
	repository.installModule(id, 'zzz/libraries/jars', false)
	
	// POM
	sincerity.out.println('POM:')
	var pom = repository.getPom(id)
	sincerity.out.println(pom.moduleIdentifier.toString())
	
	var pom = repository.getPom(id3)
	sincerity.out.println(pom.moduleIdentifier.toString())
	for (var d in pom.dependencyModuleSpecifications) {
		sincerity.out.println('| ' + pom.dependencyModuleSpecifications[d].toString())
	}
	sincerity.out.println()
	
	// Metadata
	sincerity.out.println('MetaData:')
	var metadata = repository.getMetaData('com.github.sommeri', 'less4j')
	sincerity.out.println(metadata.moduleIdentifier.toString())
	for (var i in metadata.moduleIdentifiers) {
		var id = metadata.moduleIdentifiers[i]
		sincerity.out.println(id.toString())
	}
	sincerity.out.println()
	
	// Signatures
	sincerity.out.println('Signatures:')
	var signature = repository.getSignature(uri, 'pom')
	if (signature) {
		sincerity.out.println(signature.type + ':' + signature.content)
	}
	sincerity.out.println()
}
