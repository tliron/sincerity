//
// This file is part of the Sincerity Foundation Library
//
// Copyright 2011-2014 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

document.require('/sincerity/classes/')

var Sincerity = Sincerity || {}

/**
 * A whole bunch of useful iterators with a consistent API.
 * <p>
 * Inspired by sequences in functional languages, Sincerity iterators are used to traverse potentially very large series
 * of entries without advance knowledge of how many entries are in the series. These are somewhat similar
 * to JavaScript generator functions, but more generic. A wrapper ({@link Sincerity.Iterators.Generator})
 * exists to let you use JavaScript generator functions here. 
 * <p>
 * Generally, any code that traverses arrays can be made more scalable if
 * iterators are used instead (and {@link Sincerity.Iterators.Array} lets you easily consume arrays).
 * The problem with arrays is that <i>all</i> entries exist in memory at once: so that
 * 1) it can take too much memory at once, and 2) it can waste resources, because we always fill
 * in the array entirely up-front, when we might only end up traversing a subset of the entries.
 * <p>
 * You must always close an iterator. The recommended semantics:
 * <pre>
 * var iterator = ...
 * try {
 *   while (iterator.hasNext()) {
 *     var next = iterator.next()
 *   }
 * }
 * finally {
 *   iterator.close()
 * }
 * </pre>
 * <p>
 * All the wrapping/chaining iterator classes make sure to close the underlying, wrapped iterators.
 * 
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.0
 */
