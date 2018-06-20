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
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
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

class RuleUnmarshallerS {

	private AST ast;
	private MethodInvocation startblock;

	private ExpressionResult reuslt;

	private List<String> params;
	private List<String> constants; // Same as Tag
	private List<String> packetfield;
	private Boolean match = false;
	private String interfacesend = "Default";

	public RuleUnmarshallerS(String fileName, String className, AST ast) throws MarshalException {

		this.ast = ast;
		this.params = new ArrayList<>();
		this.constants = new ArrayList<>();

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

	@SuppressWarnings("unchecked")
	public MethodDeclaration generateRule() {

		if (reuslt.getLogicalExpressionResult().size() < 1 || reuslt.getNodeOrPacket().size() < 1)
			return null;

		MethodDeclaration method = ast.newMethodDeclaration();
		method.setName(ast.newSimpleName("generate_rules"));
		method.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
		ArrayType at1 = ast.newArrayType(ast.newSimpleType(ast.newSimpleName("State")), 2);
		method.setReturnType2(at1);

		SingleVariableDeclaration param = ast.newSingleVariableDeclaration();
		param.setName(ast.newSimpleName("params"));
		param.setType(ast.newArrayType(ast.newSimpleType(ast.newName("String")), 2));

		method.parameters().add(param);

		method.setBody(ast.newBlock());

		for (ExpressionObject temp : reuslt.getLogicalExpressionResult()) {

			Assignment assignment = ast.newAssignment();

			VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
			vdf.setName(ast.newSimpleName("code"));
			VariableDeclarationExpression vde = ast.newVariableDeclarationExpression(vdf);
			vde.setType(ast.newSimpleType(ast.newSimpleName(Constants.BLOCK)));
			assignment.setLeftHandSide(vde);

			assignment.setOperator(org.eclipse.jdt.core.dom.Assignment.Operator.ASSIGN);

			startblock = ast.newMethodInvocation();
			startblock.setName(ast.newSimpleName(Constants.BLOCK));
			createPacketFieldTag();

			Expression toadd;
			if ((toadd = getType(temp)) != null)
				startblock.arguments().add(toadd);

			assignment.setRightHandSide(startblock);
			method.getBody().statements().add(ast.newExpressionStatement(assignment));
		}

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("code"));
		mi.arguments().add(ast.newName("State.clean"));
		mi.arguments().add(ast.newBooleanLiteral(true));
		method.getBody().statements().add(ast.newExpressionStatement(mi));

		return method;
	}

