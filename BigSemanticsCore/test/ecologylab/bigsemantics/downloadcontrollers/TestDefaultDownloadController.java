package ecologylab.bigsemantics.downloadcontrollers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import ecologylab.bigsemantics.downloadcontrollers.DefaultDownloadController;
import ecologylab.bigsemantics.httpclient.SimplHttpResponse;
import ecologylab.net.ParsedURL;

/**
 * JUnit tests for the NewDefaultDownloadController class
 * 
 * @author colton
 */
public class TestDefaultDownloadController
{
  // This address should return both a mime type and char set
  // If this server does not return both, the test will fail
  private final String              GOOD_ADDRESS_NO_REDIRECT     = "https://www.google.com";

  // http://www.fb.com -> https://www.facebook.com/
  private final String              GOOD_ADDRESS_ONE_REDIRECT    = "http://www.fb.com";

  // This address redirects twice
  // http://tiny.cc/kghczw -> http://www.fb.com -> https://www.facebook.com/
  private final String              GOOD_ADDRESS_MULT_REDIRECT   = "http://tiny.cc/kghczw";

  private final String              MULT_REDIRECT_EXPECTED_FINAL = "https://www.facebook.com/";

  private final String              BAD_ADDRESS                  = "http://www.badurl781471099989943.org";

  private DefaultDownloadController controller;

  @Before
  public void setUp() throws Exception
  {
    controller = new DefaultDownloadController();
  }

  @Test
  public void testNewDefaultDownloadController()
  {
    assertFalse("connection is bad without URL", controller.isGood());
  }

  @Test
  public void testAccessAndDownload_GoodNoRedirect() throws MalformedURLException
  {
    URL url = new URL(GOOD_ADDRESS_NO_REDIRECT);
    ParsedURL purl = new ParsedURL(url);

    try
    {
      assertTrue("can connect to known-good url", controller.accessAndDownload(purl));
    }
    catch (IOException e)
    {
      fail("a connection error occured");
    }
  }

  @Test
  public void testAccessAndDownload_GoodOneRedirect() throws MalformedURLException
  {
    URL url = new URL(GOOD_ADDRESS_ONE_REDIRECT);
    ParsedURL purl = new ParsedURL(url);

    try
    {
      assertTrue("can connect to known-good url with single redirect",
                 controller.accessAndDownload(purl));
    }
    catch (IOException e)
    {
      fail("a connection error occured");
    }

  }

  @Test
  public void testAccessAndDownload_GoodMultRedirect() throws MalformedURLException
  {
    URL url = new URL(GOOD_ADDRESS_MULT_REDIRECT);
    ParsedURL purl = new ParsedURL(url);

    try
    {
      assertTrue("can connect to known-good url with multiple redirect",
                 controller.accessAndDownload(purl));
    }
    catch (IOException e)
    {
      fail("a connection error occured");
    }
  }

  @Test
  public void testAccessAndDownload_Bad() throws MalformedURLException
  {
    boolean exceptionThrown = false;
    URL url = new URL(BAD_ADDRESS);
    ParsedURL purl = new ParsedURL(url);

    try
    {
      // Connection should be indicated as bad (blocked) or an exception should be thrown

      assertFalse("bad connection is indicated as such", controller.accessAndDownload(purl));

    }
    catch (IOException e)
    {
      // Being here is expected
      exceptionThrown = true;
    }

    assertTrue("An IOException was correctly thrown", exceptionThrown);
  }

  @Test
  public void testSetUserAgent() throws MalformedURLException
  {
    String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.16 Safari/534.24";
    URL url = new URL(GOOD_ADDRESS_NO_REDIRECT);
    ParsedURL purl = new ParsedURL(url);

    controller.setUserAgent(userAgent);

    try
    {
      assertTrue("can connect to known-good url", controller.accessAndDownload(purl));
    }
    catch (IOException e)
    {
      fail("a connection error occured");
    }
    assertTrue("connection status is good", controller.isGood());
  }

  @Test
  public void testIsGood_GoodSite() throws MalformedURLException
  {
    URL url = new URL(GOOD_ADDRESS_NO_REDIRECT);
    ParsedURL purl = new ParsedURL(url);

    try
    {
      assertTrue("can connect to known-good url", controller.accessAndDownload(purl));
    }
    catch (IOException e)
    {
      fail("a connection error occured");
    }

    SimplHttpResponse resp = controller.getHttpResponse();
    assertEquals("connection status is good",
                 controller.isGood(),
                 (resp.getCode() >= 200 && resp.getCode() < 300));
  }

  @Test
  public void testIsGood_BadSite() throws MalformedURLException
  {
    URL url = new URL(BAD_ADDRESS);
    ParsedURL purl = new ParsedURL(url);

    try
    {
      controller.accessAndDownload(purl);
    }
    catch (IOException e)
    {
    }
    finally
    {
      assertFalse("connection status is bad", controller.isGood());
    }
  }