Sincerity.Iterators = Sincerity.Iterators || function() {
	/** @exports Public as Sincerity.Iterators */
    var Public = {}
    
    /**
     * Makes sure to always return an iterator, converting arrays to {@link Sincerity.Iterators.Array}
     * if necessary.
     * 
     * @param {Array|Sincerity.Iterators.Iterator} value An iterator or an array or null
     * @returns {Sincerity.Iterators.Iterator}
     */
    Public.iterator = function(value) {
    	if (!Sincerity.Objects.exists(value)) {
    		return new Sincerity.Iterators.Iterator()
    	}
    	
    	if (Sincerity.Objects.isArray(value)) {
    		return new Sincerity.Iterators.Array(value)
    	}
    	
    	return value
    }

	/**
	 * Consumes an iterator into an array and closes it.
	 *  
	 * @param {Sincerity.Iterators.Iterator} iterator An iterator
	 * @param {Number} [start=0] How many entries to skip
	 * @param {Number} [limit] Maximum number of entries to consume
	 * @returns {Array}
	 */
	Public.toArray = function(iterator, start, limit) {
		var array = []
		
		try {
			if (limit === 0) {
				return array
			}
			if (start > 0) {
				iterator.skipCount(start)
			}
			if (limit > 0) {
				while (iterator.hasNext() && (limit-- > 0)) {
					array.push(iterator.next())
				}
			}
			else {
				while (iterator.hasNext()) {
					array.push(iterator.next())
				}
			}
		}
		finally {
			iterator.close()
		}
		
		return array
	}
    
    /**
     * Calls a function on all entries of the iterator, closing it when finished.
     * 
     * @param {Sincerity.Iterators.Iterato} iterator The iterator
     * @param {Function} fn A function that receives an entry; if it returns false the
     *        iteration is stopped (but the iterator will still be closed)
	 * @param [scope] The scope to use for fn
     */
    Public.consume = function(iterator, fn, scope) {
		try {
			while (iterator.hasNext()) {
				if (fn.call(scope, iterator.next()) === false) {
					break
				}
			}
		}
		finally {
			iterator.close()
		}
    }
		
	/**
	 * An iterator with no entries.
	 * 
	 * @class
	 * @name Sincerity.Iterators.Iterator
	 */
	Public.Iterator = Sincerity.Classes.define(function() {
		/** @exports Public as Sincerity.Iterators.Iterator */
	    var Public = {}

	    /**
		 * Checks if there are more entries to iterate. You should always
		 * call this function before calling {@link #next}!
		 * 
		 * @returns {Boolean} True if there are more entries 
		 */
	    Public.hasNext = function() {
			return false
		}
		
		/**
		 * Retrieves the next entry. Note that calling this function will have
		 * undefined behavior if the last {@link #hasNext} returned false. 
		 * 
		 * @returns The next entry
		 */
	    Public.next = function() {}
		
		/**
		 * Skips entries without retrieving them.
		 * <p>
		 * Depending on the iterator implementation, skipping can perform
		 * better than iterating one by one, so you should always prefer
		 * skipping if you are not interested in the content of the entries.
		 * <p>
		 * Note that skipCount() is optional! Some iterators may not
		 * support it, in which case you should use {@link #skip} instead. 
		 * 
		 * @param {Number} count How many entries to skip
		 * @returns {Number} How many entries were skipped
		 */
	    Public.skipCount = function(count) {
			var skipped = 0
			while (this.hasNext() && (count-- > 0)) {
				this.next()
				skipped++
			}
			return skipped
		}
		
		/**
		 * Skips entries without retrieving them. Nothing will happen if
		 * the iterator is exhausted before skipping the desired amount.
		 * <p>
		 * Depending on the iterator implementation, skipping can perform
		 * better than iterating one by one, so you should always prefer
		 * skipping if you are not interested in the content of the entries.
		 * <p>
		 * Note that some iterators may support {@link #skipCount} in
		 * addition to skip(), and you may prefer to call that instead.
		 * 
		 * @param {Number} count How many entries to skip
		 */
	    Public.skip = Public.skipCount
		
		/**
		 * Closes the iterator, releasing resources. The semantics of using
		 * iterators require you to always call close()! After calling close(),
		 * the iterator should not be further used.
		 */
	    Public.close = function() {}
		
		return Public
	}())
	
	/**
	 * Represents an array as an iterator.
	 * 
	 * @class
	 * @name Sincerity.Iterators.Array
	 * @augments Sincerity.Iterators.Iterator 
	 * @param {Array} array The array
	 */
	Public.Array = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Iterators.Array */
	    var Public = {}
	    
	    /** @ignore */
	    Public._inherit = Module.Iterator
	    
	    /** @ignore */
	    Public._construct = function(array) {
	    	this.array = array
	    	this.index = 0
	    	this.length = array ? array.length : 0
	    }

	    Public.hasNext = function() {
			return this.index < this.length
		}
		
	    Public.next = function() {
			return this.array[this.index++]
		}
		
	    Public.skip = Public.skipCount = function(count) {
	    	this.index += count
			return this.index < this.length ? count : this.length - (this.index + count)
		}
		
		return Public
	}(Public))
	
	/**
	 * Represents a JVM iterator (java.util.Iterator) as an iterator compatible
	 * with this library.
	 * <p>
	 * Actually, this class can wrap any iterator that supports hasNext() and next().
	 * 
	 * @class
	 * @name Sincerity.Iterators.JVM
	 * @augments Sincerity.Iterators.Iterator
	 * @param {java.util.Iterator} iterator The JVM iterator
	 */
	Public.JVM = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Iterators.JVM */
	    var Public = {}

	    /** @ignore */
	    Public._inherit = Module.Iterator

	    /** @ignore */
	    Public._construct = function(iterator) {
	    	this.iterator = iterator
	    }
	    
	    Public.hasNext = function() {
			return this.iterator.hasNext()
		}
		
	    Public.next = function() {
			return this.iterator.next()
		}
		
		return Public
	}(Public))

	/**
	 * Represents a JavaScript generator as an iterator compatible with this
	 * library.
	 * <p>
	 * The iterator is always one step ahead: the next entry is generated before
	 * next() is called, to make sure that we do indeed have more entries.
	 * This also means that skip() cannot be efficient, as it examines each
	 * and every entry.
	 * <p>
	 * See {@link Sincerity.Iterators.Fetcher} for an alternate way to use the generator
	 * pattern.
	 * 
	 * @class
	 * @name Sincerity.Iterators.Generator
	 * @augments Sincerity.Iterators.Iterator
	 * @param generator A generator is created by calling a function that
	 *        returns a next entry via the JavaScript "yield" keyword
	 */
	Public.Generator = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Iterators.Generator */
	    var Public = {}

	    /** @ignore */
	    Public._inherit = Module.Iterator

	    /** @ignore */
	    Public._construct = function(generator) {
	    	this.generator = generator
	    	this.nextValue = null
			this.hasNextFlag = generator ? true : false
			if (generator) {
				this.next()
			}
	    }
	    
	    Public.hasNext = function() {
			return this.hasNextFlag
		}
		
	    Public.next = function() {
			var value = this.nextValue
			try {
				this.nextValue = this.generator.next()
			}
			catch (x if x instanceof StopIteration) {
				this.hasNextFlag = false
			}
			return value
		}
		
	    Public.close = function() {
	    	this.generator.close()
		}
		
		return Public
	}(Public))
	
	/**
	 * An iterator that fetches entries one by one on demand.
	 * <p>
	 * In some cases, a fetcher can work similarly to JavaScript's generator functions.
	 * But, if you have a real generator function, use {@link Sincerity.Iterators.Generator}, instead. 
	 * <p>
	 * The iterator is always one step ahead: the next entry is generated before
	 * next() is called, to make sure that we do indeed have more entries.
	 * This also means that skip() cannot be efficient, as it examines each
	 * and every entry.
	 * 
	 * @class
	 * @name Sincerity.Iterators.Fetcher
	 * @augments Sincerity.Iterators.Iterator
	 * @param {Function} fetchFn A function that receives (options, index) as
	 *        arguments, and must return the next entry or set options.hasNext
	 *        to false
	 * @param [scope] The scope to use for fetchFn and closeFn
	 * @param {Function} [closeFn] An optional close function
	 */
	Public.Fetcher = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Iterators.Fetcher */
	    var Public = {}

	    /** @ignore */
	    Public._inherit = Module.Iterator

	    /** @ignore */
	    Public._construct = function(fetchFn, scope, closeFn) {
	    	this.fetchFn = fetchFn
	    	this.scope = scope
	    	this.closeFn = closeFn
			this.index = 0
			this.options = {hasNext: true}
			this.nextValue = fetchFn.call(this.scope, this.options, 0)
	    }
	    
	    Public.hasNext = function() {
			return this.options.hasNext
		}
		
	    Public.next = function() {
			var value = this.nextValue
			this.nextValue = this.fetchFn.call(this.scope, this.options, ++this.index)
			return value
		}
		
	    Public.close = function() {
			if (this.closeFn) {
				this.closeFn.call(this.scope)
			}
		}
		
		return Public
	}(Public))

	/**
	 * A wrapper that allows transforming entries in an iterator. 
	 * 
	 * @class
	 * @name Sincerity.Iterators.Transformer
	 * @augments Sincerity.Iterators.Iterator 
	 * @param {Sincerity.Iterators.Iterator} iterator The iterator to wrap
	 * @param {Function} transformFn Function that accepts an entry as an argument and should return
	 *        the transformed entry
	 * @param [scope] The scope to use for transformFn
	 */
	Public.Transformer = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Iterators.Transformer */
	    var Public = {}

	    /** @ignore */
	    Public._inherit = Module.Iterator

	    /** @ignore */
	    Public._construct = function(iterator, transformFn, scope) {
	    	this.iterator = iterator
	    	this.transformFn = transformFn
	    	this.scope = scope
	    }
	    
	    Public.hasNext = function() {
			return this.iterator.hasNext()
		}
		
	    Public.next = function() {
			return this.transformFn.call(this.scope, this.iterator.next())
		}
		
	    Public.skip = function(count) {
	    	this.iterator.skip(count)
		}
		
	    Public.skipCount = function(count) {
			if (this.iterator.skipCount) {
				return this.iterator.skipCount(count)
			}
			else {
				var skipped = 0
				while (this.iterator.hasNext() && (count-- > 0)) {
					this.iterator.next()
					skipped++
				}
				return skipped
			}
		}

	    Public.close = function() {
	    	this.iterator.close()
		}
		
		return Public
	}(Public))

	/**
	 * A wrapper that allows eliminating entries from an iterator. 
	 * <p>
	 * The iterator is always one step ahead: the next entry is generated before
	 * next() is called, to make sure that we do indeed have more entries.
	 * This also means that skip() cannot be efficient, as it examines each
	 * and every entry.
	 * 
	 * @class
	 * @name Sincerity.Iterators.Eliminator
	 * @augments Sincerity.Iterators.Iterator 
	 * @param {Sincerity.Iterators.Iterator} iterator The iterator to wrap
	 * @param {Function} testFn Function that receive an entry as an argument and returns
	 *        false if it is to be eliminated
	 * @param [scope] The scope to use for testFn
	 */
	Public.Eliminator = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Iterators.Eliminator */
	    var Public = {}

	    /** @ignore */
	    Public._inherit = Module.Iterator

	    /** @ignore */
	    Public._construct = function(iterator, testFn, scope) {
	    	this.iterator = iterator
	    	this.testFn = testFn
	    	this.scope = scope
			this.nextValue = null
			this.hasNextFlag = testFn ? true : false
			if (testFn) {
				this.next()
			}
	    }
	    
	    Public.hasNext = function() {
			return this.hasNextFlag
		}
		
	    Public.next = function() {
			var value = this.nextValue
			while (true) {
				if (this.iterator.hasNext()) {
					next = this.iterator.next()
					if (this.testFn.call(this.scope, this.nextValue) != false) {
						break
					}
				}
				else {
					this.hasNextFlag = false
					break
				}
			}
			return value
		}
		
	    Public.close = function() {
	    	this.iterator.close()
		}
		
		return Public
	}(Public))
	
	/**
	 * Allows chaining of iterators together in a series. Automatically closes exhausted
	 * iterators before moving on to the next iterator in the chain, and closes
	 * remaining iterators when its own close() is called.
	 * <p>
	 * The performance of {@link #skip} depends on whether the chained iterators support
	 * {@link #skipCount} or not. If they do not, the chain iterator will have to read
	 * entries one by one in order to count them.
	 * 
	 * @class
	 * @name Sincerity.Iterators.Chain
	 * @augments Sincerity.Iterators.Iterator
	 * @param {Sincerity.Iterators.Iterator[]} iterators An array of iterators to chain
	 */
	Public.Chain = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Iterators.Chain */
	    var Public = {}

	    /** @ignore */
	    Public._inherit = Module.Iterator

	    /** @ignore */
	    Public._construct = function(iterators) {
	    	this.iterators = iterators
			this.index = 0
			this.length = iterators.length
			this.iterator = this.length ? iterators[0] : null
	    }
	    
	    Public.hasNext = function() {
			if (!this.iterator) {
				return false
			}
			else if (this.iterator.hasNext()) {
				return true
			}
			else {
				if (++this.index == this.length) {
					this.iterator = null
					return false
				}

				this.iterator.close()
				this.iterator = this.iterators[this.index]
				return this.hasNext()
			}
		}
		
	    Public.next = function() {
			return this.iterator.next()
		}
		
	    Public.skip = Public.skipCount = function(count) {
			if (this.iterator) {
				var skipped = 0
				if (this.iterator.skipCount) {
					skipped = this.iterator.skipCount(count)
					count -= skipped
				}
				else {
					while (this.iterator.hasNext() && (count-- > 0)) {
						this.iterator.next()
						skipped++
					}
				}

				if (count <= 0) {
					return skipped
				}

				if (++this.index == this.length) {
					this.iterator = null
					return skipped
				}

				this.iterator.close()
				this.iterator = this.iterators[this.index]
				return skipped + this.skipCount(count)
			}
			else {
				return 0
			}
		}

	    Public.close = function() {
			for (var i = this.index; i < this.length; i++) {
				this.iterators[i].close()
			}
		}
		
		return Public
	}(Public))
	
	/**
	 * A wrapper that buffers entries from the underlying iterator,
	 * retrieving them in chunks, filling the buffer only when it is
	 * empty. In some cases using a buffer can increase performance
	 * or concurrency.
	 * <p>
	 * The performance of {@link #skip} depends on whether the wrapper iterator supports
	 * {@link #skipCount} or not. If it does not, the buffer iterator may have to read
	 * entries one by one in order to count them, if they have not already been buffered.
	 * 
	 * @class
	 * @name Sincerity.Iterators.Buffer
	 * @augments Sincerity.Iterators.Iterator
	 */
	Public.Buffer = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Iterators.Buffer */
	    var Public = {}

	    /** @ignore */
	    Public._inherit = Module.Iterator

	    /** @ignore */
	    Public._construct = function(iterator, bufferSize) {
	    	this.iterator = iterator
			this.buffer = []
	    }
	    
	    Public.hasNext = function() {
			return (this.buffer.length > 0) || this.iterator.hasNext()
		}
		
	    Public.next = function() {
			var value
			
			if (this.buffer.length) {
				value = this.buffer.shift()
			}
			else {
				if (this.iterator.hasNext()) {
					value = this.iterator.next()
				}
				for (var i = 1; (i < this.bufferSize) && this.iterator.hasNext(); i++) {
					this.buffer.push(this.iterator.next())
				}
			}
			
			return value
		}
		
	    Public.skip = Public.skipCount = function(count) {
			var length = this.buffer.length, skipped = 0
			if (length && count) {
				skipped = Math.min(length, count)
				this.buffer.splice(0, skipped)
				count -= skipped
			}
			
			if (count <= 0) {
				return skipped
			}
			
			if (this.iterator.skipCount) {
				return skipped + this.iterator.skipCount(count)
			}
			else {
				while (this.hasNext() && (count-- > 0)) {
					this.next()
					skipped++
				}
				return skipped
			}
		}
		
	    Public.close = function() {
	    	this.iterator.close()
		}
		
		return Public
	}(Public))
    
    return Public
}()