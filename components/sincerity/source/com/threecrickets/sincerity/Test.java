package com.threecrickets.sincerity;

import java.io.File;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.cache.DefaultRepositoryCacheManager;
import org.apache.ivy.core.cache.DefaultResolutionCacheManager;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.ParsingContext;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentFileSource;

public class Test
{
	public static void python() throws Exception
	{
		DocumentFileSource<Executable> source = new DocumentFileSource<Executable>( new File( "python" ), "default", ".py", 1000 );
		ParsingContext parsing = new ParsingContext();
		parsing.setLanguageManager( new LanguageManager() );
		parsing.setDefaultLanguageTag( "python" );
		parsing.setDocumentSource( source );
		DocumentDescriptor<Executable> document = Executable.createOnce( "/runpip/", false, parsing );
		ExecutionContext execution = new ExecutionContext();
		document.getDocument().makeEnterable( "", execution, null, null );
	}

	public static void main( String[] argv ) throws Exception
	{
		System.out.println( "Ivy " + Ivy.getIvyVersion() + " - " + Ivy.getIvyDate() + " :: " + Ivy.getIvyHomeURL() );

		// Settings
		Ivy ivy = Ivy.newInstance();
		ivy.configureDefault();
		IvySettings settings = ivy.getSettings();
		ivy.getLoggerEngine().pushLogger( new DefaultMessageLogger( Message.MSG_DEBUG ) );

		// Cache
		File cache = new File( "libraries" );
		cache.mkdirs();
		settings.setDefaultCache( cache );
		DefaultRepositoryCacheManager repositoryCacheManager = (DefaultRepositoryCacheManager) settings.getRepositoryCacheManager( "default-cache" );
		DefaultResolutionCacheManager resolutionCacheManager = (DefaultResolutionCacheManager) settings.getResolutionCacheManager();
		// "[organisation]/[module](/[branch])/[type]s/[artifact]-[revision](-[classifier])(.[ext])"
		settings.setDefaultCacheArtifactPattern( "[type]s/[artifact](.[ext])" );
		// "[organisation]/[module](/[branch])/ivy-[revision].xml"
		settings.setDefaultCacheIvyPattern( "meta/[module](/[branch])/[revision]/ivy.xml" );
		// "[organisation]/[module](/[branch])/ivydata-[revision].properties"
		repositoryCacheManager.setDataFilePattern( "meta/[module](/[branch])/[revision]/ivy.properties" );
		// "resolved-[organisation]-[module]-[revision].xml"
		resolutionCacheManager.setResolvedIvyPattern( "meta/ivy.xml" );
		// "resolved-[organisation]-[module]-[revision].properties"
		resolutionCacheManager.setResolvedIvyPropertiesPattern( "meta/ivy.properties" );

		// for (RepositoryCacheManager
		// repositoryCacheManager:settings.getRepositoryCacheManagers())
		// System.out.println(repositoryCacheManager);

		// Module
		DefaultModuleDescriptor module = DefaultModuleDescriptor.newDefaultInstance( ModuleRevisionId.newInstance( "Three Crickets", "PackPack", "working" ) );

		// Dependencies
		String organization = "commons-lang";
		String name = "commons-lang";
		String revision = "2.0";
		DefaultDependencyDescriptor dependency = new DefaultDependencyDescriptor( module, ModuleRevisionId.newInstance( organization, name, revision ), false, false, true );
		dependency.addDependencyConfiguration( "default", "*" );
		module.addDependency( dependency );

		// Create Ivy file
		// File ivyFile = new File( work, "ivy.xml" );
		// XmlModuleDescriptorWriter.write( module, ivyFile );

		// Resolve
		ResolveOptions resolveOptions = new ResolveOptions();
		resolveOptions.setConfs( new String[]
		{
			"default"
		} );
		resolveOptions.setOutputReport( false );
		ivy.resolve( module, resolveOptions );

		// Retrieve
		// String retrievePattern = "/lib/[conf]/[artifact].[ext]";
		// RetrieveOptions retrieveOptions = new RetrieveOptions();
		// ivy.retrieve( module.getModuleRevisionId(), retrievePattern,
		// retrieveOptions );

		// Resolve report
		/*
		 * ResolutionCacheManager cacheMgr = ivy.getResolutionCacheManager();
		 * String resolveId = ResolveOptions.getDefaultResolveId( module ); File
		 * resolverReport = cacheMgr.getConfigurationResolveReportInCache(
		 * resolveId, "default" ); XmlReportParser parser = new
		 * XmlReportParser(); parser.parse( resolverReport );
		 * ArtifactDownloadReport[] downloadReports =
		 * parser.getArtifactReports(); for( ArtifactDownloadReport
		 * downloadReport : downloadReports ) { if(
		 * downloadReport.getLocalFile() != null ) { System.out.println(
		 * downloadReport.getLocalFile().toURI().toURL() ); } }
		 */

		System.out.println( "PackPack!" );
	}
}
