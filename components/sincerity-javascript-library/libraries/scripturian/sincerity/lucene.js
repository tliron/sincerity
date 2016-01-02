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
	'/sincerity/classes/',
	'/sincerity/objects/',
	'/sincerity/jvm/')

var Sincerity = Sincerity || {}

/**
 * JavaScript-friendly wrapper over Lucene.
 * 
 * @namespace
 * @requires org.apache.lucene.jar
 * @see Visit <a href="http://lucene.apache.org/">Lucene</a>
 * 
 * @author Tal Liron
 * @version 1.0
 */
Sincerity.Lucene = Sincerity.Lucene || function() {
	/** @exports Public as Sincerity.Lucene */
    var Public = {}

	/**
	 * Converts a JavaScript value into a Lucene textual field.
	 * 
	 * @param {String} name The field name
	 * @param {Object} o The explicit field value (will be converted into a string) or a dict
	 * @param [o.value] The field value (will be converted into a string)
	 * @param {Boolean|String} [o.store=true] Whether to store the field in the directory (can also be string values 'yes' or 'no') 
	 * @param {Boolean|String} [o.index=true] Whether and how to index the field (can also be string values 'analyzed',
	 *        'analyzedNoNorms', 'no', 'notAnalyzed' or 'notAnalyzedNoNorms')
	 * @returns {org.apache.lucene.document.Field}
	 */
	Public.createField = function(name, o) {
		if (Sincerity.Objects.isDict(o, true)) {
			var store = (typeof o.store == 'boolean' ? (o.store ? fieldStore.yes : fieldStore.no) : fieldStore[o.store]) || fieldStore.yes
			var index = (typeof o.index == 'boolean' ? (o.index ? fieldIndex.analyzed : fieldIndex.no) : fieldIndex[o.index]) || fieldIndex.analyzed
			return new org.apache.lucene.document.Field(name, String(o.value), store, index)
		}
		else {
			return new org.apache.lucene.document.Field(name, String(o), fieldStore.yes, fieldIndex.analyzed)
		}
	}
	
	/**
	 * Converts a JavaScript dict into a Lucene document.
	 * <p>
	 * Note that Lucene documents are flat, with no hierarchical depth,
	 * so you may want to call {@link Sincerity.Objects#flatten} first for more complex
	 * data structures.
	 * 
	 * @param o A flat dict
	 * @returns {org.apache.lucene.document.Document}
	 * @see #createField
	 */
	Public.createDocument = function(o) {
	    var doc = new org.apache.lucene.document.Document()
	    for (var k in o) {
	    	var field = Public.createField(k, o[k])
	    	doc.add(field)
	    }
	    return doc
	}
	
	/**
	 * Converts a Lucene document into a JavaScript dict.
	 * 
	 * @param {org.apache.lucene.document.Document} doc The Lucene document
	 * @returns {Object} A dict
	 */
	Public.fromDocument = function(doc) {
		var o = {}
		for (var i = doc.fields.iterator(); i.hasNext(); ) {
			var field = i.next()
			var name = field.name()
			o[name] = doc.get(name)
		}
		return o
	}

	/**
	 * Represents a Lucene document store (JavaScript wrapper over org.apache.lucene.store.Directory).
	 * <p>
	 * Note that you must always call {@link #close} when done using a file directory. Though it's possible
	 * to store a global instance that is never closed, the recommended strategy is to create an instance
	 * on-the-fly when necessary, and then to close the instance when done using it. Instance creation is
	 * very lightweight and should not affect performance. Lucene automatically and efficiently handles concurrent
	 * access to the same file directory, even if different instances are used. 
	 * <p>
	 * Of course, in-process memory directories must be stored globally.
	 * 
	 * @class
	 * @name Sincerity.Lucene.Directory
	 * 
	 * @param {String|java.io.File} file The file or path for the directory; will be created if it doesn't
	 *        exist; leave empty to use an in-process memory directory
	 */
	Public.Directory = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Lucene.Directory */
	    var Public = {}
	    
	    /** @ignore */
	    Public._construct = function(file) {
			this.file = Sincerity.Objects.isString(file) ? new java.io.File(file) : file
			this.directory = Sincerity.Objects.exists(this.file) ? org.apache.lucene.store.FSDirectory.open(this.file) : new org.apache.lucene.store.RAMDirectory()
	    }

	    /**
	     * Closes the directory.
	     */
	    Public.close = function() {
	    	this.directory.close()
		}

	    /**
	     * Allows writing documents to the directory. The writer must be closed when done.
	     * 
	     * @param config See the <a href="http://lucene.apache.org/java/3_3_0/api/all/org/apache/lucene/index/IndexWriterConfig.html">Lucene API documentation</a>
	     * @param {String} config.openMode Can be 'create', 'append' or 'createOrAppend'
	     * @returns {org.apache.lucene.index.IndexWriter}
	     * @see Visit the <a href="http://lucene.apache.org/java/3_3_0/api/all/org/apache/lucene/index/IndexWriter.html">Lucene API documentation</a>
	     */
	    Public.createWriter = function(config) {
			var indexWriterConfig = new org.apache.lucene.index.IndexWriterConfig(version, analyzer)
			for (var k in config) {
				var value = config[k]
				if (k == 'openMode') {
					value = writerOpenMode[value]
				}
				indexWriterConfig[k] = value
			}
			return new org.apache.lucene.index.IndexWriter(this.directory, indexWriterConfig)
		}
		
	    /**
	     * Allows searching for documents in a directory.
	     * 
	     * @returns {org.apache.lucene.search.IndexSearcher}
	     * @see Visit the <a href="http://lucene.apache.org/java/3_3_0/api/all/org/apache/lucene/search/IndexSearcher.html">Lucene API documentation</a>
	     */
	    Public.createSearcher = function() {
			var searcher = new org.apache.lucene.search.IndexSearcher(this.directory, true)
			return searcher
		}
		
	    /**
	     * Writes documents to the directory.
	     * 
	     * @param {Array|Sincerity.Iterators.Iterator} iterator All entries will be passed through {@link Sincerity.Lucene#createDocument}
	     * @param [writerConfig] See {@link #createWriter}
	     */
	    Public.index = function(iterator, writerConfig) {
	    	iterator = Sincerity.Iterators.iterator(iterator)
			iterator = new Sincerity.Iterators.Transformer(iterator, Sincerity.Lucene.createDocument)
			var writer = this.createWriter(writerConfig)
			try {
				Sincerity.Iterators.consume(iterator, writer.addDocument, writer)
			}
			finally {
				writer.close()
			}
		}
	    
		/**
		 * Performs a search in the directory.
		 * 
		 * @param {String|Object} config Either a query or a complete {@link Sincerity.Lucene.Search} config
		 * @returns {Sincerity.Lucene.Search}
		 */
	    Public.search = function(config) {
	    	config = Sincerity.Objects.isString(config) ? {query: String(config)} : Sincerity.Objects.clone(config)
	    	config.directory = this
	    	return new Sincerity.Lucene.Search(config)
		}
		
		return Public
	}(Public))
    
	/**
	 * Represents Lucene search results.
	 * 
	 * @class
	 * @name Sincerity.Lucene.Search
	 * 
	 * @param config
	 * @param {String} config.query The Lucene query
	 * @param {Sincerity.Lucene.Directory} config.directory The directory
	 * @param {Number} [config.count=100] The maximum number of top documents to return
	 * @param {String} [config.defaultField] The default query field
	 * @param {String} [config.previewField] If present generates a short HTML preview of this field with
	 *        search terms highlighted in the fragments in which they appear
	 * @param {String} [config.preview='preview'] The new field in which to store the preview
	 * @param {Number} [config.fragmentLength=100] The maximum length of a preview fragment
	 * @param {Number} [config.maxFragments=5] The maximum number of fragments to include in the preview
	 * @param {String} [config.fragmentsSeparator='&amp;hellip;'] The HTML code to appear between preview fragments
	 * @param {String} [config.termPrefix='&lt;strong&gt'] The HTML code to appear before highlighted terms
	 * @param {String} [config.termPostfix='&lt;/strong&gt'] The HTML code to appear after highlighted terms
	 * 
	 * @see Sincerity.Lucene#fromDocument
	 */
    Public.Search = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Lucene.Directory */
	    var Public = {}
    	
	    /** @ignore */
	    Public._inherit = Sincerity.Iterators.Array

	    /** @ignore */
	    Public._construct = function(config) {
	    	// Parse query
			var parser = new org.apache.lucene.queryParser.QueryParser(version, config.defaultField, analyzer)
			this.query = parser.parse(config.query)

			// Setup highlighter
			if (config.previewField) {
				this.previewField = config.previewField
				this.preview = config.preview || 'preview'
				this.maxFragments = config.maxFragments || 5
				this.fragmentsSeparator = config.fragmentsSeparator || '&hellip;'
				var scorer = new org.apache.lucene.search.highlight.QueryScorer(this.query)
				var formatter = new org.apache.lucene.search.highlight.SimpleHTMLFormatter(config.termPrefix || '<strong>', config.termPostfix || '</strong>')
				this.highlighter = new org.apache.lucene.search.highlight.Highlighter(formatter, scorer)
				this.highlighter.textFragmenter.fragmentSize = config.fragmentLength || 100
			}
			
			this.count = config.count || 100
			this.searcher = config.directory.createSearcher()
	    }
	    
	    Public.hasNext = function() {
	    	if (!Sincerity.Objects.exists(this.hits)) {
				this.hits = this.searcher.search(this.query, null, this.count).scoreDocs
		    	this.length = Sincerity.Objects.exists(this.hits) ? this.hits.length : 0
				this.index = 0
	    	}
	    	
	    	return this.index < this.length
	    }
	    
	    Public.next = function() {
			var id = this.hits[this.index++].doc
			var doc = this.searcher.doc(id)
			var value = Module.fromDocument(doc)
			
			if (Sincerity.Objects.exists(this.highlighter)) {
				// Add preview to value
				try {
					var tokens = org.apache.lucene.search.highlight.TokenSources.getAnyTokenStream(this.searcher.indexReader, id, this.previewField, analyzer)
					value[this.preview] = this.highlighter.getBestFragments(tokens, value[this.previewField], this.maxFragments, this.fragmentsSeparator)
				}
				catch (x if Sincerity.JVM.isException(x, java.lang.IllegalArgumentException)) {
					// Field can't be analyzed; it's probably not stored in the document
				}
			}
			
			return value
	    }

	    Public.close = function() {
	    	this.searcher.close()
	    }

		return Public
	}(Public))
	
	//
	// Initialization
	//

    var version = org.apache.lucene.util.Version.LUCENE_31
	
    var fieldStore = {
    	yes: org.apache.lucene.document.Field.Store.YES,
    	no: org.apache.lucene.document.Field.Store.NO
    }
    
    var fieldIndex = {
    	analyzed: org.apache.lucene.document.Field.Index.ANALYZED,
    	analyzedNoNorms: org.apache.lucene.document.Field.Index.ANALYZED_NO_NORMS,
    	no: org.apache.lucene.document.Field.Index.NO,
    	notAnalyzed: org.apache.lucene.document.Field.Index.NOT_ANALYZED,
    	notAnalyzedNoNorms: org.apache.lucene.document.Field.Index.NOT_ANALYZED_NO_NORMS
    }
    
    var writerOpenMode = {
    	append: org.apache.lucene.index.IndexWriterConfig.OpenMode.APPEND,
    	create: org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE,
    	createOrAppend: org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE_OR_APPEND
    }
    
    var analyzer = new org.apache.lucene.analysis.standard.StandardAnalyzer(version)

	return Public	
}()
