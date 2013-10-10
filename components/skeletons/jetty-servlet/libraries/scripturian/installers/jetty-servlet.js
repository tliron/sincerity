
println()
println('To start your servlet container, run: "sincerity delegate:start jetty"')
println()

document.require('/sincerity/files/')

// Let's clear out this file so that we don't get the message again
Sincerity.Files.erase(sincerity.container.getLibrariesFile('scripturian', 'installers', 'jetty-servlet.js'))
