package ecologylab.semantics.metadata.extraction;



import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import ecologylab.generic.Debug;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.scholarlyPublication.AcmPortal;
import ecologylab.semantics.library.scholarlyPublication.Author;
import ecologylab.semantics.library.scholarlyPublication.Reference;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.xml.FieldAccessor;
import ecologylab.xml.types.scalar.ParsedURLType;
import ecologylab.xml.types.scalar.ScalarType;

public class HtmlDomExtractor<M extends Metadata> extends Debug
{
	
	private static final String ACMPORTAL_DOMAIN = "http://portal.acm.org/";
	static Tidy tidy;
	static XPath xpath;
	

	public HtmlDomExtractor()
	{
		tidy = new Tidy();
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		xpath = XPathFactory.newInstance().newXPath();
	}
	/**
	 * Checks to see which HtmlDomExtractionPattern can be used and calls
	 * <code>extractMetadataWithPattern</code> using the appropriate patter
	 * 
	 * @param purl
	 * @return
	 * @throws XmlTranslationException
	 */
	public M populateMetadata(ParsedURL purl, M  metadata)
	{
		MetaMetadata metaMetadata = metadata.getMetaMetadata();
		if(metaMetadata.isSupported(purl))
		{
			PURLConnection purlConnection = purl.connect();

			
//			println("Start Copy -------------/n/n/n");
			Document tidyDOM = tidy.parseDOM(purlConnection.inputStream(), null /*System.out*/);
//			println("/n/n/n/nEnd Copy -------------");
			
			M populatedMetadata = recursiveExtraction(metadata, metaMetadata, tidyDOM, purl);

			return populatedMetadata;
		} else
		{
			println("Pattern supports only URLs starting with: "
					+ metaMetadata.getUrlBase());
		}

		return null;
		
	}
	