	@SuppressWarnings("unchecked")
	public MethodDeclaration generateMatch(List<String> tableTypes) {
		if (match == false)
			return null;
		MethodDeclaration md = ast.newMethodDeclaration();
		md.setName(ast.newSimpleName("addrule"));

		md.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
		ArrayType at1 = ast.newArrayType(ast.newSimpleType(ast.newName(Constants.BLOCK)));
		md.setReturnType2(at1);

		SingleVariableDeclaration param = ast.newSingleVariableDeclaration();
		param.setName(ast.newSimpleName("p"));
		param.setType(ast.newArrayType(ast.newSimpleType(ast.newName("String")), 2));
		md.parameters().add(param);

		md.setBody(ast.newBlock());
		int it = 0;

		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
		vdf.setName(ast.newSimpleName("rule"));
		VariableDeclarationStatement vds = ast.newVariableDeclarationStatement(vdf);
		vds.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName(Constants.BLOCK))));
		md.getBody().statements().add(vds);

		VariableDeclarationFragment vdfrs = ast.newVariableDeclarationFragment();
		vdfrs.setName(ast.newSimpleName("rules"));
		MethodInvocation miinizializer = ast.newMethodInvocation();
		miinizializer.setName(ast.newSimpleName("Array"));
		vdfrs.setInitializer(miinizializer);
		VariableDeclarationStatement vdsrs = ast.newVariableDeclarationStatement(vdfrs);
		vdsrs.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName(Constants.BLOCK))));
		md.getBody().statements().add(vdsrs);

		ForStatement fs = ast.newForStatement();

		MethodInvocation mii = ast.newMethodInvocation();
		mii.setName(ast.newSimpleName("length"));
		mii.setExpression(ast.newSimpleName("p"));
		fs.setExpression(makeInfixExpression(ast.newSimpleName("i"), mii, Operator.LESS));

		PostfixExpression pe = ast.newPostfixExpression();
		pe.setOperand(ast.newSimpleName("i"));
		pe.setOperator(PostfixExpression.Operator.INCREMENT);
		fs.updaters().add(pe);

		VariableDeclarationFragment svd = ast.newVariableDeclarationFragment();
		svd.setName(ast.newSimpleName("i"));
		svd.setInitializer(ast.newNumberLiteral("0"));
		VariableDeclarationExpression vde = ast.newVariableDeclarationExpression(svd);
		fs.initializers().add(vde);

		MethodInvocation mia = ast.newMethodInvocation();
		mia.setName(ast.newSimpleName("Array"));

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName(Constants.BLOCK));

		MethodInvocation imi = ast.newMethodInvocation();
		imi.setName(ast.newSimpleName(Constants.IF));
		{
			MethodInvocation cmi = ast.newMethodInvocation();
			cmi.setName(ast.newSimpleName(Constants.RULE));

			Expression tmi = newtag(params.get(it));
			cmi.arguments().add(tmi);
			Expression cvmi = newconstatvalue(params.get(it), 0);
			cmi.arguments().add(cvmi);

			imi.arguments().add(cmi);

		}
		it++;
		imi.arguments().add(newib(it));
		imi.arguments().add(ast.newName("NoOp"));
		mi.arguments().add(imi);
		mia.arguments().add(mi);

		Expression a = makeAssignment(ast.newSimpleName("rule"), mia, Assignment.Operator.ASSIGN);
		Block fb = ast.newBlock();
		fb.statements().add(ast.newExpressionStatement(a));

		MethodInvocation met = ast.newMethodInvocation();
		met.setName(ast.newSimpleName("concat"));
		met.setExpression(ast.newSimpleName("Array"));
		met.arguments().add(ast.newSimpleName("rules"));
		met.arguments().add(ast.newSimpleName("rule"));
		Expression ea = makeAssignment(ast.newSimpleName("rules"), met, Assignment.Operator.ASSIGN);

		fb.statements().add(ast.newExpressionStatement(ea));
		fs.setBody(fb);
		md.getBody().statements().add(fs);

		ReturnStatement rs = ast.newReturnStatement();
		rs.setExpression((ast.newSimpleName("rules")));
		md.getBody().statements().add(rs);

		return md;

	}

	@SuppressWarnings("unchecked")
	private void createPacketFieldTag() {
		// Init
		packetfield = Arrays.asList(Constants.IP_SOURCE, Constants.IP_DESTINATION, Constants.PORT_SOURCE,
				Constants.PORT_DESTINATION, Constants.PROTO, Constants.ORIGIN, Constants.ORIG_BODY, Constants.BODY,
				Constants.SEQUENCE, Constants.EMAIL_FROM, Constants.URL, Constants.OPTIONS, Constants.INNER_SRC,
				Constants.INNER_DEST, Constants.ENCRYPTED);

		MethodInvocation mi0 = ast.newMethodInvocation();
		mi0.setName(ast.newSimpleName(Constants.CREATE));
		mi0.arguments().add(makeStringLiteral(Constants.HSTART));
		mi0.arguments().add(ast.newNumberLiteral("0"));
		startblock.arguments().add(mi0);

		for (String s : packetfield) {
			// 1)Create; Ex -> CreateTag("IPSrc", Tag("L3HeaderStart") + 96),
			MethodInvocation mi = ast.newMethodInvocation();
			mi.setName(ast.newSimpleName(Constants.CREATE));
			mi.arguments().add(makeStringLiteral(s));

			Expression mit = newtag(Constants.HSTART);
			NumberLiteral nl = newHoffset(s);
			mi.arguments().add(makeInfixExpression(mit, nl, Operator.PLUS));
			startblock.arguments().add(mi);

			// 2)Allocate; Ex -> Allocate(Tag("IPSrc"), 32),
			// For simplification all tag are allocated in 32
			MethodInvocation mia = ast.newMethodInvocation();
			mia.setName(ast.newSimpleName(Constants.ALLOCATE));
			Expression mit1 = newtag(s);
			mia.arguments().add(mit1);
			mia.arguments().add(ast.newNumberLiteral("32"));
			startblock.arguments().add(mia);

			// 3)Assign; Ex -> Assign(Tag("IPDst"), SymbolicValue()),
			MethodInvocation mi2 = ast.newMethodInvocation();
			mi2.setName(ast.newSimpleName(Constants.ASSIGN));
			Expression mit2 = newtag(s);
			mi2.arguments().add(mit2);
			MethodInvocation misv = ast.newMethodInvocation();
			misv.setName(ast.newSimpleName(Constants.SIMBOLIC));
			mi2.arguments().add(misv);
			startblock.arguments().add(mi2);

		}
	}

	@SuppressWarnings("unchecked")
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
		} else if (obj.getIsInternal() != null) {

			return generateIsInternal(obj.getIsInternal());
		} else if (obj.getMatchEntry() != null) {

			return generateMatchEntry(obj.getMatchEntry());
		} else if (obj.getParam() != null) {

			if (constants.contains(obj.getParam())) {
				MethodInvocation mi = ast.newMethodInvocation();
				if (obj.getParam().compareTo("null") == 0) // for inner_src == null
				{

					mi.setName(ast.newSimpleName("get"));

					FieldAccess fa = ast.newFieldAccess();
					fa.setName(ast.newSimpleName("am"));
					fa.setExpression(ast.newName("nctx"));

					mi.setExpression(fa);
					StringLiteral sl = ast.newStringLiteral();
					sl.setLiteralValue("null");
					mi.arguments().add(sl);
				} else {

					mi.setName(ast.newSimpleName("mkInt"));
					mi.setExpression(ast.newName("ctx"));

					FieldAccess fa = ast.newFieldAccess();
					fa.setName(ast.newSimpleName(obj.getParam()));
					fa.setExpression(ast.newName("nctx"));
					mi.arguments().add(fa);
				}
				return mi;
			}

			return ast.newSimpleName(obj.getParam());
			// }
		} else
			return null;
	}

	@SuppressWarnings("unchecked")
	private Expression generateFieldOf(LFFieldOf fieldOf) {

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("field"));
		String value = fieldOf.getField();
		mi.arguments().add(ast.newSimpleName(value));
		return mi;

	}

	@SuppressWarnings("unchecked")
	private Expression generateIsInternal(LFIsInternal isInternal) {	
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("isInternal"));
		mi.arguments().add(ast.newSimpleName(isInternal.getFieldOf().getField()));	
		return mi;

	}

	@SuppressWarnings("unchecked")
	private Expression generateMatchEntry(LFMatchEntry matchEntry) {


		MethodInvocation miib = ast.newMethodInvocation();
		miib.setName(ast.newSimpleName(Constants.BLOCK));
		{
			MethodInvocation mi = ast.newMethodInvocation();
			mi.setName(ast.newSimpleName("addrule"));
			mi.arguments().add(ast.newSimpleName("params"));
			miib.arguments().add(mi);
		}
		startblock.arguments().add(miib);
		params = new ArrayList<String>();
		for (LFFieldOf temp : matchEntry.getValue()) {
			params.add(temp.getField());
		}
		match = true;

		return null;
	}

	@SuppressWarnings("unchecked")
	private Expression generateAnd(LOAnd and) {

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("BitwiseAnd"));

		for (ExpressionObject temp : and.getExpression()) {
			Expression exp = getType(temp);
			if (exp == null) {
				continue;
			}
			if (temp.getOr() != null) {
				continue;
			}
			mi.arguments().add(exp);
		}
		if (!mi.arguments().isEmpty())
			startblock.arguments().add(mi);
		return null;
	}

	@SuppressWarnings("unchecked")
	private Expression generateOr(LOOr or) {

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("BitwiseOr"));

		for (ExpressionObject temp : or.getExpression()) {
			Expression exp = getType(temp);
			if (exp == null) {
				continue;
			}
			mi.arguments().add(exp);
		}
		startblock.arguments().add(mi);
		return mi;
	}

	private Expression generateNot(LONot not) {
		getType(not.getExpression());
		return null;
	}

	@SuppressWarnings("unchecked")
	private Expression generateEqual(LOEquals equal) {
		// Constrain(Tag("IPSrc"), :==:(ConstantValue(ipToNumber(p(i)(0))))),
		String rfield;
		String lfield;
				
		
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
		
		if(equal.getLeftExpression().getFieldOf().getUnit().equals("p_0")) {
			
			if (lfield != null && rfield != null && !lfield.equals(rfield)) {
				if (packetfield.contains(lfield)) {
					mi.setName(ast.newSimpleName(Constants.ASSIGN));

					Expression mit = newtag(lfield);
					Expression micv = newconstatvalue(rfield);

					mi.arguments().add(mit);
					mi.arguments().add(micv);
				} else if (lfield.equals("IF_OUT")) {
					interfacesend = rfield;
					return null;
				}
			}else
				return null;
		}else if (lfield != null && rfield != null && !lfield.equals(rfield)) {
			if (packetfield.contains(lfield)) {

				mi.setName(ast.newSimpleName(Constants.RULE));
				
				Expression mit = newtag(lfield);
				Expression micv = newconstatvalue(rfield);

				mi.arguments().add(makeInfixExpression(mit, micv, Operator.EQUALS));
			} else if(lfield.equals("IF_OUT")){
				interfacesend  = rfield;
				return null;
		}}else
			return null;
		return mi;
	}

	@SuppressWarnings("unchecked")
	private Expression generateImplies(LOImplies implies) {

		getType(implies.getAntecedentExpression());
		getType(implies.getConsequentExpression());

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName(Constants.FORWARD));
		mi.arguments().add(makeStringLiteral(interfacesend));

		return mi;
	}

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

	private Expression makeInfixExpression(Expression lo, Expression ro, Operator o) {
		// InfixExpression.Operator: *,/,%,+,-,<<,>>,<,>,<=,>=,==,!=,^,&,|,&&,||
		InfixExpression ie = ast.newInfixExpression();

		ie.setLeftOperand(lo);
		ie.setRightOperand(ro);
		ie.setOperator(o);

		return ie;

	}

	private Expression makeAssignment(Expression lo, Expression ro, Assignment.Operator o) {
		Assignment a = ast.newAssignment();
		a.setLeftHandSide(lo);
		a.setRightHandSide(ro);
		a.setOperator(o);
		return a;
	}

	private StringLiteral makeStringLiteral(String str) {
		StringLiteral sl = ast.newStringLiteral();
		sl.setLiteralValue(str);
		return sl;
	}

	// ---------------------------------------------------------
	// Method to generate a new method for SEFL instructions:
	// ---------------------------------------------------------

	@SuppressWarnings("unchecked")
	private Expression newfail() {
		// @Return -> Fail(Dropped)
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName(Constants.FAIL));
		mi.arguments().add(makeStringLiteral("Dropped"));
		return mi;
	}

	@SuppressWarnings("unchecked")
	private Expression newtag(String name) {
		// @Return -> Tag("name")
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName(Constants.TAG));
		mi.arguments().add(makeStringLiteral(name));
		return mi;
	}

	@SuppressWarnings("unchecked")
	private Expression newconstatvalue(String val) {
		// ConstantValue(ipToNumber(p(i)(1)))
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("ConstantValue"));
		if (val == "Ip" || val == "Ip") {
			MethodInvocation imi = ast.newMethodInvocation();
			imi.setName(ast.newSimpleName("ipToNumber"));
			imi.arguments().add(val);
			mi.arguments().add(imi);
		} else {
			mi.arguments().add(ast.newSimpleName(val));
		}
		return mi;
	}

	private NumberLiteral newHoffset(String s) {
		switch (s) {
		case ("IP_SRC"):
			return ast.newNumberLiteral("96");
		case ("IP_DST"):
			return ast.newNumberLiteral("128");
		case ("PROTO"):
			return ast.newNumberLiteral("64");
		case ("PORT_SRC"):
			return ast.newNumberLiteral("160");
		case ("PORT_DST"):
			return ast.newNumberLiteral("192");
		case ("ORIGIN"):
			return ast.newNumberLiteral("224");
		case ("ORIG_BODY"):
			return ast.newNumberLiteral("256");
		case ("BODY"):
			return ast.newNumberLiteral("288");// = application data
		case ("SEQUENCE"):
			return ast.newNumberLiteral("320");
		case ("EMAIL_FROM"):
			return ast.newNumberLiteral("352");
		case ("URL"):
			return ast.newNumberLiteral("384");
		case ("OPTIONS"):
			return ast.newNumberLiteral("416");
		case ("INNER_SRC"):
			return ast.newNumberLiteral("448");
		case ("INNER_DEST"):
			return ast.newNumberLiteral("480");
		case ("ENCRYPTED"):
			return ast.newNumberLiteral("512");
		}
		return null;
	}

	/*
	 * Method newInstructionBlock It is a recursive method to generate an ib(if)
	 * rule for all match entry
	 * 
	 * @index-> iterate all element of tableTypes.
	 */
	@SuppressWarnings("unchecked")
	private Expression newib(int index) {
		if (index == params.size()) {
			return newfail();
		}
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName(Constants.BLOCK));

		MethodInvocation imi = ast.newMethodInvocation();
		imi.setName(ast.newSimpleName(Constants.IF));
		{
			MethodInvocation cmi = ast.newMethodInvocation();
			cmi.setName(ast.newSimpleName(Constants.RULE));

			Expression tmi = newtag(params.get(index));
			cmi.arguments().add(tmi);

			Expression cv = newconstatvalue(params.get(index), index);

			cmi.arguments().add(cv);
			imi.arguments().add(cmi);
		}
		index++;
		imi.arguments().add(newib(index));
		imi.arguments().add(ast.newName("NoOp"));
		mi.arguments().add(imi);
		return mi;
	}

	@SuppressWarnings("unchecked")
	private Expression newconstatvalue(String str, int index) {

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("ConstantValue"));

		MethodInvocation mie = ast.newMethodInvocation();
		mie.setName(ast.newSimpleName("p"));

		mie.arguments().add(makeInfixExpression(ast.newSimpleName("i"), ast.newNumberLiteral(String.valueOf(index)),
				InfixExpression.Operator.PLUS));

		if (str.equals("IP_SRC") || str.equals("IP_DST")) {

			MethodInvocation imi = ast.newMethodInvocation();
			imi.setName(ast.newSimpleName("ipToNumber"));
			imi.arguments().add(mie);
			mi.arguments().add(imi);
		} else {
			mi.arguments().add(mie);
		}

		return mi;

	}

	// ---------------------------------------------------------
	// Method Setter/Getter to member class element:
	// ---------------------------------------------------------

	public ExpressionResult getReuslt() {
		return reuslt;
	}
}
