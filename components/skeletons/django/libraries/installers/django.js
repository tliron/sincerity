
if (!sincerity.container.getExecutablesFile('django-admin.py').exists()) {
	sincerity.run('delegate:execute', ['--block', 'easy_install', 'Django==1.2.7', 'django-jython==1.2.0'])
}
