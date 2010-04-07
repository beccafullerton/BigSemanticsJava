package ecologylab.semantics.documentparsers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.html.DOMFragmentInformationTagger;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.html.TidyInterface;

public class HTMLFragmentDOMParser<C extends Container> extends HTMLDOMParser<C, InfoCollector<C>> implements TidyInterface
{

	InputStream fragmentStream;
	public DOMFragmentInformationTagger taggedDoc;
	
	public HTMLFragmentDOMParser(InfoCollector<C> infoCollector, InputStream inputStream)
	{
		super(infoCollector);
		fragmentStream = inputStream;
		parse();
	}
	
	public void postParse()
	{
		taggedDoc = new DOMFragmentInformationTagger(tidy.getConfiguration(), null, this);
		taggedDoc.generateCollections(this);
	}
	
	public InputStream inputStream()
	{
		return fragmentStream;
	}
	
	public String getDNDText()
	{
		return taggedDoc.getDNDText();
	}
	
	public ArrayList<ImgElement> getDNDImages()
	{
		return taggedDoc.getDNDImages();
	}

	public void setContent()
	{	
	}

	public void setIndexPage()
	{	
	}
	

}
