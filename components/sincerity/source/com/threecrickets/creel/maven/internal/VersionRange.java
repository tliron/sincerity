package com.threecrickets.creel.maven.internal;

public class VersionRange
{
	//
	// Construction
	//

	public VersionRange( String start, String end, boolean includeStart, boolean includeEnd )
	{
		this.start = ( ( start != null ) && !start.isEmpty() ) ? new Version( start ) : null;
		this.end = ( ( end != null ) && !end.isEmpty() ) ? new Version( end ) : null;
		this.includeStart = includeStart;
		this.includeEnd = includeEnd;
	}

	public VersionRange( Version start, Version end, boolean includeStart, boolean includeEnd )
	{
		this.start = start;
		this.end = end;
		this.includeStart = includeStart;
		this.includeEnd = includeEnd;
	}

	//
	// Operations
	//

	public boolean allows( Version version )
	{
		int compareStart = start != null ? version.compareTo( start ) : 1;
		int compareEnd = end != null ? end.compareTo( version ) : 1;

		if( includeStart && includeEnd )
			return ( compareStart >= 0 ) && ( compareEnd >= 0 );
		else if( includeStart && !includeEnd )
			return ( compareStart >= 0 ) && ( compareEnd > 0 );
		else if( !includeStart && includeEnd )
			return ( compareStart > 0 ) && ( compareEnd >= 0 );
		else
			return ( compareStart > 0 ) && ( compareEnd > 0 );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Version start;

	private final Version end;

	private final boolean includeStart;

	private final boolean includeEnd;
}
