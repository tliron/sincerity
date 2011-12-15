package rest;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import java.util.Date;

public class DefaultResource extends ServerResource
{
	@Get
	public String represent()
	{
		return "This content is was dynamically generated at " + new Date();
	}
}