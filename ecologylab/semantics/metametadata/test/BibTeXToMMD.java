package ecologylab.semantics.metametadata.test;

import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.serialization.Format;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.StringFormat;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;

public class BibTeXToMMD extends NewMmTest
{

	public BibTeXToMMD() throws SIMPLTranslationException
	{
		super("BibTeXToMMD");
	}
	
	public static void main(String[] args)
	{
		SimplTypesScope.graphSwitch	= GRAPH_SWITCH.ON;
		BibTeXToMMD mmTest;
		try
		{
			mmTest = new BibTeXToMMD();
			mmTest.collect(args);
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void callback(DocumentClosure incomingClosure)
	{
		if (outputOneAtATime)
			try
			{
				SimplTypesScope.serialize(incomingClosure, outputStream, Format.BIBTEX);
			}
			catch (SIMPLTranslationException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		else if (++currentResult == documentCollection.size())
		{
			System.out.println("\n\n");
			for (DocumentClosure documentClosure : documentCollection)
				try
				{
					SimplTypesScope.serialize(incomingClosure, System.out, StringFormat.BIBTEX);
				}
				catch (SIMPLTranslationException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			semanticsSessionScope.getDownloadMonitors().stop(false);
		}
	}
}
