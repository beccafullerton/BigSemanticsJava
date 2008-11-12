package ecologylab.semantics.metametadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.tools.MetadataCompilerConstants;
import ecologylab.xml.ElementState;
import ecologylab.xml.FieldAccessor;
import ecologylab.xml.XMLTools;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.types.element.Mappable;
import ecologylab.xml.types.scalar.ScalarType;

/**
 * 
 * @author damaraju
 * 
 */
@xml_inherit
public class MetaMetadataField extends ElementState implements Mappable<String>, PackageSpecifier,
		Iterable<MetaMetadataField>
{

	/**
	 * Name of the metadata field.
	 */
	@xml_attribute
	private String																			name;

	/**
	 * The type of the field
	 */
	@xml_tag("scalar_type")
	@xml_attribute
	private ScalarType																	scalarType;													;

	/**
	 * true if this field should not be displayed in interactive in-context metadata
	 */
	@xml_attribute
	private boolean																			hide;

	/**
	 * If true the field is shown even if its null or empty.
	 */
	@xml_tag("always_show")
	@xml_attribute
	private boolean																			alwaysShow;

	/**
	 * XPath expression used to extract this field.
	 */
	@xml_attribute
	private String																			xpath;

	/**
	 * Another field name that this field navigates to (e.g. from a label in in-context metadata)
	 */
	@xml_attribute
	private String																			navigatesTo;

	/**
	 * This MetaMetadataField shadows another field, so it is to be displayed instead of the other.It
	 * is kind of over-riding a field.
	 */
	@xml_attribute
	private String																			shadows;

	// FIXME -- talk to bharat, eliminate this declaration
	// no idea what this is for....added while parsing for acmportal
	/**
	 * This is used to specify the prefix string which is to be stripped off.
	 */
	@xml_tag("string_prefix")
	@xml_attribute
	private String																			stringPrefix;

	/**
	 * The type for collection children.
	 */
	@xml_attribute
	private String																			collectionChildType;

	@xml_attribute
	private boolean																			isList;

	@xml_attribute
	private boolean																			isMap;

	@xml_attribute
	private boolean																			isFacet;

	@xml_attribute
	private boolean																			ignoreInTermVector;

	@xml_attribute
	private String																			comment;

	/**
	 * Enables hand coding a few Metadata classes, but still providing MetaMetadata to control
	 * operations on them.
	 */
	@xml_attribute
	private boolean																			dontCompile;

	@xml_attribute
	private String																			key;

	@xml_map("meta_metadata_field")
	private HashMapArrayList<String, MetaMetadataField>	childMetaMetadata;

	HashMap<String, String>															childPackagesMap	= new HashMap<String, String>(
																																						2);

	private static ArrayList<MetaMetadataField>					EMPTY_COLLECTION	= new ArrayList<MetaMetadataField>(
																																						0);

	public static Iterator<MetaMetadataField>						EMPTY_ITERATOR		= EMPTY_COLLECTION
																																						.iterator();

	public MetaMetadataField()
	{

	}

	public MetaMetadataField(String name, ScalarType metadataType,
			HashMapArrayList<String, MetaMetadataField> set)
	{
		this.name = name;
		// this.metadataType = metadataType;
		this.childMetaMetadata = set;
	}

	public String packageName()
	{
		String result = "";
		if (result != null)
			return result;
		ElementState parent = parent();
		if ((parent != null) && (parent instanceof PackageSpecifier))
		{
			return ((PackageSpecifier) parent).packageName();
		}
		else
			return null;
	}

	// TODO import paths

	// TODO track generated classes for TranslationScope declaration

	public void translateToMetadataClass(String packageName, Appendable appendable)
			throws XMLTranslationException, IOException
	{

		// check for scalar type.
		if (scalarType != null)
		{
			// Non Null scalar type means we have a nested attribute.
			appendScalarNested(appendable);
		}
		// check if it is a collection
		if (isList || isMap)
		{
			// collection of nested elements
			// TODO -- can these be scalars? if so, how can we tell?
			appendCollection(appendable);
		}

		// new java class has to be written
		if (childMetaMetadata != null)
		{
			// getting the generation path for the java class.
			String generationPath = MetadataCompilerConstants.getGenerationPath(packageName);

			// the name of the java class.
			String javaClassName = name;

			// if the meta-metadata field is of type list or map
			if (isList || isMap)
			{
				// we will generate a class of the name collectionChildType.
				javaClassName = collectionChildType;
			}

			// file writer.
			File directoryPath = PropertiesAndDirectories.createDirsAsNeeded(new File(generationPath));
			File f = new File(directoryPath, XMLTools.classNameFromElementName(javaClassName) + ".java");
			FileWriter fileWriter = new FileWriter(f);
			PrintWriter p = new PrintWriter(fileWriter);

			// writing the package declaration
			p.println(MetadataCompilerConstants.PACKAGE + " " + packageName + ";");

			// writing the imports
			p.println(MetadataCompilerConstants.IMPORTS);

			// start of class definition
			p.println("public class " + XMLTools.classNameFromElementName(javaClassName)
					+ " extends Metadata{\n");

			// write the constructors
			MetadataCompilerConstants.appendBlankConstructor(p, XMLTools
					.classNameFromElementName(javaClassName));
			MetadataCompilerConstants.appendConstructor(p, XMLTools
					.classNameFromElementName(javaClassName));

			// loop to write the class definition.
			for (int i = 0; i < childMetaMetadata.size(); i++)
			{
				// translate the each meta-metadata field into class.
				childMetaMetadata.get(i).translateToMetadataClass(packageName, p);
			}

			// ending the class.
			p.println("}");
			p.flush();

		}
	}

	protected void appendImport(Appendable appendable, String importDecl) throws IOException
	{
		appendable.append("import ").append(importDecl).append(';').append('\n');
	}

	/**
	 * Append an @xml_nested declaration to appendable, using the name of this to directly form both
	 * the class name and the field name.
	 * 
	 * @param appendable
	 * @throws IOException
	 */
	private void appendNested(Appendable appendable) throws IOException
	{
		String elementName = getName();
		appendNested(appendable, "", XMLTools.classNameFromElementName(elementName), XMLTools
				.fieldNameFromElementName(elementName));
	}

	/**
	 * Writes a Scalar Nested attribute to the class file.It is of the form
	 * 
	 * @xml_tag(tagName) @xml_nested scalarType name;
	 * @param appendable
	 *          The appendable in which this declaration has to be appended.
	 * @throws IOException
	 */
	private void appendScalarNested(Appendable appendable) throws IOException
	{
		// write the java doc comment for this field
		MetadataCompilerConstants.writeJavaDocComment(comment, appendable);

		// append the Nested field.
		String fieldName = XMLTools.fieldNameFromElementName(getName());
		fieldName = MetadataCompilerConstants.handleJavaKeyWord(fieldName);
		String fieldTypeName = scalarType.fieldTypeName();
		if (fieldTypeName.equals("int"))
		{
			// HACK FOR METADATAINTEGER
			fieldTypeName = "Integer";
		}
		appendNested(appendable, "private Metadata", scalarType.fieldTypeName(), fieldName);

		// append the getter and setter methods

		appendLazyEvaluationMethod(appendable, fieldName, "Metadata" + fieldTypeName);
		appendGetter(appendable, fieldName, fieldTypeName);
		appendSetter(appendable, fieldName, fieldTypeName);
		appendHWSetter(appendable, fieldName, fieldTypeName);

		if (fieldTypeName.equals("StringBuilder"))
		{
			// appendAppendMethod(appendable,fieldName,"StringBuilder");
			appendAppendMethod(appendable, fieldName, "String");
			appendHWAppendMethod(appendable, fieldName, "StringBuilder");
			appendHWAppendMethod(appendable, fieldName, "String");
		}
	}

	/**
	 * public void appendAnchorText(String anchorText) { this.anchorText().setValue(anchorText); }
	 * 
	 * @param appendable
	 * @param fieldName
	 * @throws IOException
	 */
	private void appendAppendMethod(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = " Appends the value to the field " + fieldName;

		// javadoc comment
		MetadataCompilerConstants.writeJavaDocComment(comment, appendable);

		// first line
		appendable.append("public void append" + XMLTools.javaNameFromElementName(fieldName, true)
				+ "( " + fieldType + " " + fieldName + " )\n{\n");

		// second line
		appendable.append("this." + fieldName + "().setValue(" + fieldName + ");\n}\n");

	}

	/**
	 * this.anchorText().setValue(anchorText); rebuildCompositeTermVector();
	 * 
	 * @param appendable
	 * @param fieldName
	 * @throws IOException
	 */
	private void appendHWAppendMethod(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "The heavy weight Append method for field " + fieldName;
		// write java doc
		MetadataCompilerConstants.writeJavaDocComment(comment, appendable);

		// first line
		appendable.append("public void hwAppend" + XMLTools.javaNameFromElementName(fieldName, true)
				+ "( " + fieldType + " " + fieldName + " )\n{\n");

		// second line
		appendable.append("this." + fieldName + "().setValue(" + fieldName + ");\n");

		// third line
		appendable.append("rebuildCompositeTermVector();\n }");
	}

	/**
	 * This method will generate the getter for the field. public String getTitle() { return
	 * title().getValue(); }
	 */
	private void appendGetter(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "Gets the value of the field " + fieldName;
		// write java doc
		MetadataCompilerConstants.writeJavaDocComment(comment, appendable);

		// first line
		appendable.append("public " + fieldType + " get"
				+ XMLTools.javaNameFromElementName(fieldName, true) + "(){\n");

		// second line
		appendable.append("return " + fieldName + "().getValue();\n}\n");

	}

	/**
	 * This method will generate setter for the field. public void setTitle(String title) {
	 * this.title().setValue(title); }
	 */
	private void appendSetter(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "Sets the value of the field " + fieldName;
		// write java doc
		MetadataCompilerConstants.writeJavaDocComment(comment, appendable);

		// first line
		appendable.append("public void set" + XMLTools.javaNameFromElementName(fieldName, true) + "( "
				+ fieldType + " " + fieldName + " )\n{\n");

		// second line
		appendable.append("this." + fieldName + "().setValue(" + fieldName + ");\n}\n");
	}

	/**
	 * public void hwSetTitle(String title) { this.setTitle(title); rebuildCompositeTermVector(); }
	 */
	private void appendHWSetter(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "The heavy weight setter method for field " + fieldName;
		// write java doc
		MetadataCompilerConstants.writeJavaDocComment(comment, appendable);

		// first line
		appendable.append("public void hwSet" + XMLTools.javaNameFromElementName(fieldName, true)
				+ "( " + fieldType + " " + fieldName + " )\n{\n");

		// second line
		appendable.append("this." + fieldName + "().setValue(" + fieldName + ");\n");

		// third line
		appendable.append("rebuildCompositeTermVector();\n }");
	}

	/**
	 * MetadataString title() { MetadataString result = this.title; if(result == null) { result = new
	 * MetadataString(); this.title = result; } return result; }
	 * 
	 * @throws IOException
	 */
	private void appendLazyEvaluationMethod(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "Lazy Evaluation for " + fieldName;

		String returnType = fieldType;

		// write comment for this method
		MetadataCompilerConstants.writeJavaDocComment(comment, appendable);

		// first line . Start of method name
		appendable.append(returnType).append("\t").append(fieldName).append("()\n{\n");

		// second line. Declaration of result variable.
		appendable.append(returnType).append("\t").append("result\t=this.").append(fieldName).append(
				";\n");

		// third line . Start of if statement
		appendable.append("if(result == null)\n{\n");

		// fourth line. creation of new result object.
		appendable.append("result = new ").append(returnType).append("();\n");

		// fifth line . end of if statement
		appendable.append("this.").append(fieldName).append("\t=\t result;\n}\n");

		// sixth line. return statement and end of method.
		appendable.append("return result;\n}\n");

	}

	/**
	 * Appends the nested field.
	 * 
	 * @param appendable
	 *          The appendable to which the field has to be appended
	 * @param classNamePrefix
	 * @param className
	 *          The type of the field
	 * @param fieldName
	 *          The name of the field.
	 * @throws IOException
	 */
	private void appendNested(Appendable appendable, String classNamePrefix, String className,
			String fieldName) throws IOException
	{
		if (className.equals("int"))
		{
			// HACK FOR METADATAINTEGER
			className = "Integer";
		}
		appendMetalanguageDecl(appendable, "@xml_tag(\"" + getName() + "\") @xml_nested",
				classNamePrefix, className, fieldName);
	}

	/**
	 * This method append a field of type collection to the Java Class. TODO Have to change it to use
	 * HashMapArrayList instead of array list.
	 * 
	 * @param appendable
	 *          The appendable to append to.
	 * @throws IOException
	 */
	private void appendCollection(Appendable appendable) throws IOException
	{
		// name of the element.
		String elementName = this.name;

		// if it belongs to a particular type we will generate a class for it so the type is set to
		// collectionChildType.
		if (this.collectionChildType != null)
		{
			elementName = this.collectionChildType;
		}
		// getting the class name
		String className = XMLTools.classNameFromElementName(elementName);

		// getting the field name.
		String fieldName = XMLTools.fieldNameFromElementName(name) + "s";

		// appending the declaration.
		String mapDecl = childMetaMetadata.get(key).getScalarType().fieldTypeName() + " , " + className;
		appendMetalanguageDecl(appendable, "@xml_collection", "HashMapArrayList<", mapDecl, ">",
				fieldName);
		appendLazyEvaluationMethod(appendable, fieldName, "HashMapArrayList<" + mapDecl + ">");
		appendSetterForCollection(appendable, fieldName, "HashMapArrayList<" + mapDecl + ">");
	}

	/**
	 * public void setAuthors(HashMapArrayList<String, Author> authors) { this.authorNames = authors;
	 * }
	 * 
	 * @param appendable
	 * @param fieldName
	 * @param fieldType
	 */
	private void appendSetterForCollection(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "Set the value of field " + fieldName;
		// write Java doc
		MetadataCompilerConstants.writeJavaDocComment(comment, appendable);

		// write first line
		appendable.append("public void set" + XMLTools.javaNameFromElementName(fieldName, true) + "( "
				+ fieldType + " " + fieldName + " )\n{\n");
		appendable.append("this." + fieldName + " = " + fieldName + " ;\n}\n");
	}

	private void appendMetalanguageDecl(Appendable appendable, String metalanguage,
			String classNamePrefix, String className, String fieldName) throws IOException
	{
		appendMetalanguageDecl(appendable, metalanguage, classNamePrefix, className, "", fieldName);
	}

	private void appendMetalanguageDecl(Appendable appendable, String metalanguage,
			String classNamePrefix, String className, String classNameSuffix, String fieldName)
			throws IOException
	{
		appendable.append('\t').append(metalanguage).append(' ').append(classNamePrefix).append(
				className).append(classNameSuffix).append('\t');
		appendable.append(fieldName).append(';').append('\n');
	}

	public static void main(String args[]) throws XMLTranslationException
	{

	}

	public int size()
	{
		return childMetaMetadata == null ? 0 : childMetaMetadata.size();
	}

	public HashMapArrayList<String, MetaMetadataField> getSet()
	{
		return childMetaMetadata;
	}

	public String getName()
	{
		return name;
	}

	public String key()
	{
		return name;
	}

	public ScalarType getMetadataType()
	{
		return scalarType;
	}

	public MetaMetadataField lookupChild(String name)
	{
		return childMetaMetadata.get(name);
	}

	public MetaMetadataField lookupChild(FieldAccessor fieldAccessor)
	{
		return childMetaMetadata.get(fieldAccessor.getTagName());
	}

	public String getXpath()
	{
		return xpath;
	}

	public boolean isList()
	{
		return isList;
	}

	public boolean isMap()
	{
		return isMap;
	}

	public String getStringPrefix()
	{
		return stringPrefix;
	}

	/**
	 * @return the key
	 */
	public String getKey()
	{
		return key;
	}

	/**
	 * @return the scalarType
	 */
	public ScalarType getScalarType()
	{
		return scalarType;
	}

	/**
	 * @param key
	 *          the key to set
	 */
	public void setKey(String key)
	{
		this.key = key;
	}

	public boolean isHide()
	{
		return hide;
	}

	protected Collection<String> importPackages()
	{
		Collection<String> result = null;
		if ((childMetaMetadata == null) || (childMetaMetadata.size() == 0))
		{
			result = new ArrayList<String>();
			result.add(packageName());
		}

		return result;
	}

	public boolean isIgnoreInTermVector()
	{
		return ignoreInTermVector;
	}

	/**
	 * Add a child field from a super class into the representation for this. Unless it should be
	 * shadowed, in which case ignore.
	 * 
	 * @param childMetaMetadataField
	 */
	void addChild(MetaMetadataField childMetaMetadataField)
	{
		String fieldName = childMetaMetadataField.getName();
		// *do not* override fields in here with fields from super classes.
		if (!childMetaMetadata.containsKey(fieldName))
			childMetaMetadata.put(fieldName, childMetaMetadataField);
	}

	/**
	 * @param childMetaMetadata
	 *          the childMetaMetadata to set
	 */
	public void setChildMetaMetadata(HashMapArrayList<String, MetaMetadataField> childMetaMetadata)
	{
		this.childMetaMetadata = childMetaMetadata;
	}

	public Iterator<MetaMetadataField> iterator()
	{
		return (childMetaMetadata != null) ? childMetaMetadata.iterator() : EMPTY_ITERATOR;
	}

	public boolean isAlwaysShow()
	{
		return alwaysShow;
	}

	public String getCollectionChildType()
	{
		return collectionChildType;
	}

	public String shadows()
	{
		return shadows;
	}

	public String getNavigatesTo()
	{
		return navigatesTo;
	}

	/**
	 * @return the childMetaMetadata
	 */
	public HashMapArrayList<String, MetaMetadataField> getChildMetaMetadata()
	{
		return childMetaMetadata;
	}

}
