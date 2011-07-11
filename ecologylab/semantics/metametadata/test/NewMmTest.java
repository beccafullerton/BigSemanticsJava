/**
 * 
 */
package ecologylab.semantics.metametadata.test;

import java.io.OutputStream;
import java.util.ArrayList;

import org.w3c.tidy.Tidy;

import ecologylab.appframework.ApplicationEnvironment;
import ecologylab.generic.Continuation;
import ecologylab.io.DownloadProcessor;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.TranslationScope.GRAPH_SWITCH;

/**
 * Basic program for testing meta-metadata.
 * Takes a set of locations as arguments as input.
 * (Input parsing is terminated by a // comment symbol).
 * 
 * Uses semantics to download and parse each input.
 * Sends output to the console as XML.
 * 
 * @author andruid
 */
public class NewMmTest extends ApplicationEnvironment
implements Continuation<DocumentClosure>
{
	ArrayList<DocumentClosure>			documentCollection	= new ArrayList<DocumentClosure>();

	int 														currentResult;
	
	protected boolean								outputOneAtATime;
	
	OutputStream										outputStream;

	DownloadProcessor 							downloadMonitor	= null;
	
	protected SemanticsSessionScope	semanticsSessionScope;


	public NewMmTest(OutputStream	outputStream) throws SIMPLTranslationException
	{
		this("NewMmTest", outputStream);
	}
	public NewMmTest(String appName) throws SIMPLTranslationException
	{
		this(appName, System.out);
	}
	public NewMmTest(String appName, OutputStream	outputStream) throws SIMPLTranslationException
	{
		super(appName);
		this.outputStream	= outputStream;
		
		semanticsSessionScope = new SemanticsSessionScope(GeneratedMetadataTranslationScope.get(), Tidy.class);
	}

	public void collect(String[] urlStrings)
	{		
		// seed start urls
		for (int i = 0; i < urlStrings.length; i++)
		{
			if ("//".equals(urlStrings[i]))
			{
				System.err.println("Terminate due to //");
				break;
			}
			ParsedURL thatPurl	= ParsedURL.getAbsolute(urlStrings[i]);
			Document document		= semanticsSessionScope.getOrConstructDocument(thatPurl);
			DocumentClosure documentClosure = document.getOrConstructClosure();
			if (documentClosure != null)	// super defensive -- make sure its not malformed or null or otherwise a mess
				documentCollection.add(documentClosure);
		}
		postParse(documentCollection.size());

		// process documents after parsing command line so we now how many are really coming
		for (DocumentClosure documentClosure: documentCollection)
		{
			documentClosure.addContinuation(this);
			if (downloadMonitor == null)
				downloadMonitor		= documentClosure.downloadMonitor();
			documentClosure.queueDownload();
		}
		semanticsSessionScope.getDownloadMonitors().requestStopDownloadMonitors();
	}
	
	protected void postParse(int size)
	{
		
	}

	public static void main(String[] args)
	{
		TranslationScope.graphSwitch	= GRAPH_SWITCH.ON;
		NewMmTest mmTest;
		try
		{
			mmTest = new NewMmTest(System.out);
			mmTest.collect(args);
		}
		catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public synchronized void callback(DocumentClosure incomingClosure)
	{
		if (outputOneAtATime)
			output(incomingClosure);
		if (++currentResult == documentCollection.size())
		{
			if (!outputOneAtATime)
			{
				System.out.println("\n\n");
				for (DocumentClosure documentClosure : documentCollection)
					output(documentClosure);
			}
			downloadMonitor.stop();
		}
	}

	protected void output(DocumentClosure incomingClosure)
	{
		incomingClosure.serialize(outputStream);
	}
}
