
print('\nTo start your Restlet component, run: "sincerity start component"\n\n')

document.executeOnce('/savory/files/')

// Let's clear out this file so that we don't get the message again
Savory.Files.erase(sincerity.container.getLibrariesFile('installers', 'restlet.js'))
