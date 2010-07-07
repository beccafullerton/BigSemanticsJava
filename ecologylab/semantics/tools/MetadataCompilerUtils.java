/**
 * 
 */
package ecologylab.semantics.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.appframework.types.prefs.PrefString;
import ecologylab.io.Files;
import ecologylab.semantics.metadata.MetadataBuiltinsTranslationScope;
import ecologylab.serialization.XMLTools;

/**
 * This class has all the constants which are needed by the compiler.
 * 
 * @author amathur
 * 
 */
public class MetadataCompilerUtils
{

	public static final String	DEFAULT_GENERATED_SEMANTICS_LOCATION	= ".."
																																				+ Files.sep
																																				+ "ecologylabGeneratedSemantics";

	public static final String	PACKAGE_NAME													= "package ecologylab.semantics.generated.library;\n";

	public static final String	START_JAVA_DOC												= "\n/**\n";

	public static final String	END_JAVA_DOC													= "\n**/ \n\n";

	public static final String	COMMENT																= START_JAVA_DOC
																																				+ "This is a generated code. DO NOT edit or modify it.\n @author MetadataCompiler \n"
																																				+ END_JAVA_DOC;
	public static final HashSet<String> importTargets = new HashSet<String>(Arrays.asList
	(
		"java.util.*",
		"ecologylab.semantics.metametadata.MetaMetadata",
		"ecologylab.net.ParsedURL",
		"ecologylab.generic.HashMapArrayList",
		"ecologylab.serialization.xml_inherit",
		"ecologylab.serialization.types.element.Mappable",
		"ecologylab.semantics.metadata.scalar.*",
		"ecologylab.serialization.TranslationScope",
		"ecologylab.serialization.ElementState.xml_tag",
		"ecologylab.semantics.metadata.builtins.*",
		"ecologylab.semantics.metametadata.MetaMetadataRepository",
		// "ecologylab.semantics.generated.library.*",
		"ecologylab.semantics.metadata.MetadataBuiltinsTranslationScope"
	));
	static
	{
		MetadataBuiltinsTranslationScope.get().addClassNamesToHashSet(importTargets);
	}
	
	public static final String	PACKAGE																= "package";

	public static final String	GENERATED_METADATA_TRANSLATIONS				= "generated_metadata_translations";

	public static PrintWriter		printWriter;

	public static PrintWriter		directBindingTranslationScope;

	public static int						GENERATE_FIELDS_PASS									= 0;

	public static int						GENERATE_METHODS_PASS									= 1;

	/*
	 * static { JAVA_KEY_WORDS_MAP.put("abstract", "abstractField"); JAVA_KEY_WORDS_MAP.put("package",
	 * "packageField"); }
	 */
	/**
	 * TODO change this to write to file
	 * 
	 * @param comment
	 * @throws IOException
	 */
	public static void writeJavaDocComment(String comment, Appendable f) throws IOException
	{
		System.out.println(START_JAVA_DOC + "\t" + comment + END_JAVA_DOC);
		f.append(START_JAVA_DOC + "\t" + comment + END_JAVA_DOC);
	}

	/**
	 * This method returns the path where the generated files are to be placed. TODO FIX ME TO USE
	 * PREFS.
	 * 
	 * @param packageName
	 *          The package in which generated files are to be placed.
	 * @return The path.
	 */
	public static String getGenerationPath(String packageName)
	{
		return getGenerationPath(packageName, DEFAULT_GENERATED_SEMANTICS_LOCATION);
	}

	public static String getGenerationPath(String packageName, String generatedSemanticsLocation)
	{
		PrefString relativePath = PrefString.usePrefString("metadata_generated_relative_path",
				generatedSemanticsLocation);
		String generationPath = relativePath.value() + Files.sep + packageName.replace('.', Files.sep) + Files.sep;
		return generationPath;
	}

	public static void main(String[] args)
	{
		System.out.println(getGenerationPath("ecologylab.semantics.generated.library"));
	}

	/**
	 * This method appends blank constructor
	 * 
	 * @throws IOException
	 */
	public static void appendBlankConstructor(Appendable appendable, String className)
			throws IOException
	{
		String comment = "Constructor";
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);
		appendable.append("public ").append(className + "()\n{\n super();\n}\n");
	}

	/**
	 * This appends a constructor with call to super.
	 */
	public static void appendConstructor(Appendable appendable, String className) throws IOException
	{
		String comment = "Constructor";
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);
		appendable.append("public ").append(className).append(
				"(MetaMetadata metaMetadata)\n{\nsuper(metaMetadata);\n}\n");
	}

	/**
	 * This function checks for JAVA keyword and if a key word is found we append "Field" string after
	 * it.
	 * 
	 * @param name
	 * @return
	 */
	/*
	 * public static String handleJavaKeyWord(String name) { if (JAVA_KEY_WORDS_MAP.containsKey(name))
	 * { return (String) JAVA_KEY_WORDS_MAP.get(name); } else return name; }
	 */

	/**
	 * It appends the specified String to the translation scope class.
	 * 
	 * @param append
	 */
	public static void appendToTranslationScope(String append)
	{
		MetadataCompilerUtils.printWriter.println(append);
	}

	/**
	 * This metod adds the get() method to generated tralation scope and flushes the class out.
	 */
	public static void endTranslationScopeClass()
	{
		MetadataCompilerUtils.printWriter.print("\n};\n \n");
		MetadataCompilerUtils.printWriter
				.print("public static TranslationScope get()\n{\n");
		MetadataCompilerUtils.printWriter.print("return TranslationScope.get(\""
				+ GENERATED_METADATA_TRANSLATIONS
				+ "\", MetadataBuiltinsTranslationScope.get(), TRANSLATIONS);\n}\n}");

		MetadataCompilerUtils.printWriter.flush();
	}

	/**
	 * Creates a class for writing translation scope for generated classes.
	 * 
	 * @param generationPath
	 * @throws IOException
	 */
	public static void createTranslationScopeClass(String generationPath, String packageName) throws IOException
	{
		File directoryPath = PropertiesAndDirectories.createDirsAsNeeded(new File(generationPath));
		File file = new File(directoryPath, XMLTools
				.classNameFromElementName("GeneratedMetadataTranslationScope")
				+ ".java");
		FileWriter fileWriter = new FileWriter(file);
		printWriter = new PrintWriter(fileWriter);

		// Write the package
		printWriter.println("package " + packageName + ";");

		// write java doc comment
		printWriter.println(COMMENT);

		// Write the import statements
//		printWriter.println(MetadataCompiler.getImportStatement());
		MetadataCompiler.printImports(printWriter);

		// Write java-doc comments
		writeJavaDocComment("\nThis is the tranlation scope class for generated files\n.",
				printWriter);

		// begin writing the class
		printWriter
				.print("public class GeneratedMetadataTranslationScope\n{");
		printWriter.print("protected static final Class TRANSLATIONS[]=\n\t{");

	}
}
