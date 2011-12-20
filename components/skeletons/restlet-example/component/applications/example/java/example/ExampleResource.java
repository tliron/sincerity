package example;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import java.util.Date;

public class ExampleResource extends ServerResource
{
	@Get("html")
	public String represent()
	{
		return "<html><body>This content was dynamically generated at " + new Date() + "</body></html>";
	}
}