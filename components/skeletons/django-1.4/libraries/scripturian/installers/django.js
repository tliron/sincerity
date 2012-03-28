
//sincerity.run('delegate:execute', ['easy_install', 'Django==1.2.7', 'django-jython==1.2.0'])

println()
println('To start your Django server, run: "sincerity start django"')
println('To manage your Django project, run: "sincerity django:manage"')
println()

document.executeOnce('/sincerity/files/')

// Let's clear out this file so that we don't get the message again
Sincerity.Files.erase(sincerity.container.getLibrariesFile('scripturian', 'installers', 'django.js'))
