
logger = repository.getLogger('org.apache.hadoop.mapred.JobInProgress$JobSummary')
logger.addAppender(appenders.hadoopMapreduceJob)
logger.additivity = false
