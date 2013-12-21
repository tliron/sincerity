
logger = repository.getLogger('org.apache.hadoop.mapred.JobInProgress$JobSummary')
logger.addAppender(appenders['file:hadoop.mapreduceJob'])
logger.additivity = false
