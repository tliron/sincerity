
println()
println('To start your Restlet component, run: "sincerity delegate:start restlet"')
println()

document.require('/sincerity/files/')

// Let's clear out this file so that we don't get the message again
Sincerity.Files.erase(sincerity.container.getLibrariesFile('scripturian', 'installers', 'restlet.js'))