	/**
	 * Extracts the metadata from the given pattern
	 * @param mmdField 
	 * 
	 * @param purl2
	 * @param hdePattern
	 * @return a HashMap of metadata (with nested HashMaps)
	 */
	private M recursiveExtraction(M metadata, MetaMetadataField mmdField, Document tidyDOM, ParsedURL purl)
	{
		HashMapArrayList<String, MetaMetadataField> mmdFieldSet = mmdField.getSet();
		int size = mmdFieldSet.size();
		for (int i = 0; i < mmdFieldSet.size(); i++)
		{
			MetaMetadataField mmdElement 	= mmdFieldSet.get(i);
			String mmdElementName 			= mmdElement.getName();
			println(mmdElementName);
		}
		for (int i = 0; i < mmdFieldSet.size(); i++)
		{
			MetaMetadataField mmdElement 	= mmdFieldSet.get(i);

			String xpathString 				= mmdElement.getXpath();
			String mmdElementName 			= mmdElement.getName();
			if(mmdElementName.equals("references"))
			{
				debug("");
			}
			if(mmdElement.getSet() == null) 
			{
//				String xpathString = mmdElement.getXpath();
//				String mmdElementName = mmdElement.getName();
				if (xpathString != null)
				{
					try
					{
						if (mmdElement.isList())
						{
							
							NodeList nodes = (NodeList) xpath.evaluate(xpathString, tidyDOM, XPathConstants.NODESET);
							ArrayList<String> values = new ArrayList<String>();
							for (int j = 0; j < nodes.getLength(); j++)
							{
								String nodeValue = nodes.item(j).getNodeValue();
								values.add(nodeValue);
								metadata.set(mmdElementName, nodeValue);
								// println(mdElement.getName() + ":\t" + nodeValue);
							}
//							metadataMap.put(mmdElementName, values);

						} else
						{
							String evaluation = xpath.evaluate(xpathString, tidyDOM);
							String stringPrefix = mmdElement.getStringPrefix();
							//
							if (stringPrefix != null)
							{
								if (evaluation.startsWith(stringPrefix))
								{
									evaluation = evaluation.substring(stringPrefix.length());
									evaluation = evaluation.trim();
								} else
									evaluation = null;
							}
							// println(mdElement.getName() + ":\t" + evaluation);
							if((mmdElementName.equals("full_text") /*|| mmdElementName.equals("img_purl") */|| 
									mmdElementName.equals("table_of_contents") || mmdElementName.equals("archive") ||
									mmdElementName.equals("results_page")) && evaluation != null &&
									evaluation != "null" && (evaluation.length() != 0))
							{
								//the url base is different form the domain so creating a constant.
								evaluation = ACMPORTAL_DOMAIN + evaluation;
							}
							
							if(mmdElementName.equals("isbn"))
							{
								println("debug");
							}
							if(metadata == null)
							{
								println("debug");
							}
							metadata.set(mmdElementName, evaluation);
//							metadataMap.put(mmdElementName, evaluation);
						}
					} catch (XPathExpressionException e)
					{
						println("Xpath Evaluation Error for expression:"
								+ xpathString + "\n\t On purl:" + purl.toString());
						e.printStackTrace();
					}
				}
			}
			else // If the meta_metadatafield is nested				
			{
				//Get the corresponding Metadata Field which is nested.
				//Now call recursion on the nested Metadata Field.
//				M nestedMetadata = (M) metadata.getMetadataWhichContainsField(mmdElementName);
//				mmdField.lookupChild(mmdElementName);
//				nestedMetadata = recursiveExtraction(nestedMetadata, tidyDOM, purl);
//				HashMap<String, Object> innerMap = recursiveExtraction(metadata, mmdElement, tidyDOM, purl);
//				metadataMap.put(mmdElementName, innerMap);
				
				M nestedMetadata = null;
				if(mmdElementName.equals("source"))
				{
					println("debug");
				}
				//For all the nested classes
				if(mmdElement.isNested())
				{
					//Have to return the nested object for the field.
					FieldAccessor fieldAccessor = metadata.getMetadataFieldAccessor(mmdElementName);
					Field field 				= fieldAccessor.getField();
					
					/**
					 * We can use either of the below methods to get the nestedMetadata.
					 */
					//nestedMetadata 				= (M) ReflectionTools.getFieldValue(metadata , field);
					try
					{
						nestedMetadata 				= (M) field.get(metadata);
					} catch (IllegalArgumentException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					debug("");
					/*M nestedMetadata = metadata.getObject(mmdElementName);
					M nestedMetadata = metadata.mmdElementName;*/
					
					if(nestedMetadata == null)
					{
						debug("");
						nestedMetadata = (M) ReflectionTools.getInstance(field.getType());
						ReflectionTools.setFieldValue(metadata, field, nestedMetadata);
					}
					recursiveExtraction(nestedMetadata, mmdElement, tidyDOM, purl);
				}
				
				else if(mmdElement.isMap())
				{
					debug("");
					FieldAccessor fieldAccessor = metadata.getMetadataFieldAccessor(mmdElementName);
					Field field = fieldAccessor.getField();
					HashMapArrayList<Object, Metadata> mappedMetadata;
//					nestedMetadata = (M) ReflectionTools.getFieldValue(metadata , field);
					
					
					mappedMetadata = (HashMapArrayList<Object, Metadata>) ReflectionTools.getFieldValue(metadata , field);
					if(mappedMetadata == null)
					{
						debug("");
						mappedMetadata = (HashMapArrayList<Object, Metadata>) ReflectionTools.getInstance(field.getType());
						ReflectionTools.setFieldValue(metadata, field, mappedMetadata);
					}					
					
					
					///
//					ReflectionTools.getFieldValue(metadata , field);
					///
//					mmdElement = author
//					mmdFieldSetChild = name,affiliation,results_page
					
					HashMapArrayList<String, MetaMetadataField> mmdFieldSetChild = mmdElement.getSet();//author
					
					//Populating the key data.
					String key = mmdElement.getKey();
					MetaMetadataField mmdChildElement = mmdFieldSetChild.get(key);
					
					String xpathStringChild = mmdChildElement.getXpath();
					String mmdElementNameChild = mmdChildElement.getName();
					NodeList nodes = null;
					
					try
					{
						nodes = (NodeList) xpath.evaluate(xpathStringChild, tidyDOM, XPathConstants.NODESET);
					} catch (XPathExpressionException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ArrayList<String> values = new ArrayList<String>();
					int keys = nodes.getLength();
					if(keys == 2)
					{
						debug("");
					}
					for (int j = 0; j < nodes.getLength(); j++)
					{
						String nodeValue = nodes.item(j).getNodeValue();
						if(mmdChildElement.getScalarType() instanceof ParsedURLType)
						{
							nodeValue = ACMPORTAL_DOMAIN + nodeValue;
						}
//						nestedMetadata.add(nodeValue);
						
						if(mmdElementName.equals("authors"))
						{
							mappedMetadata.put(nodeValue, new Author());
							Metadata mapVElement = mappedMetadata.get(nodeValue);
							mapVElement.set(key, nodeValue);
						}
						if(mmdElementName.equals("references") || mmdElementName.equals("citations"))
						{
							mappedMetadata.put(nodeValue, new Reference());
							Metadata mapVElement = mappedMetadata.get(nodeValue);
							mapVElement.set(key, nodeValue);
						}
					}
					
					//Populating the other attributes in the map
					for (int k = 0; k < mmdFieldSetChild.size(); k++)
					{
						mmdChildElement = mmdFieldSetChild.get(k);
						xpathStringChild = mmdChildElement.getXpath();
						mmdElementNameChild = mmdChildElement.getName();
						if(!key.equals(mmdElementNameChild))
						{
							try
							{
								nodes = (NodeList) xpath.evaluate(xpathStringChild, tidyDOM, XPathConstants.NODESET);
							} catch (XPathExpressionException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							values = new ArrayList<String>();
							int attributes = nodes.getLength();
							int size2 = mappedMetadata.size();
							
							if(keys != attributes)
							{
								println("debug");
							}
							for (int l = 0; l < nodes.getLength() && l < keys && l < mappedMetadata.size(); l++)
							{
								String nodeValue = nodes.item(l).getNodeValue();
								println("Attribute Name: "+mmdElementNameChild + "Attribute value: "+nodeValue);
								println("No of Keys: "+keys + "index:  "+l);
								
//								Metadata author = nestedMetadata.get(l);
								Metadata mapVElement = mappedMetadata.get(l);
								if(mmdChildElement.getScalarType() instanceof ParsedURLType)
								{
									nodeValue = ACMPORTAL_DOMAIN + nodeValue;
								}
								if(mmdElementNameChild.equals("bit_tex"))
								{
									debug("");
								}
								if(mapVElement != null)
								{
									mapVElement.set(mmdElementNameChild, nodeValue);
								}
							}
						}
					}//for
					println("debug");
					
					
					///
					
				
				}

				
				
			}
		}

		return metadata;

	}
	
	

}
