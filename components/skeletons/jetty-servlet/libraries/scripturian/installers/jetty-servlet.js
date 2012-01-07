
println()
println('To start your servlet container, run: "sincerity start jetty"')
println()

document.executeOnce('/sincerity/files/')

// Let's clear out this file so that we don't get the message again
Sincerity.Files.erase(sincerity.container.getLibrariesFile('scripturian', 'installers', 'jetty-servlet.js'))
