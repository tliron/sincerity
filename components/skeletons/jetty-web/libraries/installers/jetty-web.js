
print('\nTo start your web server, run: "sincerity start jetty"\n\n')

// Let's clear out this file so that we don't get the message again
new java.io.FileWriter(sincerity.container.getLibrariesFile('installers', 'jetty-web.js')).close()