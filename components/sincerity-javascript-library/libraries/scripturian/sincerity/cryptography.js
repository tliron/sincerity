//
// This file is part of the Sincerity Foundation Library
//
// Copyright 2011-2016 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

document.require(
	'/sincerity/jvm/',
	'/sincerity/objects/')

var Sincerity = Sincerity || {}

/**
 * Encryption, decryption, hashing, cryptographically-strong randoms,
 * hex and Base64 encoding. Uses the JVM cryptography implementation and
 * Apache Commons Codec.
 * 
 * @namespace
 * @see <a href="http://commons.apache.org/codec/">Apache Commons Codec</a>
 * 
 * @author Tal Liron
 * @version 1.0
 */
Sincerity.Cryptography = Sincerity.Cryptography || function() {
	/** @exports Public as Sincerity.Cryptography */
    var Public = {}

	/**
	 * Converts a string into a new JVM byte array, optionally prefixing it with salt.
	 * 
	 * @param {String} string The string
	 * @param {byte[]} [saltBytes] The salt
	 * @returns {byte[]}
	 */    	
	Public.toByteArray = function(string, saltBytes) {
		var bytes = Sincerity.JVM.toByteArray(string)
		
		if (Sincerity.Objects.exists(saltBytes)) {
			// Add salt before the bytes
			var bytesWithSalt = Sincerity.JVM.newArray(saltBytes.length + bytes.length, 'byte')
			java.lang.System.arraycopy(saltBytes, 0, bytesWithSalt, 0, saltBytes.length)
			java.lang.System.arraycopy(bytes, 0, bytesWithSalt, saltBytes.length, bytes.length)
			bytes = bytesWithSalt
		}
		
		return bytes
	}
	
	/**
	 * Converts a base64-encoded string into a new JVM byte array.
	 * 
	 * @param {String} string The base64-encoded string
	 * @returns {byte[]}
	 */
	Public.toByteArrayFromBase64 = function(string) {
		return org.apache.commons.codec.binary.Base64.decodeBase64(string)
	}

	/**
	 * Converts a JVM byte array into a base64-encoded string.
	 * 
	 * @param {byte[]} bytes The bytes
	 * @returns {String}
	 */
	Public.toBase64 = function(bytes) {
		bytes = org.apache.commons.codec.binary.Base64.encodeBase64(bytes)
		return Sincerity.JVM.fromBytes(bytes)
	}
	
	/**
	 * Converts a JVM byte array into a padded hex-encoded string.
	 * 
	 * @param {byte[]} bytes The bytes
	 * @returns {String}
	 */
	Public.toHex = function(bytes) {
		return String(com.threecrickets.sincerity.util.StringUtil.toHex(bytes))
	}
	
	/**
	 * Converts a JVM byte array into an encoded string.
	 * 
	 * @param {byte[]} bytes The bytes
	 * @param [encoding='base64'] Supported encodings: 'base64', 'hex'
	 * @returns {String}
	 */
	Public.toString = function(bytes, encoding) {
		switch (String(encoding)) {
			case 'hex':
				return Public.toHex(bytes)
			case 'base64':
			default:
				return Public.toBase64(bytes)
		}
	}
	
	/**
	 * Calculates the base64-encoded HMAC (Hash-based Message Authentication Code) for a binary payload.
	 * 
	 * @param {byte[]} payloadBytes The binary payload
	 * @param {byte[]} secretBytes The secret
	 * @param {String} algorithm The HMAC algorithm ('HmacSHA1', 'HmacSHA256', etc.)
	 * @param {String} [secretAlgorithm=algorithm] The secret algorithm, if different from the MAC algorithm
	 * @param {Boolean} [encoding='base64'] The encoding to use for the result
	 * @returns {String} An encoded HMAC or null if failed
	 */
	Public.hmac = function(payloadBytes, secretBytes, algorithm, secretAlgorithm, encoding) {
		var mac = javax.crypto.Mac.getInstance(algorithm)

		if (Sincerity.Objects.exists(mac)) {
			var key = new javax.crypto.spec.SecretKeySpec(secretBytes, secretAlgorithm || algorithm)
			mac.init(key)
			var digest = mac.doFinal(payloadBytes)
			
			return Public.toString(digest, encoding)
		}

		return null
	}

	/**
	 * Calculates the digest for a binary payload.
	 * 
	 * @param {byte[]} payload The binary payload
	 * @param {Number} iterations The number of digest iterations to run
	 * @param {String} algorithm The digest algorithm ('SHA-1', 'SHA-256', 'MD5', etc.)
	 * @param {Boolean} [encoding='base64'] The encoding to use for the result
	 * @returns {String} An encoded digest or null if failed
	 */
	Public.bytesDigest = function(payload, iterations, algorithm, encoding) {
		var messageDigest = java.security.MessageDigest.getInstance(algorithm)
		if (Sincerity.Objects.exists(messageDigest)) {
			var digest = payload
			
			for (var i = 0; i < iterations; i++) {
				messageDigest.reset()
				messageDigest.update(digest)
				digest = messageDigest.digest()
			}

			return Public.toString(digest, encoding)
		}
		
		return null
	}

	/**
	 * Calculates the digest for a textual payload, with support for optionally prefixing it
	 * with salt (before calculating the digest).
	 * 
	 * @param {String} payload The textual payload
	 * @param {byte[]} saltBytes The salt or null
	 * @param {Number} iterations The number of digest iterations to run
	 * @param {String} algorithm The digest algorithm ('SHA-1', 'SHA-256', 'MD5', etc.)
	 * @param {Boolean} [encoding='base64'] The encoding to use for the result
	 * @returns {String} An encoded digest or null if failed
	 */
	Public.digest = function(payload, saltBytes, iterations, algorithm, encoding) {
		payload = Public.toByteArray(payload, saltBytes)
		return Public.bytesDigest(payload, iterations, algorithm, encoding)
	}
	
	/**
	 * Calculates a digest for file contents. See {@link #digest}.
	 * 
	 * @param {String|<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/io/File.html">java.io.File</a>} file The file or its path
	 * @param {String} algorithm The digest algorithm ('SHA-1', 'SHA-256', 'MD5', etc.)
	 * @param {Boolean} [encoding='base64'] The encoding to use for the result
	 * @returns {String} An encoded digest or null if failed
	 */
	Public.fileDigest = function(file, algorithm, encoding) {
		var messageDigest = java.security.MessageDigest.getInstance(algorithm)
		if (Sincerity.Objects.exists(messageDigest)) {
			file = (Sincerity.Objects.isString(file) ? new java.io.File(file) : file).canonicalFile
			var stream = new java.io.FileInputStream(file)
			try {
				stream = new java.security.DigestInputStream(stream, messageDigest)
				var buffer = Sincerity.JVM.newArray(2048, 'byte')
				while (stream.read(buffer) != -1) {}
			}
			finally {
				stream.close()
			}
			
			var digest = messageDigest.digest()
			return Public.toString(digest, encoding)
		}
		
		return null
	}
	
	/**
	 * Shortcut to create a hex-encoded MD5 digest of a string
	 * (exactly equivalent to the md5 function in PHP).
	 * 
	 * @param {String} The string
	 * @returns {String} A hex-encoded digest or null if failed
	 */
	Public.md5 = function(string) {
		return Public.digest(string, null, 1, 'MD5', 'hex')
	}

	/**
	 * Extracts the text encrypted in a binary payload. The payload may optionally begin with an
	 * initialization vector for the cipher.
	 * 
	 * @param {byte[]} payloadBytes The binary payload
	 * @param {Number} ivLength Length (in bytes) of the initialization vector
	 * @param {byte[]} secretBytes The secret
	 * @param {String} algorithm The cipher algorithm
	 * @param {String} [secretAlgorithm=algorithm] The secret algorithm, if different from the cipher algorithm
	 * @returns {String} The text 
	 */
	Public.decode = function(payloadBytes, ivLength, secretBytes, algorithm, secretAlgorithm) {
		var cipher = javax.crypto.Cipher.getInstance(algorithm)

		if (Sincerity.Objects.exists(cipher)) {
			secretAlgorithm = secretAlgorithm || algorithm.split('/')[0]
			var key = new javax.crypto.spec.SecretKeySpec(secretBytes, secretAlgorithm)

			// Extract initialization vector from payload
			var iv = javax.crypto.spec.IvParameterSpec(java.util.Arrays.copyOf(payloadBytes, ivLength))
			payloadBytes = java.util.Arrays.copyOfRange(payloadBytes, ivLength, payloadBytes.length)
			
			cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key, iv)
			var decrypted = cipher.doFinal(payloadBytes)

			return Sincerity.JVM.fromBytes(decrypted)
		}
		
		return null
	}
	
	/**
	 * Generates a random byte phrase, where randomness is strong enough for most cryptographic purposes.
	 * 
	 * @param {Number} length Length in bytes
	 * @param {String} algorithm The algorithm ('SHA1PRNG', 'NativePRNG', 'AESCounterRNG', etc.) 
	 * @param {Boolean} [encoding='base64'] The encoding to use for the result
	 * @returns {String} An encoded string represented the random phrase
	 */
	Public.random = function(length, algorithm, encoding) {
		// Algorithms:
		//
		// NativePRNG - default on Linux, but slow, as it includes OS entropy
		// SHA1PRNG - default on Windows, implemented in Java, faster than NativePRNG but no entropy
		// AESCounterRNG - 10x faster than SHA1PRNG (from Uncommon Math project)
		
		var secureRandom = algorithm ? java.security.SecureRandom.getInstance(algorithm) : new java.security.SecureRandom()
		var random = Sincerity.JVM.newArray(length, 'byte')
		secureRandom.nextBytes(random)

		return Public.toString(random, encoding)
	}
	
	return Public
}()
