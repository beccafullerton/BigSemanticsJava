/**
 * 
 */
package ecologylab.semantics.metametadata.test;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;

import ecologylab.appframework.SingletonApplicationEnvironment;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.MetaMetadataRepositoryInit;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.cyberneko.CybernekoWrapper;
import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
import ecologylab.serialization.formatenums.Format;

/**
 * Basic program for testing meta-metadata. Takes a set of locations as arguments as input. (Input
 * parsing is terminated by a // comment symbol).
 * 
 * Uses semantics to download and parse each input. Sends output to the console as XML.
 * 
 * @author andruid
 */
public class NewMmTest extends SingletonApplicationEnvironment implements
		Continuation<DocumentClosure>
{
	ArrayList<DocumentClosure>			documentCollection	= new ArrayList<DocumentClosure>();

	int															currentResult;

	protected boolean								outputOneAtATime;

	OutputStream										outputStream;

	protected SemanticsSessionScope	semanticsSessionScope;

	public NewMmTest(String appName) throws SIMPLTranslationException
	{
		this(appName, System.out);
	}

	public NewMmTest(String appName, OutputStream outputStream) throws SIMPLTranslationException
	{
		this(appName, outputStream, RepositoryMetadataTranslationScope.get());
	}

	public NewMmTest(String appName, OutputStream outputStream,
			SimplTypesScope metadataTranslationScope) throws SIMPLTranslationException
	{
		this(appName, outputStream, metadataTranslationScope, null,
				MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_FORMAT);
	}

	public NewMmTest(String appName, File repositoryLocation, Format repositoryFormat)
			throws SIMPLTranslationException
	{
		this(appName, System.out, repositoryLocation, repositoryFormat);
	}

	public NewMmTest(String appName, OutputStream outputStream, File repositoryLocation,
			Format repositoryFormat) throws SIMPLTranslationException
	{
		this(appName, outputStream, RepositoryMetadataTranslationScope.get(), repositoryLocation,
				repositoryFormat);
	}

	public NewMmTest(String appName, OutputStream outputStream,
			SimplTypesScope metadataTranslationScope, File repositoryLocation, Format repositoryFormat)
			throws SIMPLTranslationException
	{
		super(appName);
		SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
		this.outputStream = outputStream;
		semanticsSessionScope = new SemanticsSessionScope(repositoryLocation, repositoryFormat,
				metadataTranslationScope, CybernekoWrapper.class);
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
			if (urlStrings[i].startsWith("//"))
				continue; // commented out urls

			ParsedURL thatPurl = ParsedURL.getAbsolute(urlStrings[i]);
			Document document = semanticsSessionScope.getOrConstructDocument(thatPurl);
			DocumentClosure documentClosure = document.getOrConstructClosure();
			if (documentClosure != null) // super defensive -- make sure its not malformed or null or
																		// otherwise a mess
				documentCollection.add(documentClosure);
		}
		postParse(documentCollection.size());

		// process documents after parsing command line so we now how many are really coming
		for (DocumentClosure documentClosure : documentCollection)
		{
			documentClosure.addContinuation(this);
			documentClosure.queueDownload();
		}
		semanticsSessionScope.getDownloadMonitors().requestStops();
	}

	protected void postParse(int size)
	{

	}

	public static void main(String[] args)
	{
		NewMmTest mmTest;
		try
		{
			mmTest = new NewMmTest("NewMmTest");
			mmTest.collect(args);
		}
		catch (SIMPLTranslationException e)
		{
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
			semanticsSessionScope.getDownloadMonitors().stop(false);
		}
	}

	protected void output(DocumentClosure incomingClosure)
	{
		incomingClosure.serialize(outputStream);
	}
}
