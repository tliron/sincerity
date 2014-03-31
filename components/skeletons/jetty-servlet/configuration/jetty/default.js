port = 8080
root = 'web'

sslEnabled = false
sslContextFactory.keyStorePath = sincerity.container.getConfigurationFile(['jetty', 'jetty.jks'])
sslContextFactory.keyStorePassword = 'sincerity'
//sslContextFactory.keyPassword = 'sincerity'

spdyEnabled = false
spdyVersion = 3
