
importClass(java.lang.System)

System.setProperty('orientdb.config.file', sincerity.container.getConfigurationFile('orientdb', 'server.conf'))
System.setProperty('orientdb.www.path', sincerity.container.getFile('web'))
System.setProperty('ORIENTDB_HOME', sincerity.container.root)

sincerity.run('delegate:main', ['com.orientechnologies.orient.server.OServerMain'])
