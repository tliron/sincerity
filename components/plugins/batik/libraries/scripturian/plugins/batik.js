
document.execute('/sincerity/jvm/')

importClass(
	com.threecrickets.sincerity.exception.CommandException)

function getCommands() {
	return ['render']
}

function run(command) {
	switch (String(command.name)) {
		case 'render':
			render(command)
			break
	}
}

function render(command) {
	command.parse = true
	if (command.arguments.length < 2) {
		throw new BadArgumentsCommandException(command, 'SVG source path', 'rendered output path')
	}

	var sourceFile = command.arguments[0]
	var renderedFile = command.arguments[1]
	var type = String(renderedFile).split('.').pop().toLowerCase()

	switch (type) {
		case 'jpg':
		case 'jpeg':
			transcoder = new org.apache.batik.transcoder.image.JPEGTranscoder()
			var quality = command.properties.get('quality')
			if (null !== quality) {
				quality = parseFloat(quality)
			}
			else {
				quality = 0.8
			}
			transcoder.addTranscodingHint(org.apache.batik.transcoder.image.JPEGTranscoder.KEY_QUALITY, new java.lang.Float(quality))
			break

		case 'png':
			transcoder = new org.apache.batik.transcoder.image.PNGTranscoder()
			break
			
		case 'pdf':
			transcoder = new org.apache.fop.svg.PDFTranscoder()
			break
			
		default:
			throw new CommandException(command, 'Unsupported output type: ' + type)
	}

	var reader = new java.io.FileReader(sourceFile)
	try {
		var input = new org.apache.batik.transcoder.TranscoderInput(reader)
		var stream = new java.io.BufferedOutputStream(new java.io.FileOutputStream(renderedFile))
		try {
			var output = new org.apache.batik.transcoder.TranscoderOutput(stream)
			transcoder.transcode(input, output)
		}
		finally {
			stream.close()
		}
	}
	finally {
		reader.close()
	}
}
