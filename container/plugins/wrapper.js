
function getCommands() {
	return ['start', 'stop', 'console']
}

function run(command) {
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
	println('Starting!');
}

function stop() {
	println('Stopping!');
}

function console() {
	println('Console!');
}
