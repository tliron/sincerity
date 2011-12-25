//
// This file is part of the Sincerity Foundation Library for JavaScript
//
// Copyright 2011 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.opensource.org/licenses/lgpl-3.0.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

document.executeOnce('/sincerity/jvm/')
document.executeOnce('/sincerity/objects/')

var Sincerity = Sincerity || {}

/**
 * High-performance, robust utilities to work with files.
 *  
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.0
 */
Sincerity.Files = Sincerity.Files || function() {
	/** @exports Public as Sincerity.Files */
    var Public = {}

	/**
	 * Deletes a file or a directory.
	 * 
	 * @param {String|java.io.File} file The file or directory or its path
	 * @param {Boolean} [recursive=false] True to recursively delete a directory
	 * @returns {Boolean} True if the file or directory was completely deleted, or if it
	 *          didn't exist in the first place;
	 *          note that false could mean that parts of the delete succeeded
	 */
	Public.remove = function(file, recursive) {
		file = (Sincerity.Objects.isString(file) ? new java.io.File(file) : file).canonicalFile

		if (!file.exists()) {
			return true
		}

		if (recursive && file.directory) {
			var files = file.listFiles()
			for (var f in files) {
				if (!Public.remove(files[f], true)) {
					return false
				}
			}
		}

		return file['delete']()
	}
	
	/**
	 * Copies a file or directory. Directories are always copied recursively.
	 * 
	 * @param {String|java.io.File} fromFile The source file or directory or its path
	 * @param {String|java.io.File} toFile The destination file or directory or its path
	 * @returns {Boolean} True if the file or directory was completely copied;
	 *          note that false could mean that parts of the copy succeeded
	 */
	Public.copy = function(fromFile, toFile) {
		fromFile = (Sincerity.Objects.isString(fromFile) ? new java.io.File(fromFile) : fromFile).canonicalFile
		toFile = (Sincerity.Objects.isString(toFile) ? new java.io.File(toFile) : toFile).canonicalFile

		if (!fromFile.exists()) {
			return false
		}

		if (fromFile.directory) {
			if (!toFile.directory) {
				if (!toFile.mkdirs()) {
					return false
				}
			}
			
			var fromFiles = fromFile.listFiles()
			for (var f in fromFiles) {
				fromFile = fromFiles[f]
				if (!Public.copy(fromFile, new java.io.File(toFile, fromFile.name))) {
					return false
				}
			}
			
			return true
		}
		if (!toFile.exists()) {
			if (!toFile.createNewFile()) {
				return false
			}
		}
		
		var fromChannel = new java.io.FileInputStream(fromFile).channel
		try {
			var toChannel = new java.io.FileOutputStream(toFile).channel
			try {
				var size = fromChannel.size()
				return toChannel.transferFrom(fromChannel, 0, size) == size
			}
			finally {
				toChannel.close()
			}
		}
		finally {
			fromChannel.close()
		}
	}
	
	/**
	 * Moves a file or directory. Does a simple, fast rename if the source and destination
	 * are in the same filesystem, otherwise does a full copy-and-remove.
	 * 
	 * @param {String|java.io.File} fromFile The source file or directory or its path
	 * @param {String|java.io.File} toFile The destination file or directory or its path
	 * @param {Boolean} [recursive=false] True to recursively copy a directory and its files
	 * @returns {Boolean} True if the file or directory was moved;
	 *          note that false could mean that parts of the move succeeded
	 */
	Public.move = function(fromFile, toFile, recursive) {
		fromFile = (Sincerity.Objects.isString(fromFile) ? new java.io.File(fromFile) : fromFile).canonicalFile
		toFile = (Sincerity.Objects.isString(toFile) ? new java.io.File(toFile) : toFile).canonicalFile

		if (!fromFile.exists()) {
			return false
		}
		
		// This will work only if the source and destination are in the same filesystem
		if (fromFile.renameTo(toFile)) {
			return true
		}
		
		if (!Public.copy(fromFile, toFile)) {
			return false
		}
		
		return Public.remove(fromFile, recursive)
	}
	
	Public.erase = function(file) {
		file = (Sincerity.Objects.isString(file) ? new java.io.File(file) : file).canonicalFile
		new java.io.FileWriter(file).close()		
	}
	
	Public.makeExecutable = function(file) {
		file = (Sincerity.Objects.isString(file) ? new java.io.File(file) : file).canonicalFile
		if (file.exists()) {
			if (undefined !== file.executable) { // JVM6+ only
				file.executable = true
			}
			else {
				Sincerity.JVM.exec('chmod', ['+x', file])
			}
		}
	}
	
	/**
	 * Creates a temporary file in the operating system's default temporary file directory using
	 * a nonce. The file will be deleted when the JVM shuts down.
	 * 
	 * @param {String} prefix Must be at least 3 characters long
	 * @param {String} [suffix='.tmp']
	 * @returns {String} The file path
	 */
	Public.temporary = function(prefix, suffix) {
		var file = java.io.File.createTempFile(prefix, suffix)
		file.deleteOnExit()
		return String(file)
	}
	
	/**
	 * Opens a file for writing text, optionally with gzip compression.
	 * 
	 * @param {String|java.io.File} file The file or its path
	 * @param {Boolean} [gzip=false] True to gzip the output
	 * @returns {java.io.PrintWriter}
	 */
	Public.openForTextWriting = function(file, gzip) {
		file = (Sincerity.Objects.isString(file) ? new java.io.File(file) : file).canonicalFile

		var stream = new java.io.FileOutputStream(file)
		if (gzip) {
			stream = new java.util.zip.GZIPOutputStream(stream)
		}

		var writer = new java.io.OutputStreamWriter(stream)
		writer = new java.io.BufferedWriter(writer)
		writer = new java.io.PrintWriter(writer)
		
		return writer
    }

	/**
	 * Opens a file for reading text, optionally with gzip decompression.
	 * 
	 * @param {String|java.io.File} file The file or its path
	 * @param {Boolean} [gzip=false] True to gunzip the input
	 * @returns {java.io.Reader}
	 */
	Public.openForTextReading = function(file, gzip) {
		file = (Sincerity.Objects.isString(file) ? new java.io.File(file) : file).canonicalFile

		var stream = new java.io.FileInputStream(file)
		if (gzip) {
			stream = new java.util.zip.GZIPInputStream(stream)
		}

		var reader = new java.io.InputStreamReader(stream)
		reader = new java.io.BufferedReader(reader)
		
		return reader
    }

	/**
	 * Fast loading of text contents of very large files, using the underlying operating system's
	 * file-to-memory mapping facilities.
	 * <p>
	 * Note that it does not return a string, but a buffer (which can be cast to a JavaScript
	 * String if required). 
	 * 
	 * @param {String|java.io.File} file The file or its path
	 * @param {String|java.nio.charset.Charset} [charset=default encoding (most likely UTF-8)] The charset in which the file is encoded
	 * @returns {java.nio.CharBuffer}
	 */
	Public.loadText = function(file, charset) {
		charset = Sincerity.Objects.isString(charset) ? Sincerity.JVM.getCharset(charset) : (Sincerity.Objects.exists(charset) ? charset : Sincerity.JVM.getCharset())
		var input = new java.io.FileInputStream(file)
		var channel = input.channel
		try {
			var buffer = channel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, channel.size())
			return charset.decode(buffer)
		}
		finally {
			channel.close()
		}
	}

	/**
	 * Fast grep from file to file.
	 * 
	 * @param {String|java.io.File} inputFile The input file or its path
	 * @param {String|java.io.File} outputFile The output file or its path (will be overwritten)
	 * @param {RegExp} pattern Only include lines that match this pattern
	 * @param {String|java.nio.charset.Charset} [charset=default encoding (most likely UTF-8)] The charset in which the file is encoded
	 */
	Public.grep = function(inputFile, outputFile, pattern, charset) {
		charset = Sincerity.Objects.isString(charset) ? Sincerity.JVM.getCharset(charset) : (Sincerity.Objects.exists(charset) ? charset : Sincerity.JVM.getCharset())

		var buffer = Public.loadText(inputFile, charset)

		var output = new java.io.FileOutputStream(outputFile)
		output = Sincerity.Objects.exists(charset) ? new java.io.OutputStreamWriter(output, charset) : new java.io.OutputStreamWriter(output)
		output = new java.io.BufferedWriter(output)
		try {
			var lineMatcher = linePattern.matcher(buffer)
			while (lineMatcher.find()) {
				var line = String(lineMatcher.group())
				if (line.search(pattern) != -1) {
					output.write(line)
				}
			}
		}
		finally {
			output.close()
		}
	}
	
	/**
	 * Fast tail.
	 * 
	 * @param {String|java.io.File} file The file or its path
	 * @param {Number} position Position in the file at which to start
	 * @param {Boolean} forward True to go forward from position, false to go backward
	 * @param {Number} count Number of lines
	 * @param {String|java.nio.charset.Charset} [charset=default encoding (most likely UTF-8)] The charset in which the file is encoded
	 */
	Public.tail = function(file, position, forward, count, charset) {
		var randomAccessFile = new java.io.RandomAccessFile(file, 'r')
		var position = Sincerity.Objects.exists(position) ? position : randomAccessFile.length() - 1
		var start, end

		try {
			// Find start and end of section
			if (forward) {
				if (position > 0) {
					randomAccessFile.seek(position - 1)

					// This will work for Unicode, too, because Unicode reserves newline codes!
					if (randomAccessFile.readByte() == 10) {
						// We are at the beginning of a line
						start = position
					}
					else {
						randomAccessFile.readLine()

						start = randomAccessFile.filePointer
					}
				}

				// Go forward 'count' number of newlines
				var newlines = count
				while (newlines-- > 0) {
					var line = randomAccessFile.readLine()

					if (line == null) {
						// Not enough lines reading forward, so read backward from end
						return Public.tail(file, null, false, count)
					}
				}

				end = randomAccessFile.filePointer - 1
			}
			else {
				end = position - 1
				
				// Go back 'count' number of newlines
				start = end - 1
				if (start < 0) {
					return {
						start: 0,
						end: 0,
						text: ''
					}
				}
				
				var newlines = count
				while ((newlines > 0) && (start > 0)) {
					randomAccessFile.seek(--start)

					// This will work for Unicode, too, because Unicode reserves newline codes!
					if (randomAccessFile.readByte() == 10) {
						newlines--
					}
				}
				
				start++
				randomAccessFile.seek(start)
			}
			
			// Read bytes into text
			var bytes = Sincerity.JVM.newArray(end - start + 1, 'byte')
			randomAccessFile.seek(start)
			randomAccessFile.read(bytes)
			var text = Sincerity.JVM.fromBytes(bytes, charset)
			
			return {
				start: start,
				end: end,
				text: text
			}
		}
		finally {
			randomAccessFile.close()
		}
	}
	
	//
	// Initialization
	//

	var linePattern = java.util.regex.Pattern.compile('.*\r?\n')
	
	return Public
}()
