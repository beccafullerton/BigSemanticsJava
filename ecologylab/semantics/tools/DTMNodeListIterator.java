package ecologylab.semantics.tools;

import java.util.Iterator;

import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeList;
/**
 * 
 * @author amathur
 *
 */
public class DTMNodeListIterator implements Iterator
{
	
	private DTMNodeList nodeList;
	
	// counter for the list
	private int count;
	

	public DTMNodeListIterator(DTMNodeList collectionObject)
	{
		this.nodeList = collectionObject;
		this.count=0;
	}

	@Override
	public boolean hasNext()
	{
		if(nodeList.getLength()>0)
		{
			return true;
		}
		return false;
	}
	
	@Override
	public Object next()
	{
		Object returnValue= nodeList.item(count);
		count++;
		return returnValue;
	}

	@Override
	public void remove()
	{
	 // TODO implement me
		
	}

}
