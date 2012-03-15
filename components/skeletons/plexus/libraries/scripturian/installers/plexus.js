
println()
println('To start your Plexus container, run: "sincerity start plexus"')
println()

document.executeOnce('/sincerity/files/')

// Let's clear out this file so that we don't get the message again
Sincerity.Files.erase(sincerity.container.getLibrariesFile('scripturian', 'installers', 'plexus.js'))
