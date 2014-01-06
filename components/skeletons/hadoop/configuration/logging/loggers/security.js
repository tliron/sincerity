
var logger = repository.getLogger('SecurityLogger')
logger.addAppender(appenders['file:hadoop.securityAudit'])
logger.additivity = false