  @Test
  public void testGetStatus() throws MalformedURLException
  {
    URL url = new URL(GOOD_ADDRESS_NO_REDIRECT);
    ParsedURL purl = new ParsedURL(url);

    try
    {
      assertTrue("can connect to known-good url", controller.accessAndDownload(purl));
    }
    catch (IOException e)
    {
      fail("connection failed");
    }

    SimplHttpResponse resp = controller.getHttpResponse();
    assertTrue("getStatus returns a valid HTTP status code",
               (resp.getCode() >= 100 && resp.getCode() < 600));
  }

  @Test
  public void testGetStatusMessage() throws MalformedURLException
  {
    URL url = new URL(GOOD_ADDRESS_NO_REDIRECT);
    ParsedURL purl = new ParsedURL(url);

    try
    {
      assertTrue("can connect to known-good url", controller.accessAndDownload(purl));
    }
    catch (IOException e)
    {
      fail("connection failed");
    }

    SimplHttpResponse resp = controller.getHttpResponse();
    assertTrue("getStatus returns a message", resp.getMessage() != null);
  }

  @Test
  public void testGetLocation_NoRedirect() throws MalformedURLException
  {
    URL url = new URL(GOOD_ADDRESS_NO_REDIRECT);
    ParsedURL purl = new ParsedURL(url);

    try
    {
      assertTrue("can connect to known-good url", controller.accessAndDownload(purl));
    }
    catch (IOException e)
    {
      fail("connection failed");
    }

    assertEquals("getLocation returns the expected (original) location",
                 controller.getOriginalLocation(),
                 purl);
  }

  @Test
  public void testGetLocation_WithRedirect() throws MalformedURLException
  {
    URL url = new URL(GOOD_ADDRESS_ONE_REDIRECT);
    ParsedURL purl = new ParsedURL(url);

    try
    {
      assertTrue("can connect to known-good url", controller.accessAndDownload(purl));
    }
    catch (IOException e)
    {
      fail("connection failed");
    }

    assertEquals("getLocation returns the expected (original) location",
                 controller.getOriginalLocation(),
                 purl);
  }

  @Test
  public void testGetRedirectedLocation() throws MalformedURLException
  {
    URL expectedFinalDestination = new URL(MULT_REDIRECT_EXPECTED_FINAL);
    URL url = new URL(GOOD_ADDRESS_MULT_REDIRECT);
    ParsedURL purl = new ParsedURL(url);

    try
    {
      assertTrue("can connect to known-good url with multiple redirect",
                 controller.accessAndDownload(purl));
    }
    catch (IOException e)
    {
      fail("connection failed");
    }

    SimplHttpResponse resp = controller.getHttpResponse();
    List<ParsedURL> redirects = resp.getOtherPurls();
    assertNotNull(redirects);
    Set<String> redirectStrs = new HashSet<String>();
    for (ParsedURL redirect : redirects)
    {
      redirectStrs.add(redirect.toString());
    }
    assertTrue("redirected location is expected location",
               redirectStrs.contains(expectedFinalDestination.toString()));
  }

  @Test
  public void testGetMimeType() throws MalformedURLException
  {
    URL url = new URL(GOOD_ADDRESS_NO_REDIRECT);
    ParsedURL purl = new ParsedURL(url);

    try
    {
      assertTrue("can connect to known-good url", controller.accessAndDownload(purl));
    }
    catch (IOException e)
    {
      fail("connection failed");
    }

    SimplHttpResponse resp = controller.getHttpResponse();
    assertTrue("getMimeType returns a valid string", resp.getMimeType() != null);
  }

  @Test
  public void testGetCharset() throws MalformedURLException
  {
    URL url = new URL(GOOD_ADDRESS_NO_REDIRECT);
    ParsedURL purl = new ParsedURL(url);

    try
    {
      assertTrue("can connect to known-good url", controller.accessAndDownload(purl));
    }
    catch (IOException e)
    {
      fail("connection failed");
    }

    SimplHttpResponse resp = controller.getHttpResponse();
    assertTrue("getCharset returns a valid string", resp.getCharset() != null);
  }

  @Test
  public void testGetHeader() throws MalformedURLException
  {
    URL url = new URL(GOOD_ADDRESS_NO_REDIRECT);
    ParsedURL purl = new ParsedURL(url);

    try
    {
      assertTrue("can connect to known-good url", controller.accessAndDownload(purl));
    }
    catch (IOException e)
    {
      fail("connection failed");
    }

    SimplHttpResponse resp = controller.getHttpResponse();
    assertTrue("getHeader returns a valid string when attempting to retreive a header (Date)",
               resp.getHeader("Date") != null);
  }

  @Test
  public void testGetInputStream() throws MalformedURLException
  {
    URL url = new URL(GOOD_ADDRESS_NO_REDIRECT);
    ParsedURL purl = new ParsedURL(url);
    InputStream is = null;

    try
    {
      assertTrue("can connect to known-good url", controller.accessAndDownload(purl));

      SimplHttpResponse resp = controller.getHttpResponse();
      assertTrue("getInputStream returns a valid InputStream",
                 (is = resp.getContentAsStream()) != null);
      assertTrue("there is valid content on the input stream", is.read() != -1);
    }
    catch (IOException e)
    {
      fail("connection failed");
    }
  }

}
