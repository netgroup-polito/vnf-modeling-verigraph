package it.polito.translator.symnet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.xml.sax.SAXException;

import it.polito.nfdev.jaxb.ExpressionObject;
import it.polito.nfdev.jaxb.ExpressionResult;
import it.polito.nfdev.jaxb.LFFieldOf;
import it.polito.nfdev.jaxb.LFIsInternal;
import it.polito.nfdev.jaxb.LFMatchEntry;
import it.polito.nfdev.jaxb.LOAnd;
import it.polito.nfdev.jaxb.LOEquals;
import it.polito.nfdev.jaxb.LOExist;
import it.polito.nfdev.jaxb.LOImplies;
import it.polito.nfdev.jaxb.LONot;
import it.polito.nfdev.jaxb.LOOr;
import it.polito.parser.Constants;

/**
 * Recursively scans all AST nodes of the input XML document. <br>
 * Depending on the type associated with the node, <br>
 * a different method is called and generates the corresponding rules in SEFL
 * language.
 * <p>
 * All methods with the name "generate<strong>Type</strong>-" process the nested
 * node information to generate a set of SEFL statements for the specific
 * <strong>type</strong>. <br>
 * All the methods with the name "new<strong>Name</strong>" produce a specific
 * <strong>Name</strong> SEFL instruction.<br>
 * All the methods with the name "make<strong>...</strong>" produce a
 * structure/element needed in a SEFL instruction. <br>
 * <p>
 * The starting point of the translation is on the <strong> generateRule ()
 * </strong> method and all the other methods are called recursively by it
 * 
 * @author s211483
 * @version 1.0 01/07/2018
 *
 */
public class RuleUnmarshallerS {

	private AST ast;
	private MethodInvocation startblock;
	private MethodInvocation assigvalues;
	private MethodInvocation stateblockint;
	private MethodInvocation stateblock;

	private ExpressionResult reuslt;

	private List<String> params;
	private List<String> packetfield;
	private List<String> statetype;
	private Boolean blacklist = false;
	private Boolean match = false;
	private Boolean flagnot = false;
	private String tableSize = "0";
	private Boolean state = false;
	private boolean flagstate = false;

	/**
	 * Constructor: Instantiate the client's entry point to the JAXB framework. <br>
	 * The location of the XML schema and XML document must be provided.
	 * 
	 * @param fileName  the input XML document to translate
	 * @param className the original name of the input file
	 * @param ast       the new AST to explore file
	 * @throws MarshalException if the AST root is invalid
	 */
	public RuleUnmarshallerS(String fileName, String className, AST ast) throws MarshalException {

		this.ast = ast;
		this.params = new ArrayList<>();

		JAXBContext context;
		try {
			context = JAXBContext.newInstance("it.polito.nfdev.jaxb");
			Unmarshaller u = context.createUnmarshaller();

			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(new File("./xsd/LogicalExpressions.xsd"));
			u.setSchema(schema);
			Object JaxbElement = u.unmarshal(new File(fileName));

			if (JaxbElement != null && JaxbElement instanceof JAXBElement<?>) {
				Object temp = ((JAXBElement<?>) JaxbElement).getValue();
				if (temp instanceof ExpressionResult) {
					reuslt = (ExpressionResult) temp;
				} else
					throw new MarshalException("Invalid root element");
			} else
				throw new MarshalException("Invalid root element");

		} catch (JAXBException e) {
			throw new MarshalException(e);
		} catch (SAXException e) {
			throw new MarshalException(e);
		}
	}

	/**
	 * The entry point of AST exploration. <br>
	 * A new method declaration is generated and contains all the network policies
	 * in an SEFL format.
	 * 
	 * @return a new Method Declaration
	 */
	@SuppressWarnings("unchecked")
	public MethodDeclaration generateRule() {

		if (reuslt.getLogicalExpressionResult().size() < 1 || reuslt.getNodeOrPacket().size() < 1)
			return null;

		MethodDeclaration method = ast.newMethodDeclaration();
		method.setName(ast.newSimpleName("generate_rules"));
		method.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

		SimpleType ret = ast.newSimpleType(ast.newName(Constants.BLOCK));
		method.setReturnType2(ret);

		SingleVariableDeclaration param = ast.newSingleVariableDeclaration();
		param.setName(ast.newSimpleName("params"));
		param.setType(ast.newArrayType(ast.newSimpleType(ast.newName("ConfigParameter"))));

		method.parameters().add(param);

		method.setBody(ast.newBlock());

		//for (ExpressionObject temp : reuslt.getLogicalExpressionResult()) {
			
		    ExpressionObject temp = reuslt.getLogicalExpressionResult().get(0);
			Assignment assignment = ast.newAssignment();

			VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
			vdf.setName(ast.newSimpleName("code"));
			VariableDeclarationExpression vde = ast.newVariableDeclarationExpression(vdf);
			vde.setType(ast.newSimpleType(ast.newSimpleName(Constants.BLOCK)));
			assignment.setLeftHandSide(vde);

			assignment.setOperator(Assignment.Operator.ASSIGN);

			/**
			 * The variable <strong>startblock</strong> is a MethodInvocation to mimic the
			 * InstructionBlock SEFL instructions. This Block has all policies in SEFL
			 * format that will be processed by the SymNet verification tool.
			 */
			startblock = ast.newMethodInvocation();
			startblock.setName(ast.newSimpleName(Constants.BLOCK));
			intGenerateRule();

			assigvalues = ast.newMethodInvocation();
			assigvalues.setName(ast.newSimpleName(Constants.BLOCK));

			Expression toadd;
			if ((toadd = getType(temp)) != null)
				startblock.arguments().add(toadd);

			if (!assigvalues.arguments().isEmpty()) {
				if(state) {
					stateblockint.arguments().add(assigvalues);	
				}else {
				startblock.arguments().add(assigvalues);}
			}
			assignment.setRightHandSide(startblock);
			method.getBody().statements().add(ast.newExpressionStatement(assignment));
		//}

		/**
		 * Generate a SEFL instruction for call a method that generates the SEFL
		 * instructions that correspond to the LFMatchEntry type. <br>
		 */
		if (match == true) {
			MethodInvocation miib = ast.newMethodInvocation();
			miib.setName(ast.newSimpleName(Constants.BLOCK));
			{
				MethodInvocation mi = ast.newMethodInvocation();
				mi.setName(ast.newSimpleName("addrule"));
				mi.arguments().add(ast.newSimpleName("params"));
				miib.arguments().add(mi);
			}
			startblock.arguments().add(miib);
		}
		if(state) {	
			stateblock.arguments().add(stateblockint);
			stateblock.arguments().add(ast.newName("NoOp"));
			startblock.arguments().add(stateblock);
		}

		ReturnStatement rs = ast.newReturnStatement();
		rs.setExpression((ast.newSimpleName("code")));
		method.getBody().statements().add(rs);

		return method;
	}

