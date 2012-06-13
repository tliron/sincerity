//
// This file is part of the Sincerity Foundation Library
//

// Copyright 2011-2012 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

document.executeOnce('/sincerity/objects/')

var Sincerity = Sincerity || {}

/**
 * Object Oriented Programming for JavaScript, Sincerity-style.
 * <p>
 * Though JavaScript does not in itself have most OOP constructs, it provides all the essentials for
 * building an OOP framework. And this is Sincerity's take on it: we've opted for an especially lightweight
 * approach to provide the bare minimum required for an OOP development experience that promotes
 * clarity, consistency and functionality. If you've come from heavily OOP languages, such as Java or
 * Python, we'd like to encourage you to avoid automatically turning everything into a "class," and 
 * instead and choose more horizontal ways of code organization and reuse, via closures, raw prototypes
 * and duck typing. Use OOP only when it adds coherence to your code.
 * <p>
 * That said, Sincerity supports all OOP principles:
 * <ol>
 * <li>Encapsulation: This is already available, because functions are first-class citizens in JavaScript.
 * A dict can contain both data properties and functions. Additionally, the "this" keyword is automatically
 * linked to a new dict via the "new" keyword. The result is that data and functions are encapsulated
 * into a single instance.</li>
 * <li>Inheritance: There are two obvious ways to implement this in JavaScript -- by simply copying functions
 * over from another dict (which we could have done with {@link Sincerity.Objects#merge}) or at once by
 * setting the constructor's prototype. The latter offers better performance and more coherence, which is
 * why we chose it. Additionally, for all methods, we hook up the overridden member via the ".overridden"
 * field in the function object itself. You can access it from inside the method as
 * "arguments.callee.overridden". Note that this also works for the "._construct" constructor.</li>
 * <li>Polymorphism: Loose polymorphism is provided naturally via JavaScript's duck typing, which is
 * heavily used outside of OOP proper. What we add here is basic type information: the "instanceof"
 * keyword can be used to identify which constructor was used for an instance, and additionally returns
 * true if the instance is of a sub-class (by following the path of prototypes).</li>
 * </ol>
 * A fourth mini-principle is also sometimes mentioned in discussions of OOP: information hiding.
 * JavaScript's closures allow for a simple way to maintain scope in the instance without exposing it.
 * <p>
 * Another useful feature we support, which has become popular in many OOP implementations, is the ability
 * to annotate classes. Any key in the class definition that begins with "_" is not copied to prototype,
 * but can be accessed via the prototype's "definition" key. 
 * <p>
 * An example, with the module pattern:
 * <p>
 * (Note that this pattern does not let you create private properties that are <i>not</i> static.
 * Some would argue that this is no great loss, because too much information hiding can be
 * a bad practice. )
 * <pre>
 * // We are not providing a literal dict, but instead creating an "ad hoc" anonymous function,
 * // which we then immediately call, so that it only ever used this once for purposes of closure
 * var Person = Sincerity.Classes.define(function() {
 *   // We will be returning this dict to the outside world
 *   var Public = {}
 *   
 *   // This following var is private, because we're not putting it in "Public";
 *   // Note that it is also static, in that it will be shared between all instances
 *   // of the class!
 *   var privateName = 'Tal'
 *   
 *   // Constructor annotation
 *   Public._construct = function(pet) {
 *     // This will be a public property
 *     this.pet = pet
 *     
 *     // Let's call the parent's constructor
 *     arguments.callee.overridden.call(this)
 *   }
 *   
 *   // Inheritance annotation
 *   Public._inherit = 'MyParentClass'
 *   
 *   // A custom annotation, which may be used by other systems in our application
 *   Public._saveToDatabase = 'people'
 *   
 *   // A public method
 *   Public.getName = function() {
 *     return privateName
 *   }
 *   
 *   // Another public method
 *   Public.getFriends = function(friend) {
 *     return makeFriends(privateName, friend)
 *   }
 *   
 *   // And another public method
 *   Public.getFriendsWithPet = function(friend) {
 *     // Note that unlike the previous method call, here we have to explicitly use "call"
 *     // in order to pass "this" to the method
 *     return makeFriendsWithPet.call(this, privateName, friend)
 *   }
 *   
 *   // A private method that only accesses private properties
 *   function makeFriends(friend1, friend2) {
 *     return friend1 + ' and ' + friend2
 *   }
 *   
 *   // Another private method; this one needs to access public properties
 *   function makeFriendsWithPet(friend1, friend2) {
 *     // Note the need to reference "this" here -- it must be provided when this method is called
 *     // (We could have also provided the the "this" reference as a simple argument to the method,
 *     // but using "this" keeps the code more coherent)
 *     return friend1 + ' and ' + friend2 + ' with ' + this.pet
 *   }
 *   
 *   // We are returning the dict we called "Public" to the outside world;
 *   // All the private stuff will stay in this closure!
 *   return Public
 * }()) // Note that the ad-hoc function is being called here!
 * </pre>
 * 
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.0
 */
