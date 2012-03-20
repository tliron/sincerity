
var logger = repository.getLogger('SecurityLogger')
logger.addAppender(appenders.hadoopSecurityAudit)
logger.additivity = false
