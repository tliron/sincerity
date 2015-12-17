package com.threecrickets.creel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * A module can have dependencies as well as supplicants.
 * 
 * @author Tal Liron
 */
public class Module
{
	//
	// Construction
	//

	public Module( boolean explicit, ModuleIdentifier identifier, ModuleSpecification specification )
	{
		this.explicit = explicit;
		this.identifier = identifier;
		this.specification = specification;
	}

	//
	// Attributes
	//

	public boolean isExplicit()
	{
		return explicit;
	}

	public void setExplicit( boolean explicit )
	{
		this.explicit = explicit;
	}

	public ModuleIdentifier getIdentifier()
	{
		return identifier;
	}

	public ModuleSpecification getSpecification()
	{
		return specification;
	}

	public Iterable<Module> getDependencies()
	{
		return Collections.unmodifiableCollection( dependencies );
	}

	public Iterable<Module> getSupplicants()
	{
		return Collections.unmodifiableCollection( supplicants );
	}

	//
	// Operations
	//

	public void addDependency( Module dependency )
	{
		dependencies.add( dependency );
	}

	/**
	 * Adds a new supplicant if we don't have it already.
	 * 
	 * @param supplicant
	 */
	public void addSupplicant( Module supplicant )
	{
	}

	/**
	 * Removes a supplicant if we have it.
	 * 
	 * @param supplicant
	 */
	public void removeSupplicant( Module supplicant )
	{
	}

	/**
	 * Copies identifier, repository, and dependencies from another module.
	 */
	public void copyResolutionFrom()
	{
	}

	/**
	 * Adds all supplicants of another module, and makes us explicit if the
	 * other module is explicit.
	 * 
	 * @param module
	 */
	public void merge( Module module )
	{
	}

	public void replaceModule( Module oldModule, Module newModule, boolean recursive )
	{
	}

	public String toString( boolean longForm )
	{
		StringBuilder r = new StringBuilder(), prefix = new StringBuilder();
		if( identifier != null )
		{
			r.append( "id=" );
			r.append( identifier );
		}
		if( ( longForm || !( identifier == null ) ) && ( specification != null ) )
		{
			if( r.length() != 0 )
				r.append( ", " );
			r.append( "spec=" );
			r.append( specification );
		}
		if( longForm )
		{
			prefix.append( this.explicit ? '*' : '+' ); // explicit?
			prefix.append( this.identifier != null ? '!' : '?' ); // identified?
			if( !dependencies.isEmpty() )
			{
				if( r.length() != 0 )
					r.append( ", " );
				r.append( "dependencies=" );
				r.append( dependencies.size() );
			}
			if( !supplicants.isEmpty() )
			{
				if( r.length() != 0 )
					r.append( ", " );
				r.append( "supplicants=" );
				r.append( supplicants.size() );
			}
		}
		if( prefix.length() != 0 )
		{
			r.insert( 0, ' ' );
			r.insert( 0, prefix );
		}
		return r.toString();
	}

	//
	// Objects
	//

	@Override
	public String toString()
	{
		return toString( true );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private boolean explicit;

	private ModuleIdentifier identifier;

	private ModuleSpecification specification;

	private final Collection<Module> dependencies = new ArrayList<Module>();

	private final Collection<Module> supplicants = new ArrayList<Module>();
}
