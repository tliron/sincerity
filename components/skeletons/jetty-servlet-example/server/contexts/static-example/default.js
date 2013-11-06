
importClass(
	org.eclipse.jetty.server.handler.ContextHandler,
	org.eclipse.jetty.server.handler.HandlerList)

// The context
var context = new ContextHandler(contexts, '/')
context.displayName = 'The static example context for the servlet skeleton'

// Assemble context
context.handler = new HandlerList()
Sincerity.Container.executeAll('handlers')
