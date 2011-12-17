importClass(org.tanukisoftware.wrapper.WrapperManager, org.tanukisoftware.wrapper.WrapperProcessConfig)

var config = new WrapperProcessConfig()
WrapperManager.exec([sincerity.container.getLibrariesFile('native', 'wrapper-linux-x86-64'), sincerity.container.getConfigurationFile('service.conf')], config)

//sincerity.run('main:main', '')

//var app = new org.tanukisoftware.wrapper.WrapperSimpleApp()

