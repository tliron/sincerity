package com.threecrickets.sincerity;

public class TestSincerity
{
	//
	// Main
	//

	public static void main( String[] argv ) throws Exception
	{
		Container container = new Container();
		//container.getRepositories().addIbiblioRepository( "public", "threecrickets", "http://localhost:8081/nexus/content/repositories/sincerity" );
		//container.getRepositories().addIbiblioRepository( "public", "restlet", "http://maven.restlet.org" );
		//container.getRepositories().addIbiblioRepository( "public", "clojars", "http://clojars.org/repo" );
		//container.getRepositories().addPyPiRepository( "public", "pypi", "http://pypi.python.org/simple" );
		//container.getDependencies().add( "commons-lang", "commons-lang", "2.0" );
		//container.getDependencies().add( "org.restlet.jse", "org.restlet", "2.0.0" );
		//container.getDependencies().add( "overtone", "overtone", "[0.5.0,)" );
		// container.getDependencies().add( "python", "BeautifulSoup", "3.2.0"
		// );
		// container.getDependencies().add( "python", "human_curl", "0.0.3" );
		// container.getDependencies().add( "python", "Pygments", "1.4" );
		//container.getDependencies().add( "python", "gtkeggdeps", "0.0.7" );
		//container.getDependencies().add( "python", "Sphinx", "0.6.6" );
		//container.getDependencies().add( "com.threecrickets", "com.threecrickets.scripturian", "1.0.0" );
		//container.getDependencies().add( "com.threecrickets.scripturian", "org.mozilla.javascript", "1.7R2" );
		//container.getDependencies().add( "com.threecrickets.prudence", "flavor-clojure", "1.1.0" );
		container.getDependencies().install(false);
	}
}
