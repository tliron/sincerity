
importClass(
	org.restlet.resource.Directory,
	java.io.File)

var Static = function(path, listingAllowed) {
	this.create = function(context) {
		var directory = new Directory(context, new File(Sincerity.Sincerity.here, this.path).absoluteFile.toURI())
		directory.listingAllowed = listingAllowed || false
		return directory
	}
	
	this.path = path
}

var Dynamic = function() {
}

var Chain = function() {
}

var Resource = function() {
}