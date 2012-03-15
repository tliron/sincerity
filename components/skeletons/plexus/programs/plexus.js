
importClass(java.lang.System)

//
// Logging
//

try {
sincerity.run('logging:logging')
} catch(x) {}

//
// Launcher
//

System.setProperty('classworlds.conf', sincerity.container.getConfigurationFile('classworlds.conf'))
System.setProperty('bundleBasedir', sincerity.container.getLibrariesFile('jars'))
sincerity.run('delegate:main', ['org.codehaus.plexus.classworlds.launcher.Launcher'])
