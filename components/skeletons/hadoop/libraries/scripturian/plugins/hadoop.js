
importClass(
	java.lang.System,
	com.threecrickets.sincerity.exception.CommandException,
	com.threecrickets.sincerity.exception.BadArgumentsCommandException)

var commands = {
	namenode: 'org.apache.hadoop.hdfs.server.namenode.NameNode',
	secondarynamenode: 'org.apache.hadoop.hdfs.server.namenode.SecondaryNameNode',
	datanode: 'org.apache.hadoop.hdfs.server.datanode.DataNode',
	fs: 'org.apache.hadoop.fs.FsShell',
	dfs: 'org.apache.hadoop.fs.FsShell',
	dfsadmin: 'org.apache.hadoop.hdfs.tools.DFSAdmin',
	mradmin: 'org.apache.hadoop.mapred.tools.MRAdmin',
	fsck: 'org.apache.hadoop.hdfs.tools.DFSck',
	balancer: 'org.apache.hadoop.hdfs.server.balancer.Balancer',
	fetchdt: 'org.apache.hadoop.hdfs.tools.DelegationTokenFetcher',
	jobtracker: 'org.apache.hadoop.mapred.JobTracker',
	historyserver: 'org.apache.hadoop.mapred.JobHistoryServer',
	tasktracker: 'org.apache.hadoop.mapred.TaskTracker',
	job: 'org.apache.hadoop.mapred.JobClient',
	queue: 'org.apache.hadoop.mapred.JobQueueClient',
	pipes: 'org.apache.hadoop.mapred.pipes.Submitter',
	version: 'org.apache.hadoop.util.VersionInfo',
	jar: 'org.apache.hadoop.util.RunJar',
	distcp: 'org.apache.hadoop.tools.DistCp',
	daemonlog: 'org.apache.hadoop.log.LogLevel',
	archive: 'org.apache.hadoop.tools.HadoopArchives',
	sampler: 'org.apache.hadoop.mapred.lib.InputSampler'
} 

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['hadoop']
}

function run(command) {
	switch (String(command.name)) {
		case 'hadoop':
			hadoop(command)
			break
	}
}

function hadoop(command) {
	if (command.arguments.length < 1) {
		println('Available Hadoop commands:')
		for (var commandName in commands) {
			println(' ' + commandName)
		}
		return
	}
	
	var name = System.getProperty('os.name')
	if (name == 'Linux') {
		// Check for native Linux libraries
		var binary = command.sincerity.container.getLibrariesFile('native', 'libhadoop.so')
		if (!binary.exists()) {
			var architecture = System.getProperty('os.arch')
			var bits = System.getProperty('sun.arch.data.model')
			if ((architecture.indexOf('86') != -1) || (architecture.indexOf('amd') != -1)) {
				var version = command.sincerity.container.dependencies.resolvedDependencies.getVersion('org.apache.hadoop', 'hadoop-standalone')
				if (bits == '64') {
					command.sincerity.run(['dependencies:add', 'org.apache.hadoop', 'hadoop-linux-amd64', version])
				}
				else {
					command.sincerity.run(['dependencies:add', 'org.apache.hadoop', 'hadoop-linux-i386', version])
				}
				command.sincerity.run('artifacts:install')
				return
			}
		}
	}
	
	var commandName = command.arguments[0].toLowerCase()
	if (commandName == 'start') {
		command.sincerity.run(['service:service', 'namenode', 'start'])
		command.sincerity.run(['service:service', 'datanode', 'start'])
		return
	}
	else if (commandName == 'stop') {
		command.sincerity.run(['service:service', 'datanode', 'stop'])
		command.sincerity.run(['service:service', 'namenode', 'stop'])
		return
	}
	else if (commandName == 'status') {
		command.sincerity.run(['service:service', 'namenode', 'status'])
		command.sincerity.run(['service:service', 'datanode', 'status'])
		return
	}
	
	var className = commands[commandName]
	if (!className) {
		throw new CommandException(command, 'Unsupported Hadoop command: ' + commandName)		
	}

	var runArguments = ['delegate:main', className]
	for (var i = 1, l = command.arguments.length; i < l; i++) {
		runArguments.push(command.arguments[i])
	}

	System.setProperty('hadoop.home.dir', command.sincerity.container.root)
	System.setProperty('hadoop.log.dir', command.sincerity.container.getLogsFile('hadoop'))
	System.setProperty('hadoop.log.file', 'hadoop.log')
	System.setProperty('hadoop.root.logger', 'INFO,console')
	//System.setProperty('hadoop.id.str=$HADOOP_IDENT_STRING"
	
	command.sincerity.container.bootstrap.addFile(command.sincerity.container.getConfigurationFile('hadoop'))
	
	command.sincerity.run(runArguments)
}

function getPlatform() {
	org.apache.hadoop.util.PlatformName	
}
