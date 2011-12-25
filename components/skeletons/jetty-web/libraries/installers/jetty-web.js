
print('\nTo start your web server, run: "sincerity start jetty"\n\n')

document.executeOnce('/savory/files/')

// Let's clear out this file so that we don't get the message again
Savory.Files.erase(sincerity.container.getLibrariesFile('installers', 'jetty-web.js'))
