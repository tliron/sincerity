package com.threecrickets.sincerity.ivy.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.plugins.parser.ModuleDescriptorParser;
import org.apache.ivy.plugins.parser.ParserSettings;
import org.apache.ivy.plugins.repository.Resource;

public class PyPiModuleDescriptorParser implements ModuleDescriptorParser
{
	public ModuleDescriptor parseDescriptor( ParserSettings ivySettings, URL descriptorURL, boolean validate ) throws ParseException, IOException
	{
		System.out.println( "parseDescriptor " + descriptorURL );
		return null;
	}

	public ModuleDescriptor parseDescriptor( ParserSettings ivySettings, URL descriptorURL, Resource res, boolean validate ) throws ParseException, IOException
	{
		System.out.println( "parseDescriptor " + res );
		return null;
	}

	public void toIvyFile( InputStream is, Resource res, File destFile, ModuleDescriptor md ) throws ParseException, IOException
	{
		System.out.println( "toIvyFile " + res );
	}

	public boolean accept( Resource res )
	{
		System.out.println( "accept? " + res );
		return res.getName().equals( "python" );
	}

	public String getType()
	{
		return "python";
	}

	public Artifact getMetadataArtifact( ModuleRevisionId mrid, Resource res )
	{
		System.out.println( "getMetadataArtifact " + res );
		// Object reference = ( (ReferenceResource) res ).getReference();
		return new DefaultArtifact( mrid, new Date( res.getLastModified() ), mrid.getName(), "python", "python", true );
	}
}