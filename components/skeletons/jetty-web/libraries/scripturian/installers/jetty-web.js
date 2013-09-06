
println()
print('To start your web server, run: "sincerity delegate:start jetty"')
println()

document.execute('/sincerity/files/')

// Let's clear out this file so that we don't get the message again
Sincerity.Files.erase(sincerity.container.getLibrariesFile('scripturian', 'installers', 'jetty-web.js'))
