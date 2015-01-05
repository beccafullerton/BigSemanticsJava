package ecologylab.bigsemantics.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;

import ecologylab.bigsemantics.collecting.DownloadStatus;
import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.metadata.builtins.RichArtifact;
import ecologylab.serialization.SIMPLTranslationException;

public class TestMmdSpeed extends MmTest
{
	PrintStream	print;

	public TestMmdSpeed(String appName) throws SIMPLTranslationException
	{
		super(appName);
		try
		{
			File outfile = new File("TestMmdSpeed.csv");
			print = new PrintStream(outfile);
			System.out.println("Output csv file at:"+outfile.getAbsolutePath());
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String args[])
	{
		TestMmdSpeed speedTest;
		try
		{
			speedTest = new TestMmdSpeed("TestMmSpeed");
			speedTest.collect(args);
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
	}
	
	boolean shownHeaderYet = false;
	@Override protected void output(DocumentClosure incomingClosure)
	{
		Document document	= incomingClosure.getDocument();
		if (document != null)
		{
		  RichArtifact<Metadata> artifact = new RichArtifact<Metadata>();
		  artifact.outlinks().add(document);
		  curation.metadataCollection().add(artifact);

			String outline = ""+document.getLocation().toString();
			HashMap<DownloadStatus, Long> statusChanges = document.getTransitionTimeToDownloadStatus();
			
			if(shownHeaderYet == false)
			{
				String line = "URL,";
				for(DownloadStatus downloadStatus : DownloadStatus.values())
					line += downloadStatus+",";
				print.append(line+"\n");
				System.out.println(line);
				shownHeaderYet = true;
			}
			for(DownloadStatus downloadStatus : DownloadStatus.values())
			{
			  if(statusChanges.containsKey(downloadStatus))
			  {
			  	outline += ","+statusChanges.get(downloadStatus);
			  }
			  else
			  {
			  	outline += ","+"null";
			  }
			}
			System.out.println(outline);
			print.append(outline+"\n");
			System.out.println(outline);		
		}
	}
}