	/**
	 * Generate a Method Invocation that contains a set of SEFL instructions
	 * corresponding to the LFMatchEntry. <br>
	 * If the flag <strong> match </strong> is <strong> false </strong> the method
	 * return null because in the input file there is not a LFMatchType. Otherwise,
	 * it also check the flag <strong> blacklist </strong> and generate the relative
	 * SEFL instructions.
	 * 
	 * @param tableTypes The network function table entry types
	 * @return a new Method Declaration if the flag match is set otherwise null.
	 */
	@SuppressWarnings("unchecked")
	public MethodDeclaration generateMatch(List<String> tableTypes) {
		int it = 0; // Iterate col of tableTypes
		if (match == false)
			return null;

		/**
		 * The variable <strong>tableSize</strong> is the number of columns in the NF
		 * table.
		 */
		this.tableSize = Integer.toString(tableTypes.size());

		MethodDeclaration md = ast.newMethodDeclaration();
		md.setName(ast.newSimpleName("addrule"));
		md.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

		ArrayType at1 = ast.newArrayType(ast.newSimpleType(ast.newSimpleName(Constants.BLOCK)));
		md.setReturnType2(at1);

		/**
		 * The variable <strong>p</strong> is the array that contains the entries given
		 * to the network function being configured
		 */
		SingleVariableDeclaration param = ast.newSingleVariableDeclaration();
		param.setName(ast.newSimpleName("p"));
		param.setType(ast.newArrayType(ast.newSimpleType(ast.newName("ConfigParameter"))));
		md.parameters().add(param);

		md.setBody(ast.newBlock());

		/**
		 * The variable <strong>rule</strong> is the InstructionBlock SEFL statement
		 * generate to each entries given to the network function being configured.
		 */
		VariableDeclarationFragment vdfrule = ast.newVariableDeclarationFragment();
		vdfrule.setName(ast.newSimpleName("rule"));
		VariableDeclarationStatement vdsrule = ast.newVariableDeclarationStatement(vdfrule);
		vdsrule.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName(Constants.BLOCK))));
		md.getBody().statements().add(vdsrule);

		/**
		 * The variable <strong>rules</strong> is the array that contains all the
		 * <strong>rule</strong> statements.
		 */
		VariableDeclarationFragment vdfrules = ast.newVariableDeclarationFragment();
		vdfrules.setName(ast.newSimpleName("rules"));
		MethodInvocation miinizializer = ast.newMethodInvocation();
		miinizializer.setName(ast.newSimpleName("Array"));
		{
			MethodInvocation miib = ast.newMethodInvocation();
			miib.setName(ast.newSimpleName(Constants.BLOCK));
			miib.arguments().add(ast.newSimpleName("Nil"));
			miinizializer.arguments().add(miib);
		}
		vdfrules.setInitializer(miinizializer);
		VariableDeclarationStatement vdsrules2 = ast.newVariableDeclarationStatement(vdfrules);
		vdsrules2.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName(Constants.BLOCK))));
		md.getBody().statements().add(vdsrules2);

		VariableDeclarationFragment vdflimit = ast.newVariableDeclarationFragment();
		vdflimit.setName(ast.newSimpleName("limit"));
		MethodInvocation miforlimit = ast.newMethodInvocation();
		miforlimit.setName(ast.newSimpleName("length"));
		miforlimit.setExpression(ast.newSimpleName("p"));
		vdflimit.setInitializer(makeInfixExpression(miforlimit, ast.newNumberLiteral(tableSize), Operator.MINUS));

		VariableDeclarationStatement vdsfl = ast.newVariableDeclarationStatement(vdflimit);
		md.getBody().statements().add(vdsfl);

		ForStatement fs = ast.newForStatement();

		fs.setExpression(makeInfixExpression(ast.newSimpleName("i"), ast.newSimpleName("limit"), Operator.LESS_EQUALS));
		Expression e = makeInfixExpression(ast.newSimpleName("i"), ast.newNumberLiteral(tableSize), Operator.PLUS);
		Expression e1 = makeAssignment(ast.newSimpleName("i"), e, Assignment.Operator.ASSIGN);
		fs.updaters().add(e1);

		VariableDeclarationFragment vdfforint = ast.newVariableDeclarationFragment();
		vdfforint.setName(ast.newSimpleName("i"));
		vdfforint.setInitializer(ast.newNumberLiteral("0"));
		VariableDeclarationExpression vdeforinit = ast.newVariableDeclarationExpression(vdfforint);
		fs.initializers().add(vdeforinit);

		if (blacklist) {
			MethodInvocation mia = ast.newMethodInvocation();
			mia.setName(ast.newSimpleName("Array"));
			{
				MethodInvocation mib = ast.newMethodInvocation();
				mib.setName(ast.newSimpleName(Constants.BLOCK));
				{

					MethodInvocation mif = ast.newMethodInvocation();
					mif.setName(ast.newSimpleName(Constants.IF));
					{ // Output: If(mic,newib,NoOp)
						MethodInvocation mic = ast.newMethodInvocation();
						mic.setName(ast.newSimpleName(Constants.RULE));
						{ // Output: Constrain(mic,postParsef(ConstantValue(cvmi)))
							mic.arguments().add(ast.newSimpleName(fieldMapping(params.get(it))));
							MethodInvocation mitt = ast.newMethodInvocation();
							// postParsef=> :==: Function in SEFL invalid in Java! Need post-parser
							mitt.setName(ast.newSimpleName("postParsef"));
							Expression cvmi = newconstatvalue(params.get(it), 0);
							mitt.arguments().add(cvmi);
							mic.arguments().add(mitt);
						}
						mif.arguments().add(mic);

						it++;
						mif.arguments().add(newib(it)); // If-Branch-True
						mif.arguments().add(ast.newName("NoOp")); // If-Branch-False
					}
					mib.arguments().add(mif);
				}
				mia.arguments().add(mib);
			}
			Expression earule = makeAssignment(ast.newSimpleName("rule"), mia, Assignment.Operator.ASSIGN);
			Block fb = ast.newBlock();
			fb.statements().add(ast.newExpressionStatement(earule));
			fb.statements().add(ast.newExpressionStatement(newconcatlist()));
			fs.setBody(fb); // fs=ForStatement

			md.getBody().statements().add(fs);
		} else { // White-list
			MethodInvocation mia = ast.newMethodInvocation(); // InstructionBlock SEFL to scan the table entries and SET
																// the flag-match
			mia.setName(ast.newSimpleName("Array"));
			{

				MethodInvocation mib = ast.newMethodInvocation();
				mib.setName(ast.newSimpleName(Constants.BLOCK));

				MethodInvocation mif = ast.newMethodInvocation();
				mif.setName(ast.newSimpleName(Constants.IF));
				{
					MethodInvocation mic = ast.newMethodInvocation();
					mic.setName(ast.newSimpleName(Constants.RULE));
					{
						mic.arguments().add(makeStringLiteral("flag"));
						MethodInvocation mitt = ast.newMethodInvocation();
						mitt.setName(ast.newSimpleName("postParsef"));
						MethodInvocation micv = ast.newMethodInvocation();
						micv.setName(ast.newSimpleName("ConstantValue"));
						micv.arguments().add(ast.newNumberLiteral("0"));
						mitt.arguments().add(micv);
						mic.arguments().add(mitt);
					}
					mif.arguments().add(mic);
				}
				mif.arguments().add(newib(it));
				mif.arguments().add(ast.newName("NoOp"));
				mib.arguments().add(mif);
				mia.arguments().add(mib);
			}
			Expression earule = makeAssignment(ast.newSimpleName("rule"), mia, Assignment.Operator.ASSIGN);
			Block fb = ast.newBlock();
			fb.statements().add(ast.newExpressionStatement(earule));
			fb.statements().add(ast.newExpressionStatement(newconcatlist()));
			fs.setBody(fb);
			md.getBody().statements().add(fs);

			MethodInvocation mia2 = ast.newMethodInvocation(); // InstructionBlock SEFL to CHECK the flag-match
			mia2.setName(ast.newSimpleName("Array"));
			{
				MethodInvocation mib2 = ast.newMethodInvocation();
				mib2.setName(ast.newSimpleName(Constants.BLOCK));

				MethodInvocation mif2 = ast.newMethodInvocation();
				mif2.setName(ast.newSimpleName(Constants.IF));
				{
					MethodInvocation mic = ast.newMethodInvocation();
					mic.setName(ast.newSimpleName(Constants.RULE));
					{
						mic.arguments().add(makeStringLiteral("flag"));
						MethodInvocation mitt = ast.newMethodInvocation();
						mitt.setName(ast.newSimpleName("postParsef"));
						MethodInvocation micv = ast.newMethodInvocation();
						micv.setName(ast.newSimpleName("ConstantValue"));
						micv.arguments().add(ast.newNumberLiteral("0"));
						mitt.arguments().add(micv);
						mic.arguments().add(mitt);
					}
					mif2.arguments().add(mic);
				}
				mif2.arguments().add(newfail("No-Match"));
				mif2.arguments().add(ast.newName("NoOp"));
				mib2.arguments().add(mif2);
				mia2.arguments().add(mib2);
			}
			Expression ea2 = makeAssignment(ast.newSimpleName("rule"), mia2, Assignment.Operator.ASSIGN);
			md.getBody().statements().add(ast.newExpressionStatement(ea2));
			md.getBody().statements().add(ast.newExpressionStatement(newconcatlist()));
		}
		ReturnStatement rs = ast.newReturnStatement();
		rs.setExpression((ast.newSimpleName("rules")));
		md.getBody().statements().add(rs);
		return md;
	}
	
	@SuppressWarnings("unchecked")
	public MethodDeclaration generateCheckState() {
		if (!state)
			return null;	
		flagstate = true;
		String numEntryState = Integer.toString(statetype.size());
		MethodDeclaration md = ast.newMethodDeclaration();
		md.setName(ast.newSimpleName("checkstate"));
		md.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

		ArrayType at1 = ast.newArrayType(ast.newSimpleType(ast.newSimpleName(Constants.BLOCK)));
		md.setReturnType2(at1);

		/**
		 * The variable <strong>p</strong> is the array that contains the entries given
		 * to the network function being configured
		 */
		SingleVariableDeclaration param = ast.newSingleVariableDeclaration();
		param.setName(ast.newSimpleName("p"));
		param.setType(ast.newArrayType(ast.newSimpleType(ast.newName("ConfigParameter"))));
		md.parameters().add(param);

		md.setBody(ast.newBlock());

		/**
		 * The variable <strong>rule</strong> is the InstructionBlock SEFL statement
		 * generate to each entries given to the network function being configured.
		 */
		VariableDeclarationFragment vdfrule = ast.newVariableDeclarationFragment();
		vdfrule.setName(ast.newSimpleName("rule"));
		VariableDeclarationStatement vdsrule = ast.newVariableDeclarationStatement(vdfrule);
		vdsrule.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName(Constants.BLOCK))));
		md.getBody().statements().add(vdsrule);

		/**
		 * The variable <strong>rules</strong> is the array that contains all the
		 * <strong>rule</strong> statements.
		 */
		VariableDeclarationFragment vdfrules = ast.newVariableDeclarationFragment();
		vdfrules.setName(ast.newSimpleName("rules"));
		MethodInvocation miinizializer = ast.newMethodInvocation();
		miinizializer.setName(ast.newSimpleName("Array"));
		{
			MethodInvocation miib = ast.newMethodInvocation();
			miib.setName(ast.newSimpleName(Constants.BLOCK));
			miib.arguments().add(ast.newSimpleName("Nil"));
			miinizializer.arguments().add(miib);
		}
		vdfrules.setInitializer(miinizializer);
		VariableDeclarationStatement vdsrules2 = ast.newVariableDeclarationStatement(vdfrules);
		vdsrules2.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName(Constants.BLOCK))));
		md.getBody().statements().add(vdsrules2);

		VariableDeclarationFragment vdflimit = ast.newVariableDeclarationFragment();
		vdflimit.setName(ast.newSimpleName("limit"));
		MethodInvocation miforlimit = ast.newMethodInvocation();
		miforlimit.setName(ast.newSimpleName("length"));
		miforlimit.setExpression(ast.newSimpleName("p"));
		vdflimit.setInitializer(makeInfixExpression(miforlimit, ast.newNumberLiteral(numEntryState), Operator.MINUS));

		VariableDeclarationStatement vdsfl = ast.newVariableDeclarationStatement(vdflimit);
		md.getBody().statements().add(vdsfl);

		ForStatement fs = ast.newForStatement();

		fs.setExpression(makeInfixExpression(ast.newSimpleName("i"), ast.newSimpleName("limit"), Operator.LESS_EQUALS));
		Expression e = makeInfixExpression(ast.newSimpleName("i"), ast.newNumberLiteral(numEntryState), Operator.PLUS);
		Expression e1 = makeAssignment(ast.newSimpleName("i"), e, Assignment.Operator.ASSIGN);
		fs.updaters().add(e1);

		VariableDeclarationFragment vdfforint = ast.newVariableDeclarationFragment();
		vdfforint.setName(ast.newSimpleName("i"));
		vdfforint.setInitializer(ast.newNumberLiteral("0"));
		VariableDeclarationExpression vdeforinit = ast.newVariableDeclarationExpression(vdfforint);
		fs.initializers().add(vdeforinit);

		MethodInvocation mia = ast.newMethodInvocation(); // InstructionBlock SEFL to scan the table entries and SET
															// the flag-match
		mia.setName(ast.newSimpleName("Array"));
		{

			MethodInvocation mib = ast.newMethodInvocation();
			mib.setName(ast.newSimpleName(Constants.BLOCK));

			MethodInvocation mif = ast.newMethodInvocation();
			mif.setName(ast.newSimpleName(Constants.IF));
			{
				MethodInvocation mic = ast.newMethodInvocation();
				mic.setName(ast.newSimpleName(Constants.RULE));
				{
					mic.arguments().add(makeStringLiteral("flag"));
					MethodInvocation mitt = ast.newMethodInvocation();
					mitt.setName(ast.newSimpleName("postParsef"));
					MethodInvocation micv = ast.newMethodInvocation();
					micv.setName(ast.newSimpleName("ConstantValue"));
					micv.arguments().add(ast.newNumberLiteral("0"));
					mitt.arguments().add(micv);
					mic.arguments().add(mitt);
				}
				mif.arguments().add(mic);
			}
			mif.arguments().add(newibstate(0));
			mif.arguments().add(ast.newName("NoOp"));
			mib.arguments().add(mif);
			mia.arguments().add(mib);
		}
		Expression earule = makeAssignment(ast.newSimpleName("rule"), mia, Assignment.Operator.ASSIGN);
		Block fb = ast.newBlock();
		fb.statements().add(ast.newExpressionStatement(earule));
		fb.statements().add(ast.newExpressionStatement(newconcatlist()));
		fs.setBody(fb);
		md.getBody().statements().add(fs);

		md.getBody().statements().add(ast.newExpressionStatement(newconcatlist()));

		ReturnStatement rs = ast.newReturnStatement();
		rs.setExpression((ast.newSimpleName("rules")));
		md.getBody().statements().add(rs);
		return md;
	}

	/**
	 * Generate the SEFL instructions of initialization, e.g. Assign flag value.
	 */
	@SuppressWarnings("unchecked")
	private void intGenerateRule() {
		packetfield = Arrays.asList(Constants.IP_SOURCE, Constants.IP_DESTINATION, Constants.PROTO, Constants.ORIGIN,
				Constants.ORIG_BODY, Constants.BODY, Constants.SEQUENCE, Constants.EMAIL_FROM, Constants.URL,
				Constants.OPTIONS, Constants.INNER_SRC, Constants.INNER_DEST, Constants.ENCRYPTED);
		// Add Flag
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName(Constants.ASSIGN));
		mi.arguments().add(makeStringLiteral("flag"));
		MethodInvocation micv = ast.newMethodInvocation();
		micv.setName(ast.newSimpleName("ConstantValue"));
		micv.arguments().add(ast.newNumberLiteral("0"));
		mi.arguments().add(micv);
		startblock.arguments().add(mi);
	}

	/**
	 * It receives an XML document node as input parameter and is responsible for
	 * calling the method associated with the node type to translate it into SEFL
	 * instructions. <br>
	 * 
	 * @param obj node
	 * @return the SEFL instruction generate
	 */
	private Expression getType(ExpressionObject obj) {
		if (obj.getAnd() != null) {

			return generateAnd(obj.getAnd());
		} else if (obj.getOr() != null) {

			return generateOr(obj.getOr());
		} else if (obj.getNot() != null) {

			return generateNot(obj.getNot());
		} else if (obj.getEqual() != null) {

			return generateEqual(obj.getEqual());
		} else if (obj.getImplies() != null) {

			return generateImplies(obj.getImplies());
		} else if (obj.getExist() != null) {

			return generateExist(obj.getExist());
		} else if (obj.getFieldOf() != null) {

			return generateFieldOf(obj.getFieldOf());
		} /*else if (obj.getIsInternal() != null) {

			return generateIsInternal(obj.getIsInternal());
		}*/ else if (obj.getMatchEntry() != null) {

			return generateMatchEntry(obj.getMatchEntry());
		} else if (obj.getParam() != null) {
			return ast.newSimpleName(obj.getParam());
		} else
			return null;
	}

	/**
	 * Generate a SEFL instruction for the node type LFFieldOf. <br>
	 * It generates a String with the value associate to the field. <br>
	 * 
	 * @param fieldOf The AST node of type LFFieldOf
	 * @return the new Method Invocation that represent the LFFieldOf type in SEFL
	 */
	@SuppressWarnings("unchecked")
	private Expression generateFieldOf(LFFieldOf fieldOf) {
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("field"));
		String value = fieldOf.getField();
		mi.arguments().add(ast.newSimpleName(value));
		return mi;
	}

