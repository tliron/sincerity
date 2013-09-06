
// Allow for running specific applications
var arguments = application.arguments.length
if(arguments > 1) {
	var applications = ''
	for(var i = 1; i < arguments; i++) {
		var app = application.arguments[i]
		applications += app
		if(i < arguments - 1) {
			applications += ','
		}
	}
	java.lang.System.setProperty('restlet.applications', applications)
}

sincerity.run(['delegate:start', '/component/'])
