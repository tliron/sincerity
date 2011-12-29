
var Restlet = Restlet || function() {
	var Public = {}

	Public.getHost = function(component, name) {
		if (name == 'default') {
			return component.defaultHost
		}
		else if (name == 'internal') {
			return component.internalRouter
		}

		for (var i = component.hosts.iterator(); i.hasNext(); ) {
			var host = i.next()
			if (name == host.name) {
				return host
			}
		}
		
		return null
	}

	return Public
}()