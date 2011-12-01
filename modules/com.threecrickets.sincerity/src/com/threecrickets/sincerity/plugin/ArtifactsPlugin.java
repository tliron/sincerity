package com.threecrickets.sincerity.plugin;

import java.io.OutputStreamWriter;

import com.threecrickets.sincerity.Dependencies;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.Sincerity;

public class ArtifactsPlugin implements Plugin
{
	//
	// Plugin
	//

	public String getName()
	{
		return "artifacts";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"artifacts", "resolve", "clean", "prune"
		};
	}

	public void run( String command, String[] arguments, Sincerity sincerity ) throws Exception
	{
		Dependencies dependencies = sincerity.getContainer().getDependencies();

		if( "artifacts".equals( command ) )
		{
			dependencies.getResolvedDependencies().printArtifacts( new OutputStreamWriter( System.out ) );
		}
		else if( "clean".equals( command ) )
		{
			dependencies.clean();
		}
		else if( "prune".equals( command ) )
		{
			dependencies.prune();
		}
		else
			throw new Exception( "Unknown command: " + command );
	}
}
