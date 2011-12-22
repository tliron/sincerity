

var manageFile = sincerity.container.getFile('project', 'manage.py')

sincerity.run('python:python', [manageFile, 'runserver'])
