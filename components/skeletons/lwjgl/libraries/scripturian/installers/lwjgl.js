
println()
println('To start your LWJGL game, run: "sincerity delegate:start lwjgl"')
println()

document.executeOnce('/sincerity/files/')

// Let's clear out this file so that we don't get the message again
Sincerity.Files.erase(sincerity.container.getLibrariesFile('scripturian', 'installers', 'lwjgl.js'))
