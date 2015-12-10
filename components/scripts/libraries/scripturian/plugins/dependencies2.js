
document.require(
	'/sincerity/dependencies/maven/',
	'/sincerity/dependencies/console/',
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
	
	Sincerity.JVM.addShutdownHook(function() {
		this.out.println('Goodbye...')
	}, 'shutdown', sincerity)
	
	var f = function(msg) {
		this.out.println('My ' + msg)
	}.thread('hi', sincerity, 'thread2')
	f.start()

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
	}.task('recursiveAction'))
	
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
		}.task('recursiveTask')
	}
	
	var arr = []
	for (var i = 0; i < 1000000; i++) {
		arr[i] = i
	}
	
	sincerity.out.println(pool.invoke(sumTask(arr, 0, arr.length)).value)
	sincerity.out.println()
	
	// matchSimple
	sincerity.out.println('matchSimple:')
	sincerity.out.println('true=' + 'This is the ? text'.matchSimple())
	sincerity.out.println('true=' + 'This is the ? text'.matchSimple(''))
	sincerity.out.println('true=' + 'This is the ? text'.matchSimple('*'))
	sincerity.out.println('true=' + 'This is the ? text'.matchSimple('This*'))
	sincerity.out.println('true=' + 'This is the ? text'.matchSimple('*the ? text*'))
	sincerity.out.println('true=' + 'This is the ? text'.matchSimple('*the \\? text*'))
	sincerity.out.println('true=' + 'This is the ! text'.matchSimple('*the ? text*'))
	sincerity.out.println('false=' + 'This is the ! text'.matchSimple('*the \\? text*'))
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
	
	// Fetch
	sincerity.out.println('Fetch:')
	repository.fetchModule(id, 'zzz', false, {eventHandler: new Sincerity.Dependencies.Console.EventHandler(sincerity.out)})
	
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
	
	// Resolve
	sincerity.out.println('Resolve:')

	var modules = [
		{group: 'com.github.sommeri', name: 'less4j', version: '(,1.15.2)'},
 		{group: 'org.jsoup', name: 'jsoup', version: '1.8.1'},
 		{group: 'com.fasterxml.jackson', name: 'jackson'},
 		{group: 'com.threecrickets.prudence', name: 'prudence'}
 	]
	
	var repositories = [
		//{uri: 'file:/Depot/DevRepository/'}
		{uri: 'http://repository.threecrickets.com/maven'}
	]
	
	var rules = [
 		{type: 'exclude', name: '*annotations*'},
		{type: 'excludeDependencies', group: 'org.apache.commons', name: 'commons-beanutils'},
   		//{type: 'rewrite'},
  		{type: 'rewriteVersion', group: 'com.beust', name: '*c?mmand*', newVersion: '1.35+'}
  	]

	var resolver = new Sincerity.Dependencies.Resolver({
		modules: modules,
		repositories: repositories,
		rules: rules,
		conflictPolicy: 'oldest'
	})
	
	resolver.eventHandler.add(new Sincerity.Dependencies.Console.EventHandler(sincerity.out))
	resolver.eventHandler.add(new Sincerity.Dependencies.LogEventHandler())

	resolver.resolve()
	sincerity.out.println('Tree:')
	for (var m in resolver.explicitModules) {
		resolver.explicitModules[m].dump(sincerity.out, true, 1)
	}
	sincerity.out.println('Resolved: (' + resolver.resolvedModules.length + ')')
	for (var m in resolver.resolvedModules) {
		resolver.resolvedModules[m].dump(sincerity.out, false, 1)
	}
	sincerity.out.println('resolvedCacheHits: ' + resolver.resolvedCacheHits.get())
	sincerity.out.println('Unresolved: (' + resolver.unresolvedModules.length + ')')
	for (var m in resolver.unresolvedModules) {
		resolver.unresolvedModules[m].dump(sincerity.out, false, 1)
	}
	sincerity.out.println('Conflicts: (' + resolver.conflicts.length + ')')
	for (var c in resolver.conflicts) {
		var conflict = resolver.conflicts[c]
		for (var m in conflict) {
			var module = conflict[m]
			module.dump(sincerity.out, false, 1)
		}
	}
	
	// Fetch
	resolver.fetch('zzz/libraries/jars', true)
	
	resolver.release()
}
