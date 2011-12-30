
function getCommands() {
	return ['mtail']
}

function run(command) {
	switch (String(command.name)) {
		case 'mtail':
			mtail(command)
			break
	}
}

function mtail(command) {
}
