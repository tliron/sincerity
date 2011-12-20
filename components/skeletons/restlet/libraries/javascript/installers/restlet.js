
print('\nTo start your Restlet component, run: "sincerity run restlet"\n\n')

// Let's clear out this file so that we don't get the message again
new java.io.FileWriter(sincerity.container.getLibrariesFile('javascript', 'installers', 'restlet.js')).close()
