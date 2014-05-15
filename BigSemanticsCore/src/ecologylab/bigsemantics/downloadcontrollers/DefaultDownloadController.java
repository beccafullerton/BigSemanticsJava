package ecologylab.bigsemantics.downloadcontrollers;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.net.ParsedURL;

/**
 * The default class that handles document downloading.
 * 
 * @author colton
 */
public class DefaultDownloadController extends AbstractDownloadController
{

  static Logger             logger;

  static
  {
    logger = LoggerFactory.getLogger(DefaultDownloadController.class);
  }

  public static final int   MAX_REDIRECTS = 10;

  private boolean           successStatus;

  private int               httpStatus;

  private String            charset;

  private String            mimeType;

  private String            userAgent;

  private String            httpStatusMessage;

  private ParsedURL         originalLocation;

  private List<ParsedURL>   redirectedLocations;

  private HttpURLConnection connection;

  /**
   * Opens the HttpURLConnection to the specified location and downloads the resource
   * 
   * @param location
   *          a ParsedURL object pointing to a resource
   * @return a boolean indicating the success status of the connection
   */
  public boolean accessAndDownload(ParsedURL location) throws IOException
  {

    originalLocation = location;

    try
    {
      connection = (HttpURLConnection) originalLocation.url().openConnection();

      // Attempt to follow redirects until redirect limit is reached
      int redirects = 0;
      Set<String> redirectedLocs = new HashSet<String>();
      while (true)
      {
        if (userAgent != null)
        {
          connection.setRequestProperty("User-Agent", userAgent);
        }
        httpStatus = connection.getResponseCode();

        if (isRedirecting(httpStatus) && redirects <= MAX_REDIRECTS)
        {
          String redirectedLoc = connection.getHeaderField("Location");
          addRedirectedLocation(redirectedLocs, redirectedLoc);
          connection = (HttpURLConnection) new URL(redirectedLoc).openConnection();
        }
        else
        {
          break;
        }
      }
      if (redirectedLocs.size() > 0)
      {
        for (String loc : redirectedLocs)
        {
          redirectedLocations().add(ParsedURL.getAbsolute(loc));
        }
      }

      if (connection.getContentType() != null)
      {
        String[] contentType = connection.getContentType().split(";");

        mimeType = contentType[0];
        charset = contentType.length > 1
            ? contentType[1].trim().substring("charset=".length())
            : null;
      }

      successStatus = (200 <= httpStatus && httpStatus < 300);

      if (successStatus)
      {
        InputStream contentStream = connection.getInputStream();
        this.setInputStream(contentStream);
        httpStatusMessage = connection.getResponseMessage();
      }
    }
    catch (MalformedURLException e)
    {
      logger.error("Error connecting to " + location, e);
      successStatus = false;
    }

    return successStatus;
  }

  private boolean isRedirecting(int httpStatus)
  {
    return httpStatus == HttpURLConnection.HTTP_MOVED_PERM
        || httpStatus == HttpURLConnection.HTTP_MOVED_TEMP;
  }

  /**
   * Sets the user agent
   * 
   * @param userAgent
   *          a string representation of the user agent
   */
  public void setUserAgent(String userAgent)
  {
    this.userAgent = userAgent;
  }

  /**
   * Returns a boolean indicating if the HTTP response code is that of a good connection
   * 
   * @return a boolean indicating if the HTTP response code is that of a good connection
   */
  public boolean isGood()
  {
    return successStatus;
  }

  /**
   * Returns the status code of the HTTP response message for the connection
   * 
   * @return the status code of the HTTP response message for the connection
   */
  public int getStatus()
  {
    return httpStatus;
  }

  /**
   * Returns the message from the HTTP response message
   * 
   * @return the message from the HTTP response message
   */
  public String getStatusMessage()
  {
    return httpStatusMessage;
  }

  /**
   * Returns a ParsedURL object corresponding to the original resource location used to initiate the
   * connection. This value does not change if the connection is redirected
   * 
   * @return a ParsedURL object corresponding to the original resource location used to initiate the
   *         connection
   */
  public ParsedURL getLocation()
  {
    return originalLocation;
  }

  private List<ParsedURL> redirectedLocations()
  {
    if (redirectedLocations == null)
    {
      redirectedLocations = new ArrayList<ParsedURL>();
    }
    return redirectedLocations;
  }

  /**
   * Returns a ParsedURL object corresponding to the location of the resource with which the
   * connection is associated. This value does change with redirects
   * 
   * @return a ParsedURL object corresponding to the location of the resource with which the
   *         connection is associated
   */
  public List<ParsedURL> getRedirectedLocations()
  {
    return redirectedLocations;
  }

  private void addRedirectedLocation(Set<String> redirectedLocs, String redirectedLoc)
  {
    if (redirectedLoc != null && !redirectedLoc.equals(originalLocation.toString()))
    {
      redirectedLocs.add(redirectedLoc);
    }
  }

  /**
   * Returns the String representation of the content type
   * 
   * @return the String representation of the content type
   */
  public String getMimeType()
  {
    return mimeType;
  }

  /**
   * Returns the content encoding type (character set)
   * 
   * @return a String representation of the content encoding type (character set)
   */
  public String getCharset()
  {
    return charset;
  }

  /**
   * Returns the content of the named header field
   * 
   * @param name
   *          the name of the requested header field
   * @return a String of the content of the named header field
   */
  public String getHeader(String name)
  {
    return connection.getHeaderField(name);
  }

}
