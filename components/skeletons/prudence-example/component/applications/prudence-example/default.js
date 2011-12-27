
document.executeOnce('/savory/sincerity/')
document.executeOnce('/prudence/')

var app = new Prudence.Application()

Savory.Sincerity.include('settings')
Savory.Sincerity.include('uri-space')

app.create(component)

// Additional restlets
Savory.Sincerity.include('restlets')
