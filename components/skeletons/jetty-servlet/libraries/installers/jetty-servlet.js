
print('\nTo start your servlet container, run: "sincerity start jetty"\n\n')

// Let's clear out this file so that we don't get the message again
new java.io.FileWriter(sincerity.container.getLibrariesFile('installers', 'jetty-servlet.js')).close()
