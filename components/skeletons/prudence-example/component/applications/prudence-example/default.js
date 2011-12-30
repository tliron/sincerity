
document.executeOnce('/savory/sincerity/')
document.executeOnce('/prudence/')

var app = new Prudence.Application()

Savory.Sincerity.include('settings')
Savory.Sincerity.include('routing')

app.create(component)
