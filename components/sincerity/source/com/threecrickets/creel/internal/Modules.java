/**
 * Copyright 2015-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.creel.internal;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import com.threecrickets.creel.Module;
import com.threecrickets.creel.ModuleIdentifier;
import com.threecrickets.creel.ModuleSpecification;

/**
 * @author Tal Liron
 */
public class Modules implements Iterable<Module>
{
	//
	// Attributes
	//

	public int size()
	{
		return modules.size();
	}

	//
	// Operations
	//

	public Module get( ModuleIdentifier moduleIdentifier )
	{
		lock.lock();
		try
		{
			for( Module module : modules )
				if( moduleIdentifier.equals( module.getIdentifier() ) )
					return module;
		}
		finally
		{
			lock.unlock();
		}
		return null;
	}

	public Module get( ModuleSpecification moduleSpecification )
	{
		lock.lock();
		try
		{
			for( Module module : modules )
				if( moduleSpecification.equals( module.getSpecification() ) )
					return module;
		}
		finally
		{
			lock.unlock();
		}
		return null;
	}

	public void addByIdentifier( Module module )
	{
		lock.lock();
		try
		{
			boolean found = false;
			for( Module aModule : modules )
				if( module.getIdentifier().equals( aModule.getIdentifier() ) )
				{
					aModule.mergeSupplicants( module );
					found = true;
					break;
				}
			if( !found )
				modules.add( module );
		}
		finally
		{
			lock.unlock();
		}
	}

	public void addBySpecification( Module module )
	{
		lock.lock();
		try
		{
			boolean found = false;
			for( Module aModule : modules )
				if( module.getSpecification().equals( aModule.getSpecification() ) )
				{
					aModule.mergeSupplicants( module );
					found = true;
					break;
				}
			if( !found )
				modules.add( module );
		}
		finally
		{
			lock.unlock();
		}
	}

	public void remove( ModuleIdentifier moduleIdentifier )
	{
		lock.lock();
		try
		{
			for( Module module : modules )
				if( moduleIdentifier.equals( module.getIdentifier() ) )
				{
					modules.remove( module );
					break;
				}
		}
		finally
		{
			lock.unlock();
		}
	}

	public void remove( ModuleSpecification moduleSpecification )
	{
		lock.lock();
		try
		{
			for( Module module : modules )
				if( moduleSpecification.equals( module.getSpecification() ) )
				{
					modules.remove( module );
					break;
				}
		}
		finally
		{
			lock.unlock();
		}
	}

	public void sortByIdentifiers()
	{
		Collections.sort( modules, ModuleIdentifierComparator.INSTANCE );
	}

	public void sortBySpecifications()
	{
		Collections.sort( modules, ModuleSpecificationComparator.INSTANCE );
	}

	//
	// Iterable
	//

	@Override
	public Iterator<Module> iterator()
	{
		return modules.iterator();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final List<Module> modules = new CopyOnWriteArrayList<Module>();

	private final ReentrantLock lock = new ReentrantLock();
}
