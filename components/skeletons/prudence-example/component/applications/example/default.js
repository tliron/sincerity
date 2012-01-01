
document.executeOnce('/sincerity/container/')
document.executeOnce('/prudence/')

var app = new Prudence.Application()

Sincerity.Container.include('settings')
Sincerity.Container.include('routing')

app = app.create(component)

// Restlets
Sincerity.Container.include('restlets')
