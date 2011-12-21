
print('\nTo start your LWJGL game, run: "sincerity run lwjgl"\n\n')

// Let's clear out this file so that we don't get the message again
new java.io.FileWriter(sincerity.container.getLibrariesFile('installers', 'lwjgl.js')).close()
