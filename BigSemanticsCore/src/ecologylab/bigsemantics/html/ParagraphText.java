package ecologylab.bigsemantics.html;

import org.w3c.dom.Node;

import ecologylab.bigsemantics.html.utils.StringBuilderUtils;
import ecologylab.bigsemantics.model.text.TermVector;
import ecologylab.serialization.XMLTools;

/**
 * Keep the paragraph text in the document with the DOM Node to recognize the ArticleMain node.
 * 
 * @author eunyee
 *
 */    
public class ParagraphText
{
	private StringBuilder	buffy;
	private Node 			node;
	
	public ParagraphText()
	{
//		ptext = new String();
	}
	
	public Node getNode()
	{
		return node;
	}

	public void setNode(Node blockNode)
	{
		this.node = blockNode;
	}

//TODO -- get rid of this visibility, because it is prone to creating memory leaks.
// Anyway, higher level passing of this is better, because it carries context with it.
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
		if (this.buffy != null)
			StringBuilderUtils.release(this.buffy);

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
	
	public void append(char[] bytes, int start, int end)
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
		{
			StringBuilderUtils.release(buffy);
			buffy				= null;
		}
		if (termVector != null)
		{
			termVector.clear();
			termVector	= null;
		}
	}
	
	public Node getElementNode()
	{
		for (Node thisNode = node; thisNode != null; thisNode = thisNode.getParentNode())
		{
			switch (thisNode.getNodeType())
			{
			case Node.ELEMENT_NODE:
				return thisNode;
			}
		}
		return null;
	}
	
	public boolean hasText()
	{
		return buffy != null && buffy.length() > 0;
	}

	public boolean isEmpty()
	{
		return buffy == null || buffy.length() == 0;
	}

	public int indexOf(String s)
	{
		return (buffy == null) ? -1 : buffy.indexOf(s);
	}
	public void unescapeXML()
	{
		XMLTools.unescapeXML(buffy);
	}
	TermVector termVector;
	
	public TermVector termVector()
	{
		TermVector result = this.termVector;
		if (result == null)
		{
			result					= new TermVector(buffy);
			this.termVector	= result;
		}
		return result;
	}
	
	/**
	 * Set the textContext for the ImgElement (HTMLElement) to the buffy of this.
	 * 
	 * @param imgElement
	 */
	public void setImgElementTextContext(ImgElement imgElement)
	{
		imgElement.setTextContext(buffy);
	}
}