Sincerity.Classes = Sincerity.Classes || function() {
	/** @exports Public as Sincerity.Classes */
    var Public = {}
    
    /**
     * Implements OOP inheritance by using an instance of the parent class
     * as the prototype of the child class. Additionally, implements an aspect
     * of OOP polymorphism by allowing JavaScript's "instanceof" keyword to
     * recognize the class hierarchy.
     * <p>
     * The child's prototype will have a "superclass" property added to it, pointing
     * to the parent class, to allow for dynamic backward traversal of the class
     * hierarchy. 
     * 
     *  @param {Function} Parent The parent constructor
     *  @param {Function} Child The child constructor
     */
    Public.inherit = function(Parent, Child) {
    	// A no-op constructor
    	var Intermediate = function() {}
    	
    	// ...with the same prototype as Parent
    	Intermediate.prototype = Parent.prototype
    	
    	// Inherit!
    	Child.prototype = new Intermediate()

    	// Make sure instanceof will work (it compares constructors)
    	Child.prototype.constructor = Child

    	// Access to parent
    	Child.prototype.superclass = Parent
    }
    
    /**
     * Convenient initialization of class constructors and their prototypes using a simple class
     * definition dict, with support for inheritance and polymorphism.
     * <p>
     * Any dict key that does <i>not</i> begin with a "_" will be copied to the class prototype.
     * Those that <i>do</i> begin with a "_" are considered "annotations". The entire class
     * definition remains stored as a "definition" property in the class prototype, so that you can
     * access these annotations at runtime.
     * <p>
     * A few optional annotations are used here: "_construct", "_inherit", "_configure" and "_configureOnly".
     * 
     * @param definition
     * @param {Function} [definition._construct] Becomes the class constructor (if not provided, a default no-op constructor is used)
     * @param {String} [definition._inherit] The name of the class from which we will {@link #inherit}
     * @returns The class constructor, with the prototype properly initialized
     */
    Public.define = function(definition) {
    	var TheClass
    	
    	if (definition._construct) {
    		if (typeof definition._construct != 'function') {
    			throw 'Constructor must be a function'
    		}
    		TheClass = definition._construct
    	}
    	else {
    		// Default no-op constructor
    		TheClass = function() {}
    	}
    	
    	if (definition._configureOnly) {
    		definition._configure = definition._configureOnly
    		delete definition._configureOnly
    	}
    	else if (definition._inherit) {
    		// Inherit the parent _configure if it exists
			var parentConfigure = definition._inherit.prototype.definition._configure
    		if (parentConfigure) {
    			definition._configure = Sincerity.Objects.concatUnique(definition._configure || [], parentConfigure)
    		}
    	}
    	
    	if (definition._configure) {
    		// Wrap the constructor with one that merges the configuration
    		var OriginalClass = TheClass
    		TheClass = function(config) {
    			Sincerity.Objects.merge(this, config, TheClass.prototype.definition._configure)
    			OriginalClass.call(this, this)
    		}
    	}

    	if (definition._inherit) {
    		Public.inherit(definition._inherit, TheClass)
    	}
    	else {
        	// Make sure instanceof will work (it compares constructors)
        	TheClass.prototype.constructor = TheClass
    		
    		// TODO: do we need to hook this up to the external class var somehow?!?!?!
    	}
    	
    	// Access to original definition
    	TheClass.prototype.definition = definition
    	
    	// Access to overridden constructor
    	if (definition._construct && definition._inherit) {
    		definition._construct.overridden = definition._inherit
    	}

    	// Public members go in the prototype (overriding inherited members of the same name)
    	for (var c in definition) {
    		if (c[0] != '_') {
    			var member = definition[c]
    			
    			// Access to overridden member for functions
    			if ((typeof member == 'function') && definition._inherit && definition._inherit.prototype[c]) {
    				member.overridden = definition._inherit.prototype[c]
    			}
    			
    			TheClass.prototype[c] = member
    		}
    	}
    	
    	return TheClass
    }
	
    return Public
}()