package ecologylab.semantics.html;

import org.w3c.tidy.TdNode;

import ecologylab.generic.StringBuilderPool;
import ecologylab.semantics.html.utils.StringBuilderUtils;

/**
 * Keep the paragraph text in the document with the DOM Node to recognize the ArticleMain node.
 * 
 * @author eunyee
 *
 */    
public class ParagraphText
{
	private StringBuilder	buffy;
	private TdNode 			node;
	
	public ParagraphText()
	{
//		ptext = new String();
	}
	
	public TdNode getNode()
	{
		return node;
	}

	public void setNode(TdNode node)
	{
		this.node = node;
	}

	public StringBuilder getBuffy() 
	{
		return buffy;
	}
	
	public int length()
	{
		return buffy == null ? 0 : buffy.length();
	}
	
	public void setBuffy(StringBuilder buffy)
	{
		this.buffy	= buffy;
	}
	
	/**
	 * Append the argument to the buffy inside.
	 * If buffy was not empty at the start of this operation, append a space first.
	 * 
	 * @param toAppend
	 * @return	The number of characters added.
	 */
	public void append(CharSequence toAppend) 
	{
		if (buffy == null)
			//TODO -- should this be built larger? how many calls are made on average?
			buffy			= StringBuilderUtils.acquire();
		else
			buffy.append(' ');
		
		buffy.append(toAppend);
	}
	
	public void append(byte[] bytes, int start, int end)
	{
		if (buffy == null)
			//TODO -- should this be built larger? how many calls are made on average?
			buffy			= StringBuilderUtils.acquire();
		else
			buffy.append(' ');
		
		while (start < end)
		{
			buffy.append((char) bytes[start++]);
		}
	}
	
	public void recycle()
	{
		if (buffy != null)
			StringBuilderUtils.release(buffy);
		buffy				= null;
	}
	
	public TdNode getElementNode()
	{
		for (TdNode thisNode = node; thisNode != null; thisNode = thisNode.parent())
		{
			switch (thisNode.type)
			{
			case TdNode.StartTag:
			case TdNode.StartEndTag:
				return thisNode;
			}
		}
		return null;
	}

}

