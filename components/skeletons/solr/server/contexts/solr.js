
importClass(java.lang.System)

System.setProperty('solr.solr.home', sincerity.container.getConfigurationFile('solr'))
System.setProperty('solr.data.dir', sincerity.container.getFile('data', 'solr'))
System.setProperty('solr.velocity.enabled', false)

// velocity
// core
// solrj