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

/**
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.0
 */
var Sincerity = {}

document.require(
	'/sincerity/annotations/',
	'/sincerity/calendar/',
	'/sincerity/classes/',
	'/sincerity/cryptography/',
	'/sincerity/files/',
	'/sincerity/iterators/',
	'/sincerity/json/',
	'/sincerity/jvm/',
	'/sincerity/localization/',
	'/sincerity/lucene/',
	'/sincerity/mail/',
	'/sincerity/objects/',
	'/sincerity/platform/',
	'/sincerity/templates/',
	'/sincerity/xml/')

/**
 * Provides access to the current Sincerity installation as well as the current container and its plugins.
 * Useful for boostrapping code as well as plugin development.
 * <p>
 * The Scripturian {@link executable#container} value is set to equal this namespace.
 * 
 * @name sincerity
 * @namespace
 * @see <a href="http://threecrickets.com/api/java/sincerity/index.html?com/threecrickets/sincerity/Sincerity.html">The Sincerity API documentation</a>
 */

/**
 * A read-only map of Sincerity's version information.
 * <p>
 * Supported keys are "version" and "built".
 * 
 * @name sincerity.version
 * @type <a href="http://docs.oracle.com/javase/1.5.0/docs/api/index.html?java/util/Map.html">java.util.Map</a>&lt;String, String&gt;
 */

/**
 * The root directory of the Sincerity installation.
 * 
 * @name sincerity.home
 * @type <a href="http://docs.oracle.com/javase/1.5.0/docs/api/index.html?java/io/File.html">java.io.File</a>
 */

/**
 * Shortcut to access the root directory of the current container.
 * <p>
 * Will throw an exception if there is no current container.
 * 
 * @name sincerity.containerRoot
 * @type <a href="http://docs.oracle.com/javase/1.5.0/docs/api/index.html?java/io/File.html">java.io.File</a>
 * @see sincerity#container
 */

/**
 * An integer specifying the current verbosity level. Messages are sent to the current {@link sincerity#out}
 * or {@link sincerity#err}. The default verbosity is 1.
 * <p>
 * Verbosity is interpreted individually by individual commands, though 0 usually means "silent,"
 * 1 means "only important messages" and 2 means "quite chatty." Higher values usually include more minute debugging
 * information.
 * <p>
 * You should always check this value before printing out messages!
 * 
 * @name sincerity.verbosity
 * @type Number
 */

/**
 * The standard output for Sincerity.
 * <p>
 * Note that you can change this value.
 * 
 * @name sincerity.out
 * @type <a href="http://docs.oracle.com/javase/1.5.0/docs/api/index.html?java/io/PrintWriter.html">java.io.PrintWriter</a>
 */

/**
 * The standard error output for Sincerity.
 * <p>
 * Note that you can change this value.
 * 
 * @name sincerity.err
 * @type <a href="http://docs.oracle.com/javase/1.5.0/docs/api/index.html?java/io/PrintWriter.html">java.io.PrintWriter</a>
 */

/**
 * Access to the current container.
 * <p>
 * Will throw an exception if there is no current container.
 * 
 * @name sincerity.container
 * @type <a href="http://threecrickets.com/api/java/sincerity/index.html?com/threecrickets/sincerity/Container.html">com.threeecrickets.sincerity.Container</a>
 * @see sincerity#containerRoot
 */

/**
 * Access to the current container's set of supported plugins. This includes plugins installed
 * in the container as well as the core plugins installed in Sincerity.
 * <p>
 * Will throw an exception if there is no current container.
 * 
 * @name sincerity.plugins
 * @type <a href="http://threecrickets.com/api/java/sincerity/index.html?com/threecrickets/sincerity/Plugins.html">com.threeecrickets.sincerity.Plugins</a>
 */

/**
 * Access to the GUI frame. Useful for GUI plugins.
 * <p>
 * Will be null if the GUI frame is not open. Plugins can check this value for null
 * to determine if they are running in GUI mode.
 * 
 * @name sincerity.frame
 * @type <a href="http://threecrickets.com/api/java/sincerity/index.html?com/threecrickets/sincerity/plugin/gui/Frame.html">com.threeecrickets.sincerity.plugin.gui.Frame</a>
 */

/**
 * Access to the set of container templates supported by this Sincerity installation.
 * 
 * @name sincerity.templates
 * @type <a href="http://docs.oracle.com/javase/1.5.0/docs/api/index.html?java/util/List.html">java.util.List</a>&lt;<a href="http://threecrickets.com/api/java/sincerity/index.html?com/threecrickets/sincerity/Template.html">com.threecrickets.sincerity.Template</a>&gt;
 */

/**
 * Runs arbitrary Sincerity commands.
 * <p>
 * The argument is an arrayof strings, identical to the Sincerity command line. For example:
 * <code>
 * sincerity.run(['add', 'javascript', ':', 'install', ':', 'javascript'])
 * </code>
 * 
 * @name sincerity.run
 * @function
 * @param {String[]} arguments
 */

/**
 * Causes Sincerity to "reboot" and re-run its current command queue.
 * <p>
 * This is useful if you somehow changed aspects that would effect the current
 * bootstrap, such as adding new plugins or installing new libraries.
 * 
 * @name sincerity.reboot
 * @function
 * @param {Boolean} [forceNewBootstrap=false] When true forces Sincerity to reinitialize its class loader 
 */
