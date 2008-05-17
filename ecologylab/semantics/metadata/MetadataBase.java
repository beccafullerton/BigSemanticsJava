/**
 * 
 */
package ecologylab.semantics.metadata;

import ecologylab.model.ParticipantInterest;
import ecologylab.model.text.TermVector;
import ecologylab.xml.ElementState;

/**
 * Base class for Metadata fields that represent scalar values.
 * 
 * These, for example, lack mixins.
 * 
 * @author andruid
 *
 */
public class MetadataBase extends ElementState
{
	//FIXME -- not public!
	public TermVector 				compositeTermVector;
	
	/**
	 * Represents interest in this field as a whole
	 * (Example: author = Ben Shneiderman), in addition to interest
	 * propagated into the termVector, which represents interest in
	 * particular Terms. While the latter is the basis of the information
	 * retrieval model (IR), this is what enables the model to function
	 * in the context of the semantic web / digital libraries.
	 */  
	ParticipantInterest				participantInterest = new ParticipantInterest();


	/**
	 * 
	 */
	public MetadataBase()
	{
		// TODO Auto-generated constructor stub
	}

}
