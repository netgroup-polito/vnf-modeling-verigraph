package it.polito.translator.symnet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;
import javax.xml.bind.MarshalException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import it.polito.parser.Constants;

public class ClassGeneratorS {

	private String originalName;
	private String className;
	private String nfName;
	private String fileNameXml;
	private String fileNameSymNet;
	private boolean isDataDriven;

	private List<String[]> imports;

	private AST ast;
	private CompilationUnit cu;
	private RuleUnmarshallerS u;

	private int tableSize = 0;
	private List<String> tableTypes = new ArrayList<>();

	public ClassGeneratorS(String className) {
		this.originalName = className;
		this.className = "SymRule_" + className;
		this.nfName = "n_" + className;
		this.imports = new ArrayList<String[]>();

		fileNameXml = "./xsd/Rule_" + className + ".xml";
		fileNameSymNet = "./netest/Rule_" + className + ".java";

		imports.add(new String[] { "org", "change", "v2", "analysis", "expression", "concrete", "_" });
		imports.add(new String[] { "org", "change", "v2", "analysis", "memory", "State" });
		imports.add(new String[] { "org", "change", "v2", "analysis", "memory", "TagExp", "_" });
		imports.add(new String[] { "org", "change", "v2", "analysis", "memory", "Tag" });
		imports.add(new String[] { "org", "change", "v2", "analysis", "processingmodels", "instructions", "_" });		
		imports.add(new String[] { "org", "change", "v2", "util", "conversion", "RepresentationConversion", "_" });

		this.ast = AST.newAST(AST.JLS9);
		this.cu = ast.newCompilationUnit();

	}

	@SuppressWarnings("unchecked")
	public void startGeneration() {

		try {

			u = new RuleUnmarshallerS(fileNameXml, originalName, ast);

			tableSize = u.getReuslt().getTableSize();
			isDataDriven = u.getReuslt().isDataDriven();
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
			pd.setName(ast.newName(new String[] { "modify_it" }));
			cu.setPackage(pd);

			for (String[] temp : imports) {
				ImportDeclaration id = ast.newImportDeclaration();
				id.setName(ast.newName(temp)); //TM
				cu.imports().add(id);
			}

			TypeDeclaration td = ast.newTypeDeclaration();
			td.setName(ast.newSimpleName(className));
			td.setInterface(false);
	
			cu.types().add(td);

			td.bodyDeclarations().add(u.generateRule());
			if(!tableTypes.isEmpty())
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
