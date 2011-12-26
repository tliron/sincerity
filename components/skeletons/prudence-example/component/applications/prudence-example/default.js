
document.executeOnce('/savory/sincerity/')
document.executeOnce('/prudence/')

var app = new Prudence.Application()

Savory.Sincerity.executeAll('types')
Savory.Sincerity.executeAll('settings')
Savory.Sincerity.executeAll('routes')

app.create(component)

// Additional restlets
Savory.Sincerity.executeAll('restlets')
