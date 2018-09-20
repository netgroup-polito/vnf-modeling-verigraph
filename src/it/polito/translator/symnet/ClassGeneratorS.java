package it.polito.translator.symnet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.MarshalException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import it.polito.parser.Constants;

/**
 * To provides unmarshalling operations for accessing to the XML document of
 * network rules and generate network rules in format scala.
 * 
 * @author s211483
 * @version 1.0 01/07/2018
 * 
 */
public class ClassGeneratorS {

	private String originalName;
	private String className;
	private String fileNameXml;
	private String fileNameSymNet;

	private List<String[]> imports;

	private AST ast;
	private CompilationUnit cu;
	private RuleUnmarshallerS u;

	private int tableSize = 0;
	private List<String> tableTypes = new ArrayList<>();

	/**
	 * Constructor: initialization of the unmarshaller phase: <br> + Assign the name
	 * of the input and output files <br> + Generate the compilation unit <br> +
	 * Defines the necessary imports
	 * 
	 * @param className it is the XML document to give as input to the unmarshaller
	 */
	public ClassGeneratorS(String className) {
		this.originalName = className;
		this.className = "Rule_" + className;
		this.imports = new ArrayList<String[]>();

		/**
		 * Input file
		 */
		fileNameXml = "./xsd/Rule_" + className + ".xml";

		/**
		 * Output file
		 */
		fileNameSymNet = "./nfSymNetJava/Rule_" + className + ".java";

		imports.add(new String[] { "org", "change", "v2", "analysis", "expression", "concrete", "_" });
		imports.add(new String[] { "org", "change", "v2", "analysis", "memory", "State" });
		imports.add(new String[] { "org", "change", "v2", "analysis", "memory", "TagExp", "_" });
		imports.add(new String[] { "org", "change", "v2", "analysis", "memory", "Tag" });
		imports.add(new String[] { "org", "change", "v2", "analysis", "processingmodels", "instructions", "_" });
		imports.add(new String[] { "org", "change", "v2", "util", "conversion", "RepresentationConversion", "_" });
		imports.add(new String[] { "org", "change", "v2", "util", "canonicalnames", "_" });
		imports.add(new String[] { "org", "change", "v2", "analysis", "memory", "Value" });
		imports.add(new String[] { "org", "change", "v2", "abstractnet", "generic", "_" });
		imports.add(new String[] { "org", "change", "v2", "analysis", "expression", "concrete","nonprimitive","_"});

		this.ast = AST.newAST(AST.JLS9);
		this.cu = ast.newCompilationUnit();

	}

	/**
	 * Start the translation by instantiate a new object of RuleUnmarshallerS . <br>
	 * It read the input xml file, write the package declaration and the type declaration, call the generateRule method and write the result in the java output file. <br>
	 * 
	 * @exception RuntimeException if the input file has an inconsistency in the table definition between the type and the declaration of the dimension
	 */
	@SuppressWarnings("unchecked")
	public void startGeneration() {

		try {

			u = new RuleUnmarshallerS(fileNameXml, originalName, ast);

			tableSize = u.getReuslt().getTableSize();
			tableTypes = u.getReuslt().getTableFields();

			if (tableSize != tableTypes.size())
				throw new RuntimeException("Error: tableSize and tableTypes.size() must be of the same size");

			List<String> tempList = new ArrayList<>();
			for (String type : tableTypes) {
				if (type.compareTo(Constants.ENUM_GENERIC) == 0)
					tempList.add(type);
			}
			tableTypes.removeAll(tempList);
			tableSize = tableTypes.size();

			PackageDeclaration pd = ast.newPackageDeclaration();
			//pd.setName(ast.newName(new String[] { "modify" }));
			pd.setName(ast.newName(new String[] { "org", "change", "v2", "abstractnet", "click","sefl" }));
			cu.setPackage(pd);

			for (String[] temp : imports) {
				ImportDeclaration id = ast.newImportDeclaration();
				id.setName(ast.newName(temp)); // TM
				cu.imports().add(id);
			}

			TypeDeclaration td = ast.newTypeDeclaration();
			td.setName(ast.newSimpleName(className));
			td.setInterface(false);

			cu.types().add(td);

			td.bodyDeclarations().add(u.generateRule());
			if (!tableTypes.isEmpty())
				td.bodyDeclarations().add(u.generateMatch(tableTypes));

			FileWriter writer = new FileWriter(new File(fileNameSymNet));
			writer.write(cu.toString());
			writer.flush();
			writer.close();
		} catch (MarshalException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
