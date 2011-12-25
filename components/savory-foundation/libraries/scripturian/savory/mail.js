//
// This file is part of the Savory Foundation Library for JavaScript
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

document.executeOnce('/savory/classes/')
document.executeOnce('/savory/templates/')
document.executeOnce('/savory/objects/')
document.executeOnce('/savory/jvm/')

var Savory = Savory || {}

/**
 * Email utilities. Supports mixed-media HTML/plain-text emails.
 * <p>
 * JavaScript-friendly wrapper over JavaMail. 
 *
 * @namespace
 * @requires javax.mail.jar
 * @see Visit <a href="http://www.oracle.com/technetwork/java/javamail/">JavaMail</a>
 * 
 * @author Tal Liron
 * @version 1.0
 */
Savory.Mail = Savory.Mail || function() {
	/** @exports Public as Savory.Mail */
    var Public = {}

	/**
	 * A combined template for the subject and content an email message, also supporting mixed-media
	 * HTML/plain-text emails. 
	 * <p>
	 * Arguments can be either:
	 * a dict of templates: {subject:'', text:'', html:''},
	 * or (textPack, prefix): where subject, text and html will be taken from the
	 * {@link Savory.Internationalization.Pack} as prefix+'.subject', prefix+'.text' and prefix+'.html'.
	 * <p>
	 * In both cases the 'html' template is optional.
	 * 
	 * @class
	 * @name Savory.Mail.MessageTemplate
	 */
	Public.MessageTemplate = Savory.Classes.define(function() {
		/** @exports Public as Savory.Mail.MessageTemplate */
	    var Public = {}
	    
	    /** @ignore */
	    Public._construct = function(/* arguments */) {
			if (arguments.length == 2) {
				var textPack = arguments[0]
				var prefix = arguments[1]
				this.templates = {
					subject: textPack.get(prefix + '.subject'),
					text: textPack.get(prefix + '.text'),
					html: textPack.getOrNull(prefix + '.html')
				}
			}
			else {
				this.templates = arguments[0]
			}
	    }
	    
		/**
		 * Casts the subject, text and html templates with the filling.
		 * The result can be used as the 'message' param in {@link Savory.Mail.SMTP#send}.
		 * 
		 * @returns A dict in the form {subject: '...', text: '...'} or
		 *          {subject: '...', text: '...', html: '...'}
		 * @see Savory.Templates#cast
		 */
		Public.cast = function(filling) {
			var message = {
				subject: this.templates.subject.cast(filling),
				text: this.templates.text.cast(filling)
			}
			if (this.templates.html) {
				message.html = this.templates.html.cast(filling)
			}
			return message
		}
		
		return Public
	}())
	
	/**
	 * Represents an SMTP host (thread-safe).
	 * <p>
	 * It's up to you to install and configure the host for the proper behavior. See <a href="http://www.postfix.org/">Postfix</a>
	 * for a mature SMTP host implementation running on most Unix environments.
	 * <p>
	 * For internet applications, you will probably want to configure your SMTP host to simply relay to the destination host.
	 * Intranet applications may require more specialized delivery and storage within your organization's infrastructure. 
	 * 
	 * @class
	 * @name Savory.Mail.SMTP
	 * 
	 * @param {Object|String} [params=application.globals.get('savory.foundation.mail.smtp.host') || '127.0.0.1'] Params to be applied
	 *        to javax.mail.Session.getInstance; if it is a string, will be considered as the 'mail.smtp.host' param
	 */
	Public.SMTP = Savory.Classes.define(function() {
		/** @exports Public as Savory.Mail.SMTP */
	    var Public = {}
	    
	    /** @ignore */
	    Public._construct = function(params) {
	    	params = Savory.Objects.isString(params) ? {'mail.smtp.host': String(params)} : (Savory.Objects.exists(params) ? Savory.Objects.clone(params) : {})
	    	params['mail.transport.protocol'] = 'smtp'
    		params['mail.smtp.host'] = params['mail.smtp.host'] || application.globals.get('savory.foundation.mail.smtp.host') || '127.0.0.1'
			this.session = javax.mail.Session.getInstance(Savory.JVM.toProperties(params))
	    }

	    /**
		 * Sends an email via SMTP.
		 * 
		 * @param params
		 * @param {String} params.from The email address of the sender
		 * @param {String|String[]} params.to One or more recipient email addresses
		 * @param [params.replyTo] The email address to use for replying
		 * @param message The message to send
		 * @param {String} message.subject
		 * @param {String} message.text
		 * @param {String} [message.html] If present, sends as a mixed-media HTML/plain-text message
		 */
		Public.send = function(params, message) {
			var mimeMessage = new javax.mail.internet.MimeMessage(this.session)
			
			mimeMessage.setFrom(new javax.mail.internet.InternetAddress(params.from))
			mimeMessage.subject = message.subject

			params.to = Savory.Objects.array(params.to)
			for (var t in params.to) {
				mimeMessage.addRecipient(javax.mail.Message.RecipientType.TO, new javax.mail.internet.InternetAddress(params.to[t]))
			}

			params.replyTo = Savory.Objects.array(params.replyTo)
			var replyTo = []
			for (var r in params.replyTo) {
				replyTo.push(new javax.mail.internet.InternetAddress(params.replyTo[r]))
			}
			if (replyTo.length) {
				mimeMessage.replyTo = Savory.JVM.toArray(replyTo, 'javax.mail.Address')
			}
			
			if (!message.html) {
				// Simple text message
				mimeMessage.setText(message.text, 'UTF-8')
			}
			else {
				// Multipart message with alternatives
				var textPart = new javax.mail.internet.MimeBodyPart()
				textPart.setText(message.text, 'UTF-8')
				
				var htmlPart = new javax.mail.internet.MimeBodyPart()
				htmlPart.setText(message.html, 'UTF-8', 'html')
				
				var multipart = new javax.mail.internet.MimeMultipart('alternative')
				multipart.addBodyPart(textPart)
				multipart.addBodyPart(htmlPart)
				
				mimeMessage.setContent(multipart)
			}
			
			javax.mail.Transport.send(mimeMessage)
		}

		return Public
	}())
	
	return Public
}()