/*	*//**
	 * Generate a SEFL instruction for the node type LFIsInternal. <br>
	 * For example, <br>
	 * isInternal(IP_SRC)
	 * 
	 * @param isInternal The AST node of type LFIsInternal
	 * @return the new Method Invocation that represent the LFIsInternal type in
	 *         SEFL
	 *//*
	@SuppressWarnings("unchecked")
	private Expression generateIsInternal(LFIsInternal isInternal) {
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("isInternal"));
		mi.arguments().add(ast.newSimpleName(isInternal.getFieldOf().getField()));
		return mi;
	}*/

	/**
	 * It sets the <strong>blacklist</strong> variable/falg to differentiate the
	 * behavior. <br>
	 * It also sets the <strong>match</strong> variable/flag to inform the
	 * translator that a LFMatchEntry node has been found and that the associated
	 * method must be written to the output file. <br>
	 * For example, <br>
	 * InstructionBlock(addrule(params))
	 * 
	 * @param matchEntry The AST node of type LFMatchEntry
	 * @return null
	 */
	private Expression generateMatchEntry(LFMatchEntry matchEntry) {
		if (flagnot) {
			blacklist = true;
		}
		params = new ArrayList<String>();
		for (LFFieldOf temp : matchEntry.getValue()) {
			params.add(temp.getField());
		}
		match = true;
		return null;
	}

	/**
	 * Generate a SEFL instruction for all expressions of the LOAnd. <br>
	 * 
	 * @param and The AST node of type LOAnd
	 * @return null
	 */
	@SuppressWarnings("unchecked")
	private Expression generateAnd(LOAnd and) {
		for (ExpressionObject temp : and.getExpression()) {
			Expression exp = getType(temp);
			if (exp == null) {
				continue;
			}
			if (temp.getOr() != null) {
				continue;
			}
			if(state) {
				stateblockint.arguments().add(exp);
			} else {
				startblock.arguments().add(exp);
			}
		}
		return null;
	}

	/**
	 * Generate a SEFL instruction for the node type LOOr. <br>
	 * It generates recursively a set of If-Constraint to each Or expressions.
	 * The nested Ifs are make by <strong>nweNestedIf</strong> method. 
	 * For example, <br>
	 * ( A || B || C )  =
	 * If(Constrain(A),
	 * 		NoOp,
	 *      If(Constrain(B),
	 *      	NoOp
	 *      	Constrain(C) 
	 *      )
	 * )
	 * @param or The AST node of type LOOr
	 * @return the new Method Invocation that represent the LOOr type in SEFL
	 */
	@SuppressWarnings("unchecked")
	private Expression generateOr(LOOr or) {
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName(Constants.IF));

		ArrayList<Expression> exps = new ArrayList<Expression>();
		for (ExpressionObject temp : or.getExpression()) {
			Expression exp = getType(temp);
			if (exp == null) {
				continue;
			}
			exps.add(exp);
		}
		mi.arguments().add(exps.get(0));
		mi.arguments().add(ast.newSimpleName("NoOp"));
		mi.arguments().add(newNestedIf(exps, 1));
		if(state) {
			stateblockint.arguments().add(mi);
		}else {
		startblock.arguments().add(mi);
		}return mi;
	}

	@SuppressWarnings("unchecked")
	private Expression newNestedIf(ArrayList<Expression> exps, int i) {
		if (i == exps.size() - 1) {
			return exps.get(exps.size() - 1);
		}
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName(Constants.IF));
		mi.arguments().add(exps.get(i));
		mi.arguments().add(ast.newSimpleName("NoOp"));
		mi.arguments().add(newNestedIf(exps, ++i));
		return mi;
	}

	/**
	 * If the node type is a LONot, the method sets the <strong>flagnot</strong>
	 * variable, so the nested element can operate differently if the statement is
	 * negative. <br>
	 * Before the return to the call the <strong>flagnot</strong> variable is reset
	 * to the positive value.
	 * 
	 * @param not The AST node of type LONot
	 * @return null
	 */
	private Expression generateNot(LONot not) {
		flagnot = true;
		getType(not.getExpression());
		flagnot = false;
		return null;
	}

	/**
	 * Generate a SEFL instruction for the node type LOEquals. <br>
	 * For example, <br>
	 * Constrain(IPSrc, :==:(ConstantValue(ipToNumber(p(i)(0))))),
	 * 
	 * @param equal The AST node
	 * @return the new Method Invocation that represent the Equals type in SEFL or
	 *         null if the equal node is about the field interface_send.
	 */
	@SuppressWarnings("unchecked")
	private Expression generateEqual(LOEquals equal) {
		String rfield; // Right expression of equal
		String lfield; // Left expression of equal

		MethodInvocation mi = ast.newMethodInvocation();

		if (equal.getRightExpression().getParam() != null) {
			rfield = equal.getRightExpression().getParam();
		} else {
			rfield = equal.getRightExpression().getFieldOf().getField();
		}
		lfield = equal.getLeftExpression().getFieldOf().getField();
		if (rfield == null) {
			rfield = equal.getRightExpression().getParam();
		}

		if (equal.getLeftExpression().getFieldOf().getUnit().equals("p_0")) { // p_0 => the field is on the
																				// output_packet.
			if (lfield != null && rfield != null && !lfield.equals(rfield)) {
				if ((equal.getRightExpression().getFieldOf() != null)
						&& equal.getRightExpression().getFieldOf().getUnit().equals("p_1")
						&& lfield.contains(Constants.IP_SOURCE)) {
					newSwitchIpRules();
					return null;
				} else if ((equal.getRightExpression().getFieldOf() != null)
						&& equal.getRightExpression().getFieldOf().getUnit().equals("p_1")
						&& lfield.contains(Constants.IP_DESTINATION)) {
					return null;
				}
				if (packetfield.contains(lfield)) {
					mi.setName(ast.newSimpleName(Constants.ASSIGN));

					mi.arguments().add(ast.newSimpleName(fieldMapping(lfield)));
					MethodInvocation micv = ast.newMethodInvocation();
					micv.setName(ast.newSimpleName("ConstantValue"));
					QualifiedName qn = ast.newQualifiedName(ast.newSimpleName(fieldMapping(rfield)),
							ast.newSimpleName("value"));
					micv.arguments().add(qn);
					mi.arguments().add(micv);
					assigvalues.arguments().add(mi);
				}
			}else if (lfield!=null && rfield!=null) {
				if ((equal.getRightExpression().getFieldOf() != null)
						&& equal.getRightExpression().getFieldOf().getUnit().equals("p_2")) {
					newMatchState(lfield);
					return null;
				}	
			}
		} else if (lfield != null && rfield != null && !lfield.equals(rfield)) { // p_1 => field of the input_packet
			if (packetfield.contains(lfield)) {
				mi.setName(ast.newSimpleName(Constants.RULE));

				mi.arguments().add(ast.newSimpleName(fieldMapping(lfield)));
				MethodInvocation mitt = ast.newMethodInvocation();
				mitt.setName(ast.newSimpleName("postParsef"));
				MethodInvocation micv = ast.newMethodInvocation();
				micv.setName(ast.newSimpleName("ConstantValue"));
				QualifiedName qn = ast.newQualifiedName(ast.newSimpleName(fieldMapping(rfield)),
						ast.newSimpleName("value"));
				micv.arguments().add(qn);
				mitt.arguments().add(micv);
				mi.arguments().add(mitt);
				return mi;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void newMatchState(String lfield) {
    if (state) {
    	statetype.add(lfield);
    }
    else {
		state = true;
		statetype = new ArrayList<>();
		statetype.add(lfield);
		
		MethodInvocation miib = ast.newMethodInvocation();
		miib.setName(ast.newSimpleName(Constants.BLOCK));
		{
			MethodInvocation mi = ast.newMethodInvocation();
			mi.setName(ast.newSimpleName("checkstate"));
			mi.arguments().add(ast.newSimpleName("params"));
			miib.arguments().add(mi);
		}
		
		startblock.arguments().add(miib);
		
		stateblock = ast.newMethodInvocation();
		stateblock.setName(ast.newSimpleName(Constants.IF));
		{ // Output: If(flaf,aggiorna,NoOp)
			MethodInvocation mic = ast.newMethodInvocation();
			mic.setName(ast.newSimpleName(Constants.RULE));
			{ // Output: Constrain(mic,postParsef(ConstantValue(cvmi)))
				MethodInvocation mitt = ast.newMethodInvocation();
				// postParsef=> :==: Function in SEFL invalid in Java! Need post-parser
				mitt.setName(ast.newSimpleName("postParsef"));
				MethodInvocation micv = ast.newMethodInvocation();
				micv.setName(ast.newSimpleName("ConstantValue"));
				micv.arguments().add(ast.newNumberLiteral("1"));
				mitt.arguments().add(micv);
				mic.arguments().add(makeStringLiteral("flag"));
				mic.arguments().add(mitt);
			}
			stateblock.arguments().add(mic);
			stateblockint = ast.newMethodInvocation();
			stateblockint.setName(ast.newSimpleName(Constants.BLOCK));
		}
    }
}

	/**
	 * Generate SEFL instructions to switch the header field IP_SOURCE and
	 * IP_DESRINARION <br>
	 * For Example, <br>
	 * Allocate("tmp"), Assign("tmp",:@(IPSrc)), Assign(IPSrc,:@(IPDst)),
	 * Assign(IPDst,:@("tmp")), Deallocate("tmp"),
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void newSwitchIpRules() {
		MethodInvocation miatmp = ast.newMethodInvocation();
		miatmp.setName(ast.newSimpleName(Constants.ALLOCATE));
		miatmp.arguments().add(makeStringLiteral("tmp"));
		assigvalues.arguments().add(miatmp);
		
		MethodInvocation mit = ast.newMethodInvocation();
		mit.setName(ast.newSimpleName(Constants.ASSIGN));
		mit.arguments().add(makeStringLiteral("tmp"));
		mit.arguments().add(newFchiocciola(fieldMapping(Constants.IP_SOURCE)));
		assigvalues.arguments().add(mit);
		
		MethodInvocation miips = ast.newMethodInvocation();
		miips.setName(ast.newSimpleName(Constants.ASSIGN));
		miips.arguments().add(ast.newSimpleName(fieldMapping(Constants.IP_SOURCE)));
		miips.arguments().add(newFchiocciola(fieldMapping(Constants.IP_DESTINATION)));
		assigvalues.arguments().add(miips);
		
		MethodInvocation miipd = ast.newMethodInvocation();
		miipd.setName(ast.newSimpleName(Constants.ASSIGN));
		miipd.arguments().add(ast.newSimpleName(fieldMapping(Constants.IP_DESTINATION)));
		miipd.arguments().add(newFchiocciola(makeStringLiteral("tmp")));
		assigvalues.arguments().add(miipd);

		MethodInvocation midtmp = ast.newMethodInvocation();
		midtmp.setName(ast.newSimpleName(Constants.DEALLOCATE));
		midtmp.arguments().add(makeStringLiteral("tmp"));
		assigvalues.arguments().add(midtmp);
	}


	/**
	 * Mapping from VNF Modeling fields to SymNet SEFL fields <br>
	 * 
	 * @param field of VNF Modeling
	 * @return field of SymNet
	 */
	private String fieldMapping(String field) {
		switch (field) {
		case ("POP3_REQUEST"):
			return "POP3REQUEST";
		case ("POP3_RESPONSE"):
			return "POP3RESPONSE";
		case ("HTTP_REQUEST"):
			return "HTTPREQUEST";
		case ("HTTP_RESPONSE"):
			return "HTTPRESPONSE";
		case ("natIp"):
			return "natIp";
		case ("IP_SRC"):
			return "IPSrc";
		case ("IP_DST"):
			return "IPDst";
		case ("PROTO"):
			return "Proto";
		case ("PORT_SRC"):
			return "PortSrc";
		case ("PORT_DST"):
			return "PortDst";
		case ("ORIGIN"):
			return "Origin";
		case ("ORIG_BODY"):
			return "OriginBody";
		case ("BODY"):
			return "Body";// = application data
		case ("SEQUENCE"):
			return "Sequence";
		case ("EMAIL_FROM"):
			return "EmailFrom";
		case ("URL"):
			return "URL";
		case ("OPTIONS"):
			return "Options";
		case ("INNER_SRC"):
			return "InnerSrc";
		case ("INNER_DEST"):
			return "InnerDst";
		case ("ENCRYPTED"):
			return "Encrypted";
		case ("RESPONSE"):
			return "RESPONSE";
		}

		return null;
	}

	/**
	 * Generate a SEFL instruction for the node type LOImplies. <br>
	 * It checks recursively the nested nodes of the type. 
	 * 
	 * @param implies The AST node
	 * @return null
	 */
	private Expression generateImplies(LOImplies implies) {

		getType(implies.getAntecedentExpression());
		getType(implies.getConsequentExpression());
		return null;
	}

	/**
	 * Generate a SEFL instruction for the nested node of type Exist.
	 * 
	 * @param exist The AST node
	 * @return the new Method Invocation that represent the Exit type in SEFL or
	 *         null if the exist element do not has nested element.
	 */
	@SuppressWarnings("unchecked")
	private Expression generateExist(LOExist exist) {

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("nstat"));

		Expression exp = getType(exist.getExpression());
		if (exp == null) {
			return null;
		}
		mi.arguments().add(exp);
		return mi;

	}

	// ---------------------------------------------------------
	// Method to generate AST element:
	// ---------------------------------------------------------

	/**
	 * Generate an InfixExpression expression AST node type <br>
	 * InfixExpression.Operator: *,/,%,+,-,<<,>>,<,>,<=,>=,==,!=,^,&,|,&&,||
	 * 
	 * @param lo The "leftOperand" structural property of this node type
	 * @param ro The "rightOperand" structural property of this node type
	 * @param o  The "operator" structural property of this node type
	 * @return the InfixExpression
	 * @see Class InfixExpression of org.eclipse.jdt.core.dom
	 */
	private Expression makeInfixExpression(Expression lo, Expression ro, Operator o) {
		InfixExpression ie = ast.newInfixExpression();
		ie.setLeftOperand(lo);
		ie.setRightOperand(ro);
		ie.setOperator(o);
		return ie;
	}

	/**
	 * Generate an Assignment expression AST node type
	 * 
	 * @param lo The "leftHandSide" structural property of this node type
	 * @param ro The "rightHandSide" structural property of this node type
	 * @param o  The "operator" structural property of this node type
	 * @return the Assignment expression
	 * @see Class Assignment of org.eclipse.jdt.core.dom
	 */
	private Expression makeAssignment(Expression lo, Expression ro, Assignment.Operator o) {
		Assignment a = ast.newAssignment();
		a.setLeftHandSide(lo);
		a.setRightHandSide(ro);
		a.setOperator(o);
		return a;
	}

	/**
	 * Generate a String Literal node of AST
	 * 
	 * @param str the string to put in the new AST node as String Literal, the
	 *            string value without enclosing double quotes and embedded escapes
	 * @return the String Literal node with value set to str
	 * @throws IllegalArgumentException - if the argument is incorrect
	 * @see Class StringLiteral of org.eclipse.jdt.core.dom
	 */
	private StringLiteral makeStringLiteral(String str) {
		StringLiteral sl = ast.newStringLiteral();
		sl.setLiteralValue(str);
		return sl;
	}

	// ---------------------------------------------------------
	// Generate new method for SEFL instructions:
	// ---------------------------------------------------------

	/**
	 * Generate a new method that represents a Fail SEFL instruction. <br>
	 * For example, <br>
	 * Fail("message")
	 * 
	 * @param msg is a message. It is the argument of the Fail SEFL instruction and
	 *            it describes why a failure occurs.
	 * @return a Method Invocation that represent the Fail SEFL instruction
	 */
	@SuppressWarnings("unchecked")
	private Expression newfail(String msg) {
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName(Constants.FAIL));
		mi.arguments().add(makeStringLiteral(msg));
		return mi;
	}

	/**
	 * The method newInstructionBlock is a recursive method to generate an
	 * Instruction Block SEFL rule for each table entry of the network function.
	 * <br>
	 * The method generates a different set of instructions for the white list and
	 * for the black list. <br>
	 * For example, <br>
	 * InstructionBlock( If( Constrain(Tag("IP_SRC"),
	 * :==:(ConstantValue(ipToNumber(p(i)(0))))), InstructionBlock( If(
	 * Constrain(Tag("IPDst"), :==:(ConstantValue(ipToNumber(p(i)(1))))),
	 * Fail("Dropped"), NoOp)), NoOp ))
	 * 
	 * @param index position of an element in the table of network function.
	 * @return If the index is a valid value, the method return an Invocation Method
	 *         that represents a new instruction block for the element in the table
	 *         of network function at the index position.<br>
	 *         Otherwise, If all the items in the table have been visited, the
	 *         method returns a failed SEFL statement for the blacklist or an Assign
	 *         SEFL statement for the whitelist. <br>
	 */
	@SuppressWarnings("unchecked")
	private Expression newib(int index) {
		if (index == params.size()) {
			if (blacklist) {
				return newfail("Match-in-blacklist");
			} else {

				MethodInvocation miib = ast.newMethodInvocation();
				miib.setName(ast.newSimpleName(Constants.BLOCK));
				{
					MethodInvocation mi = ast.newMethodInvocation();
					mi.setName(ast.newSimpleName(Constants.ASSIGN));
					mi.arguments().add(makeStringLiteral("flag"));
					MethodInvocation micv = ast.newMethodInvocation();
					micv.setName(ast.newSimpleName("ConstantValue"));
					micv.arguments().add(ast.newNumberLiteral("1"));
					mi.arguments().add(micv);
					miib.arguments().add(mi);
				}
				{	//UPDATE: set the Interface assign only if the 'param' is an interface!
					if(!flagstate ) {
					MethodInvocation mi = ast.newMethodInvocation();
					mi.setName(ast.newSimpleName(Constants.ASSIGN));
					mi.arguments().add(makeStringLiteral("idIfSend"));
					Expression ecv = newconstatvalue("if", 1);
					mi.arguments().add(ecv);
					miib.arguments().add(mi);
					}
				}
				return miib;
			}

		}
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName(Constants.BLOCK));

		MethodInvocation imi = ast.newMethodInvocation();
		imi.setName(ast.newSimpleName(Constants.IF));
		{
			MethodInvocation cmi = ast.newMethodInvocation();
			cmi.setName(ast.newSimpleName(Constants.RULE));
			cmi.arguments().add(ast.newSimpleName(fieldMapping(params.get(index))));
			MethodInvocation mitt = ast.newMethodInvocation();
			mitt.setName(ast.newSimpleName("postParsef"));
			Expression cv = newconstatvalue(params.get(index), index);
			mitt.arguments().add(cv);
			cmi.arguments().add(mitt);
			imi.arguments().add(cmi);
		}
		index++;
		imi.arguments().add(newib(index)); // true-branch of IF-statement
		imi.arguments().add(ast.newName("NoOp")); // false-branch of IF-statement
		mi.arguments().add(imi);
		return mi;
	}
	
	@SuppressWarnings("unchecked")
	private Expression newibstate(int index) {
		if (index == statetype.size()) {
			{
				MethodInvocation miib = ast.newMethodInvocation();
				miib.setName(ast.newSimpleName(Constants.BLOCK));
				{
					MethodInvocation mi = ast.newMethodInvocation();
					mi.setName(ast.newSimpleName(Constants.ASSIGN));
					mi.arguments().add(makeStringLiteral("flag"));
					MethodInvocation micv = ast.newMethodInvocation();
					micv.setName(ast.newSimpleName("ConstantValue"));
					micv.arguments().add(ast.newNumberLiteral("1"));
					mi.arguments().add(micv);
					miib.arguments().add(mi);
				}
				return miib;
			}
		}
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName(Constants.BLOCK));

		MethodInvocation imi = ast.newMethodInvocation();
		imi.setName(ast.newSimpleName(Constants.IF));
		{
			MethodInvocation cmi = ast.newMethodInvocation();
			cmi.setName(ast.newSimpleName(Constants.RULE));
			cmi.arguments().add(ast.newSimpleName(fieldMapping(statetype.get(index))));
			MethodInvocation mitt = ast.newMethodInvocation();
			mitt.setName(ast.newSimpleName("postParsef"));
			Expression cv = newconstatvalue("p", index);
			mitt.arguments().add(cv);
			cmi.arguments().add(mitt);
			imi.arguments().add(cmi);
		}
		index++;
		imi.arguments().add(newibstate(index)); // true-branch of IF-statement
		imi.arguments().add(ast.newName("NoOp")); // false-branch of IF-statement
		mi.arguments().add(imi);
		return mi;
	}

	/**
	 * Generate a new Method Invocation that represent a SEFL ConstantValue
	 * constructor for the input parameters. <br>
	 * This method is inserted into a Constraint method that checks whether a field
	 * in the packet is equal to the value in the table of network functions.
	 * ConstantValue represents a value of the network function table. So we need to
	 * have the name of the field to be checked and the index of the table element.
	 * Example: ConstantValue(ipToNumber(p(i)(0))))
	 * 
	 * @param str   is the field of the packet to which you want to assign the
	 *              constant value generated by the method.
	 * @param index is an int value that represents the index of the element in the
	 *              table.
	 * @return The ConstantValue Method Invocation generate.
	 */
	@SuppressWarnings("unchecked")
	private Expression newconstatvalue(String str, int index) {

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("ConstantValue"));
		MethodInvocation mie = ast.newMethodInvocation();
		mie.setName(ast.newSimpleName("p"));
		mie.arguments().add(makeInfixExpression(ast.newSimpleName("i"), ast.newNumberLiteral(String.valueOf(index)),
				InfixExpression.Operator.PLUS));
		FieldAccess fap = ast.newFieldAccess();
		fap.setExpression(mie);
		fap.setName(ast.newSimpleName("value"));

		if (str.equals("IP_SRC") || str.equals("IP_DST")) {
			MethodInvocation imi = ast.newMethodInvocation();
			imi.setName(ast.newSimpleName("ipToNumber"));
			imi.arguments().add(fap);
			mi.arguments().add(imi);
		} else {
			FieldAccess faint = ast.newFieldAccess();
			faint.setName(ast.newSimpleName("toInt"));
			faint.setExpression(fap);
			mi.arguments().add(faint);
		}
		return mi;
	}

	/**
	 * Generate a new Method Invocation that represent an assignment. <br>
	 * The concatenation of the <strong>rule</strong> and <strong>rules</strong>
	 * arrays is assigned to the variable <strong>rules</strong>. <br>
	 * It allows you to create an array containing a set of SEFL rules. <br>
	 * Example: rules = Array.concat(rules, rule);
	 * 
	 * @return The Assignment expression generate.
	 */
	@SuppressWarnings("unchecked")
	private Expression newconcatlist() {
		MethodInvocation meconcat = ast.newMethodInvocation();
		meconcat.setName(ast.newSimpleName("concat"));
		meconcat.setExpression(ast.newSimpleName("Array"));
		meconcat.arguments().add(ast.newSimpleName("rules"));
		meconcat.arguments().add(ast.newSimpleName("rule"));
		Expression ea = makeAssignment(ast.newSimpleName("rules"), meconcat, Assignment.Operator.ASSIGN);
		return ea;
	}
	
	/**
	 * Generate a SEFL instruction to access of tag values. <br>
	 * For example, <br>
	 * :@(IPSrc))
	 * 
	 * @param val tag name
	 * @return the new Method Invocation that represent the :@ SEFL method.
	 */
	@SuppressWarnings("unchecked")
	private Expression newFchiocciola(String val) {
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("Fchiocciola")); // => :@ SymNet symbol 
		mi.arguments().add(ast.newSimpleName(val));
		return mi;
	}
	@SuppressWarnings("unchecked")
	private Object newFchiocciola(StringLiteral val) {
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("Fchiocciola")); // => :@ SymNet symbol 
		mi.arguments().add(val);
		return mi;
	}

	// ---------------------------------------------------------
	// Method Setter/Getter to member class element:
	// ---------------------------------------------------------

	/**
	 * Returns the Expression Result of the JAXB process
	 * 
	 * @return Returns the internal Expression Result result.
	 */
	public ExpressionResult getReuslt() {
		return reuslt;
	}
}
