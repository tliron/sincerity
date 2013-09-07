
// See: http://blog.rubyrockers.com/2011/03/rails3-application-jruby/

sincerity.run(['ruby:gem', 'install', '--verbose', 'rails'])

println('Creating Rails app...')
sincerity.run(['delegate:execute', 'rails', 'new', sincerity.container.getFile('app'), '--database=jdbcsqlite3', '--template=http://jruby.org/rails3.rb', '--ruby=' + sincerity.container.getExecutablesFile('ruby')])

println('Please wait while installing additional dependencies for your Rails app...')
sincerity.run(['ruby:ruby', '-C' + command.sincerity.container.getFile('app'), 'bin/bundle', 'install'])

println()
println('To start your Rails server, run: "sincerity delegate:start rails"')
println()

document.execute('/sincerity/files/')

// Let's clear out this file so that we don't get the message again
Sincerity.Files.erase(sincerity.container.getLibrariesFile('scripturian', 'installers', 'rails.js'))
