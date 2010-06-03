/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.types.element.Mappable;

/**
 * This class represents a linked nested object in a metadata reference graph.
 * The object will be resolved for presentation to the user as a linked entity,
 * such as in InContextMetadata.
 * 
 * @author andruid
 */
@xml_inherit
public class Entity<D extends Document> extends Metadata
implements Mappable<ParsedURL>
{
	@xml_leaf MetadataString		gist;
	@xml_leaf MetadataParsedURL	location;
	
	D															linkedDocument;
	
	
	/**
	 * 
	 */
	public Entity()
	{

	}

	/**
	 * @param metaMetadata
	 */
	public Entity(MetaMetadata metaMetadata)
	{
		super(metaMetadata);
		// TODO Auto-generated constructor stub
	}

	public ParsedURL key()
	{
		return location != null ? location.getValue() : null;
	}

	/**
	 * @return the linkedDocument
	 */
	public D getLinkedDocument()
	{
		return linkedDocument;
	}
	
	public String getGist()
	{
		return gist == null ? null : gist.getValue();
	}

	public void setGist(MetadataString gist)
	{
		this.gist = gist;
	}

	public MetadataParsedURL getEntityLocation()
	{
		return location;
	}

}
