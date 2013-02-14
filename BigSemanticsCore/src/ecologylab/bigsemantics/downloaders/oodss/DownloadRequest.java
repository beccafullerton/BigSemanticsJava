/**
 * 
 */
package ecologylab.bigsemantics.downloaders.oodss;

import java.io.IOException;

import ecologylab.bigsemantics.downloaders.NetworkDocumentDownloader;
import ecologylab.collections.Scope;
import ecologylab.net.ParsedURL;
import ecologylab.oodss.messages.RequestMessage;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * Request message for dowanloading the document over network
 * 
 * @author ajit
 * 
 */

public class DownloadRequest extends RequestMessage
{

	@simpl_scalar
	ParsedURL	location;

	@simpl_scalar
	String		userAgentString;

	public DownloadRequest()
	{
	}

	public DownloadRequest(ParsedURL location, String userAgentString)
	{
		this.location = location;
		this.userAgentString = userAgentString;
	}

	@Override
	public DownloadResponse performService(Scope clientSessionScope)
	{
		debug("document download url: " + this.location);
		long millis = System.currentTimeMillis();

		NetworkDocumentDownloader documentDownloader = new NetworkDocumentDownloader(location,
				userAgentString);
		// boolean bChanged = false;
		ParsedURL redirectedLocation = null;
		String location = null;
		String mimeType = null;
		try
		{
			documentDownloader.connect(true);
			// additional location
			redirectedLocation = documentDownloader.getRedirectedLocation();
			// local saved location
			location = documentDownloader.getLocalLocation();
			// mimeType
			mimeType = documentDownloader.mimeType();
		}
		catch (IOException e)
		{
		  error("Cannot download url: " + this.location + " because of error: " + e.getMessage());
			e.printStackTrace();
		}

		debug("document from url: " + this.location + " downloaded to: " + location + " in total "
				+ (System.currentTimeMillis() - millis) + "ms");
		return new DownloadResponse(redirectedLocation, location, mimeType);
	}
}
