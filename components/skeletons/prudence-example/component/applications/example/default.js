
document.executeOnce('/savory/sincerity/')
document.executeOnce('/prudence/')

var app = new Prudence.Application()

Savory.Sincerity.include('settings')
Savory.Sincerity.include('routing')

app = app.create(component)

// Restlets
Savory.Sincerity.include('restlets')
