
function getCommands() {
	return ['start', 'stop', 'console']
}

function run(command, arguments, sincerity) {
	switch (String(command)) {
		case 'start':
			start()
			break
		case 'stop':
			stop()
			break
		case 'console':
			console();
			break;
	}
}

function start() {
	print('Starting!\n');
}

function stop() {
	print('Stopping!\n');
}

function console() {
	print('Console!\n');
}
