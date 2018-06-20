package it.polito.rule.unmarshaller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.xml.sax.SAXException;

import com.microsoft.z3.BoolExpr;
//import com.microsoft.z3.AST;
import com.microsoft.z3.IntExpr;

import it.polito.nfdev.jaxb.ExpressionObject;
import it.polito.nfdev.jaxb.ExpressionResult;
import it.polito.nfdev.jaxb.LFFieldOf;
import it.polito.nfdev.jaxb.LFIsInternal;
import it.polito.nfdev.jaxb.LFMatchEntry;
import it.polito.nfdev.jaxb.LFRecv;
import it.polito.nfdev.jaxb.LFSend;
import it.polito.nfdev.jaxb.LOAnd;
import it.polito.nfdev.jaxb.LOEquals;
import it.polito.nfdev.jaxb.LOExist;
import it.polito.nfdev.jaxb.LOGreaterThan;
import it.polito.nfdev.jaxb.LOGreaterThanOrEqual;
import it.polito.nfdev.jaxb.LOImplies;
import it.polito.nfdev.jaxb.LOLessThan;
import it.polito.nfdev.jaxb.LOLessThanOrEqual;
import it.polito.nfdev.jaxb.LONot;
import it.polito.nfdev.jaxb.LOOr;
import it.polito.nfdev.jaxb.LogicalUnit;
import it.polito.parser.Constants;

class RuleUnmarshaller {

	private String n_className;
	private String className;
	private AST ast;

	private ExpressionResult reuslt;

	private List<String> additionalParam;
	private List<String> logicalUnits;
	private List<String> constants;

	private int counter = 0;

	public RuleUnmarshaller(String fileName, String className, AST ast) throws MarshalException {

		this.className = className;
		this.n_className = "n_" + className;
		this.ast = ast;
		this.additionalParam = new ArrayList<>();
		this.constants = new ArrayList<>();

		// This list need to be in sync with z3 tool
		constants.add("HTTP_REQUEST");
		constants.add("HTTP_RESPONSE");
		constants.add("POP3_REQUEST");
		constants.add("POP3_RESPONSE");
		constants.add("DNS_REQUEST");
		constants.add("DNS_RESPONSE");
		constants.add("DNS_PORT_53");
		constants.add("HTTP_PORT_80");
		constants.add("SIP_INVITE");
		constants.add("SIP_INVITE_OK");
		constants.add("SIP_REGISTE_OK");
		constants.add("SIP_REGISTE");
		constants.add("SIP_END");
		constants.add("null");
		constants.add("true");
		constants.add("false");
		// constants.add("ip_sipServer"); // if not add these leements, they will be
		// requested when install this model
		// constants.add("REQUESTED_URL");
		/*
		 * constants.add("new_port"); constants.add("value_0");
		 */

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

	public MethodDeclaration generateRule() {

		if (reuslt.getLogicalExpressionResult().size() < 1 || reuslt.getNodeOrPacket().size() < 1)
			return null;

		logicalUnits = new ArrayList<>();

		for (LogicalUnit temp : reuslt.getNodeOrPacket())
			logicalUnits.add(temp.getName());

		MethodDeclaration method = ast.newMethodDeclaration();
		method.setName(ast.newSimpleName("install" + className));
		method.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

		SingleVariableDeclaration param = ast.newSingleVariableDeclaration();
		//param.setName(ast.newSimpleName("internalNodes"));
		//param.setType(ast.newArrayType(ast.newSimpleType(ast.newName("NetworkObject"))));
		//method.parameters().add(param);

		method.setBody(ast.newBlock());

		for (LogicalUnit temp : reuslt.getNodeOrPacket()) {
			char firstChar = temp.getName().charAt(0);
			if (Character.compare(firstChar, 'n') == 0 || Character.compare(firstChar, 'p') == 0
					|| Character.compare(firstChar, 't') == 0) {
				if (Character.compare(firstChar, 'n') == 0 && !Character.isDigit(temp.getName().charAt(2)))
					continue;

				Assignment assignment = ast.newAssignment();

				VariableDeclarationFragment varFrag = ast.newVariableDeclarationFragment();
				varFrag.setName(ast.newSimpleName(temp.getName()));
				VariableDeclarationExpression varExp = ast.newVariableDeclarationExpression(varFrag);
				if (Character.compare(firstChar, 't') == 0)
					varExp.setType(ast.newSimpleType(ast.newName("IntExpr")));
				else
					varExp.setType(ast.newSimpleType(ast.newName("Expr")));

				assignment.setLeftHandSide(varExp);

				StringBuilder builder = new StringBuilder();

				MethodInvocation mi = ast.newMethodInvocation();
				if (Character.compare(firstChar, 't') == 0)
					mi.setName(ast.newSimpleName("mkIntConst"));
				else
					mi.setName(ast.newSimpleName("mkConst"));
				mi.setExpression(ast.newName("ctx"));

				InfixExpression expression = ast.newInfixExpression();
				expression.setOperator(Operator.PLUS);

				StringLiteral string = ast.newStringLiteral();
				string.setLiteralValue(n_className + "_");

				expression.setLeftOperand(string);

				expression.setRightOperand(ast.newSimpleName(n_className));

				string = ast.newStringLiteral();
				string.setLiteralValue("_" + temp.getName());

				expression.extendedOperands().add(string);

				mi.arguments().add(expression);
				if (Character.compare(firstChar, 't') != 0
						&& (Character.compare(firstChar, 'n') == 0 || Character.compare(firstChar, 'p') == 0)) {

					FieldAccess fa = ast.newFieldAccess();
					fa.setExpression(ast.newName("nctx"));

					if (Character.compare(firstChar, 'n') == 0)
						fa.setName(ast.newSimpleName("node"));
					else if (Character.compare(firstChar, 'p') == 0)
						fa.setName(ast.newSimpleName("packet"));

					mi.arguments().add(fa);
				}

				assignment.setRightHandSide(mi);

				method.getBody().statements().add(ast.newExpressionStatement(assignment));
			}

		}

		// IfStatement is = ast.newIfStatement();
		// InfixExpression ife = ast.newInfixExpression();
		// is.setExpression(ife);
		//
		// ife.setOperator(InfixExpression.Operator.NOT_EQUALS);
		// ife.setLeftOperand(ast.newName("src_ip"));
		// ife.setRightOperand(ast.newNullLiteral());
		//
		// MethodInvocation mi = ast.newMethodInvocation();
		// is.setThenStatement(ast.newExpressionStatement(mi));
		// mi.setName(ast.newSimpleName("add"));
		// mi.setExpression(ast.newName("constraints"));
		//
		// MethodInvocation innerMi = ast.newMethodInvocation();
		// innerMi.setName(ast.newSimpleName("mkForall"));
		// innerMi.setExpression(ast.newName("ctx"));
		//
		// mi.arguments().add(innerMi);
		//
		// ArrayCreation ac = ast.newArrayCreation();
		// ac.setType(ast.newArrayType(ast.newSimpleType(ast.newName("Expr"))));
		// ArrayInitializer ai = ast.newArrayInitializer();
		// ai.expressions().add(ast.newName("t_"+0));
		// ai.expressions().add(ast.newName("p_"+0));
		// ai.expressions().add(ast.newName("n_"+0));
		//
		//
		// ac.setInitializer(ai);
		//
		// innerMi.arguments().add(ac);
		//
		// MethodInvocation mkImplies = ast.newMethodInvocation();
		// innerMi.arguments().add(mkImplies);
		// mkImplies.setName(ast.newSimpleName("mkImplies"));
		// mkImplies.setExpression(ast.newName("ctx"));
		//
		// CastExpression cast = ast.newCastExpression();
		// mkImplies.arguments().add(cast);
		// cast.setType(ast.newSimpleType(ast.newName("BoolExpr")));
		//
		// MethodInvocation apply = ast.newMethodInvocation();
		// cast.setExpression(apply);
		// apply.setName(ast.newSimpleName("apply"));
		// apply.arguments().add(ast.newName(n_className));
		// apply.arguments().add(ast.newName("n_"+0));
		// apply.arguments().add(ast.newName("p_"+0));
		// apply.arguments().add(ast.newName("t_"+0));
		//
		// FieldAccess fa = ast.newFieldAccess();
		// apply.setExpression(fa);
		// fa.setName(ast.newSimpleName("send"));
		// fa.setExpression(ast.newName("nctx"));
		//
		// MethodInvocation mkEq = ast.newMethodInvocation();
		// mkImplies.arguments().add(mkEq);
		// mkEq.setName(ast.newSimpleName("mkEq"));
		// mkEq.setExpression(ast.newName("ctx"));
		//
		// apply = ast.newMethodInvocation();
		// mkEq.arguments().add(apply);
		// apply.setName(ast.newSimpleName("apply"));
		// apply.arguments().add(ast.newName("p_0"));
		//
		// MethodInvocation get = ast.newMethodInvocation();
		// apply.setExpression(get);
		// get.setName(ast.newSimpleName("get"));
		// StringLiteral sl = ast.newStringLiteral();
		// sl.setLiteralValue("src_ip");
		// get.arguments().add(sl);
		//
		// fa = ast.newFieldAccess();
		// get.setExpression(fa);
		// fa.setName(ast.newSimpleName("pf"));
		// fa.setExpression(ast.newName("nctx"));
		//
		// MethodInvocation mkInt = ast.newMethodInvocation();
		// mkEq.arguments().add(mkInt);
		// mkInt.setName(ast.newSimpleName("mkInt"));
		// mkInt.setExpression(ast.newName("ctx"));
		// mkInt.arguments().add(ast.newName("src_ip"));
		//
		//
		// innerMi.arguments().add(ast.newNumberLiteral("1"));
		// innerMi.arguments().add(ast.newNullLiteral());
		// innerMi.arguments().add(ast.newNullLiteral());
		// innerMi.arguments().add(ast.newNullLiteral());
		// innerMi.arguments().add(ast.newNullLiteral());
		//
		// method.getBody().statements().add(is);

		for (ExpressionObject temp : reuslt.getLogicalExpressionResult()) {

			counter = 0;

			MethodInvocation mi = ast.newMethodInvocation();
			mi.setName(ast.newSimpleName("add"));
			mi.setExpression(ast.newName("constraints"));

			MethodInvocation innerMi = ast.newMethodInvocation();
			innerMi.setName(ast.newSimpleName("mkForall"));
			innerMi.setExpression(ast.newName("ctx"));

			mi.arguments().add(innerMi);

			ArrayCreation ac = ast.newArrayCreation();
			ac.setType(ast.newArrayType(ast.newSimpleType(ast.newName("Expr"))));
			ArrayInitializer ai = ast.newArrayInitializer();
			// ai.expressions().add(ast.newName("t_"+counter));
			ai.expressions().add(ast.newName("p_" + counter));
			ai.expressions().add(ast.newName("n_" + counter));
			counter++;

			ac.setInitializer(ai);

			innerMi.arguments().add(ac);
			innerMi.arguments().add(getType(temp)); 
			innerMi.arguments().add(ast.newNumberLiteral("1"));
			innerMi.arguments().add(ast.newNullLiteral());
			innerMi.arguments().add(ast.newNullLiteral());
			innerMi.arguments().add(ast.newNullLiteral());
			innerMi.arguments().add(ast.newNullLiteral());

			method.getBody().statements().add(ast.newExpressionStatement(mi));
		}

		// System.out.println(additionalParam);
		for (String temp : additionalParam) {

			param = ast.newSingleVariableDeclaration();
			param.setName(ast.newSimpleName(temp));
			param.setType(ast.newSimpleType(ast.newName("Expr")));
			method.parameters().add(param);
		}

		return method;
	}

	// @SuppressWarnings("unchecked")
	private Expression getType(ExpressionObject obj) {
		if (obj.getAnd() != null) {

			return generateAnd(obj.getAnd());
		} else if (obj.getOr() != null) {

			return generateOr(obj.getOr());
		} else if (obj.getNot() != null) {

			return generateNot(obj.getNot());
		} else if (obj.getEqual() != null) {

			return generateEqual(obj.getEqual());
		} else if (obj.getGreaterThan() != null) {

			return generateGreaterThan(obj.getGreaterThan());
		} else if (obj.getGreaterThanOrEqual() != null) {

			return generateGreaterThanOrEqual(obj.getGreaterThanOrEqual());
		} else if (obj.getLessThan() != null) {

			return generateLessThan(obj.getLessThan());
		} else if (obj.getLessThanOrEqual() != null) {

			return generateLessThanOrEqual(obj.getLessThanOrEqual());
		} else if (obj.getImplies() != null) {

			return generateImplies(obj.getImplies());
		} else if (obj.getExist() != null) {

			return generateExist(obj.getExist());
		} else if (obj.getSend() != null) {

			return generateSend(obj.getSend());
		} else if (obj.getRecv() != null) {

			return generateRecv(obj.getRecv());
		} else if (obj.getFieldOf() != null) {
			
			return generateFieldOf(obj.getFieldOf());
		} else if (obj.getIsInternal() != null) {

			return generateIsInternal(obj.getIsInternal());
		} else if (obj.getMatchEntry() != null) {

			return generateMatchEntry(obj.getMatchEntry());
		} else if (obj.getParam() != null) {

			// Check if the string is a well-known constant in the netcontext class

			/*
			 * (BoolExpr)nctx.pf.get("encrypted").apply(p_0)
			 * ctx.mkNot((BoolExpr)nctx.pf.get("encrypted").apply(p_1)),
			 * ctx.mkEq(nctx.pf.get("inner_src").apply(p_1), nctx.am.get("null")),
			 * 
			 */

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
			/*
			 * try{ // used in MailServer in case the response is always 1, corresponding to
			 * the content of Antispam ( similar in webClient,EndHost)
			 * Integer.parseInt(obj.getParam());
			 * 
			 * MethodInvocation mi = ast.newMethodInvocation();
			 * mi.setName(ast.newSimpleName("mkInt")); mi.setExpression(ast.newName("ctx"));
			 * 
			 * FieldAccess fa = ast.newFieldAccess();
			 * System.out.println("----------param= "+obj.getParam());
			 * fa.setName(ast.newSimpleName(obj.getParam()));
			 * //fa.setExpression(ast.newName("nctx")); // NumberLiteral num = new
			 * NumberLiteral(); // num.setToken(obj.getParam());
			 * 
			 * mi.arguments().add(fa);
			 * 
			 * return mi;
			 * 
			 * }catch(NumberFormatException ex){
			 */

			if (!logicalUnits.contains(obj.getParam()) && !additionalParam.contains(obj.getParam()))
				additionalParam.add(obj.getParam());

			return ast.newSimpleName(obj.getParam());
			// }
		} else
			return null;
	}

	private Expression generateAnd(LOAnd and) {

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("mkAnd"));
		mi.setExpression(ast.newName("ctx"));

		for (ExpressionObject temp : and.getExpression()) {
			Expression exp = getType(temp);
			//MODIFY
			if (exp == null) {
				continue;
			}
			mi.arguments().add(exp);
		}

		return mi;
	}

	private Expression generateOr(LOOr or) {

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("mkOr"));
		mi.setExpression(ast.newName("ctx"));

		for (ExpressionObject temp : or.getExpression()) {
			Expression exp = getType(temp);
			// if(exp!=null)
			mi.arguments().add(exp);
		}

		return mi;

	}

	private Expression generateNot(LONot not) {

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("mkNot"));
		mi.setExpression(ast.newName("ctx"));

		Expression exp = getType(not.getExpression());
		// if(exp!=null)
		mi.arguments().add(exp);

		return mi;

	}

	private Expression generateEqual(LOEquals equal) {

		MethodInvocation mi = ast.newMethodInvocation();
		// when consider encrypted packet field, must construct a different z3 function.
		if (equal.getRightExpression().getParam() != null
				&& (equal.getRightExpression().getParam().compareTo("true") == 0
						|| equal.getRightExpression().getParam().compareTo("false") == 0)) {
			String value = equal.getRightExpression().getParam();
			if (value.compareTo("true") == 0) { // (BoolExpr)nctx.pf.get("encrypted").apply(p_1)
				System.out.println("ruleUnmasharler line 540 equal.rightObj = true");
				if (equal.getLeftExpression().getFieldOf() != null) {
					CastExpression ce = ast.newCastExpression();
					ce.setType(ast.newSimpleType(ast.newSimpleName("BoolExpr")));

					LFFieldOf fieldOf = equal.getLeftExpression().getFieldOf();
					mi.setName(ast.newSimpleName("apply"));
					mi.arguments().add(ast.newName(fieldOf.getUnit()));
					MethodInvocation innerMi = ast.newMethodInvocation();
					innerMi.setName(ast.newSimpleName("get"));

					FieldAccess fa = ast.newFieldAccess();
					fa.setName(ast.newSimpleName("pf"));
					fa.setExpression(ast.newName("nctx"));

					innerMi.setExpression(fa);
					StringLiteral sl = ast.newStringLiteral();
					sl.setLiteralValue(mapToZ3PacketField(fieldOf.getField()));
					innerMi.arguments().add(sl);

					mi.setExpression(innerMi);
					ce.setExpression(mi);
					return ce;
				}
			}
			if (value.compareTo("false") == 0) { // ctx.mkNot((BoolExpr)nctx.pf.get("encrypted").apply(p_1)),

				MethodInvocation miNot = ast.newMethodInvocation();
				miNot.setName(ast.newSimpleName("mkNot"));
				miNot.setExpression(ast.newName("ctx"));

				System.out.println("ruleUnmasharler line 596 equal.rightObj = false-------");
				if (equal.getLeftExpression().getFieldOf() != null) {
					CastExpression ce = ast.newCastExpression();
					ce.setType(ast.newSimpleType(ast.newSimpleName("BoolExpr")));

					LFFieldOf fieldOf = equal.getLeftExpression().getFieldOf();
					mi.setName(ast.newSimpleName("apply"));
					mi.arguments().add(ast.newName(fieldOf.getUnit()));
					MethodInvocation innerMi = ast.newMethodInvocation();
					innerMi.setName(ast.newSimpleName("get"));

					FieldAccess fa = ast.newFieldAccess();
					fa.setName(ast.newSimpleName("pf"));
					fa.setExpression(ast.newName("nctx"));

					innerMi.setExpression(fa);
					StringLiteral sl = ast.newStringLiteral();
					sl.setLiteralValue(mapToZ3PacketField(fieldOf.getField()));
					innerMi.arguments().add(sl);

					mi.setExpression(innerMi);
					ce.setExpression(mi);
					miNot.arguments().add(ce);
					return miNot;
				}
			}

		} else {
			mi.setName(ast.newSimpleName("mkEq"));
			mi.setExpression(ast.newName("ctx"));
			Expression leftExp = getType(equal.getLeftExpression());
			// MODIFY
			if (leftExp == null) {
				return null;
			}
			mi.arguments().add(leftExp);
			Expression rightExp = getType(equal.getRightExpression());
			mi.arguments().add(rightExp);
		}
		return mi;

	}

	private Expression generateGreaterThan(LOGreaterThan greaterThan) {

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("mkGt"));
		mi.setExpression(ast.newName("ctx"));

		Expression leftExp = getType(greaterThan.getLeftExpression());
		mi.arguments().add(leftExp);
		Expression rightExp = getType(greaterThan.getRightExpression());
		mi.arguments().add(rightExp);

		return mi;

	}

	private Expression generateGreaterThanOrEqual(LOGreaterThanOrEqual greaterThanOrEqual) {

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("mkGe"));
		mi.setExpression(ast.newName("ctx"));

		Expression leftExp = getType(greaterThanOrEqual.getLeftExpression());
		mi.arguments().add(leftExp);
		Expression rightExp = getType(greaterThanOrEqual.getRightExpression());
		mi.arguments().add(rightExp);

		return mi;

	}

	private Expression generateLessThan(LOLessThan lessThan) {

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("mkLt"));
		mi.setExpression(ast.newName("ctx"));

		Expression leftExp = getType(lessThan.getLeftExpression());
		mi.arguments().add(leftExp);
		Expression rightExp = getType(lessThan.getRightExpression());
		mi.arguments().add(rightExp);

		return mi;

	}

	private Expression generateLessThanOrEqual(LOLessThanOrEqual lessThanOrEqual) {

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("mkLe"));
		mi.setExpression(ast.newName("ctx"));

		Expression leftExp = getType(lessThanOrEqual.getLeftExpression());
		mi.arguments().add(leftExp);
		Expression rightExp = getType(lessThanOrEqual.getRightExpression());
		mi.arguments().add(rightExp);

		return mi;

	}

	private Expression generateImplies(LOImplies implies) {

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("mkImplies"));
		mi.setExpression(ast.newName("ctx"));

		Expression antExp = getType(implies.getAntecedentExpression());
		mi.arguments().add(antExp);
		Expression consExp = getType(implies.getConsequentExpression());
		mi.arguments().add(consExp);

		return mi;
	}

	private Expression generateExist(LOExist exist) {

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("mkExists"));
		mi.setExpression(ast.newName("ctx"));

		ArrayCreation ac = ast.newArrayCreation();
		ac.setType(ast.newArrayType(ast.newSimpleType(ast.newName("Expr"))));
		ArrayInitializer ai = ast.newArrayInitializer();
		if (exist.getUnit().size() > 1) {
			ai.expressions().add(ast.newName("p_" + counter));
			ai.expressions().add(ast.newName("n_" + counter));
		} else
			ai.expressions().add(ast.newName("p_" + counter));
		counter++;

		ac.setInitializer(ai);
		mi.arguments().add(ac);

		Expression exp = getType(exist.getExpression());
		if (exp == null) {
			return null;
		}
		mi.arguments().add(exp);
		mi.arguments().add(ast.newNumberLiteral("1"));
		mi.arguments().add(ast.newNullLiteral());
		mi.arguments().add(ast.newNullLiteral());
		mi.arguments().add(ast.newNullLiteral());
		mi.arguments().add(ast.newNullLiteral());

		return mi;

	}

	private Expression generateSend(LFSend send) {

		CastExpression ce = ast.newCastExpression();
		ce.setType(ast.newSimpleType(ast.newSimpleName("BoolExpr")));

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("apply"));

		FieldAccess fa = ast.newFieldAccess();
		fa.setName(ast.newSimpleName("send"));
		fa.setExpression(ast.newName("nctx"));

		mi.setExpression(fa);

		mi.arguments().add(ast.newName(send.getSource()));
		mi.arguments().add(ast.newName(send.getDestination()));
		mi.arguments().add(ast.newName(send.getPacketOut()));
		// mi.arguments().add(ast.newName(send.getTimeOut()));

		ce.setExpression(mi);
		return ce;
	}

	private Expression generateRecv(LFRecv recv) {

		CastExpression ce = ast.newCastExpression();
		ce.setType(ast.newSimpleType(ast.newSimpleName("BoolExpr")));

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("apply"));

		FieldAccess fa = ast.newFieldAccess();
		fa.setName(ast.newSimpleName("recv"));
		fa.setExpression(ast.newName("nctx"));

		mi.setExpression(fa);

		mi.arguments().add(ast.newName(recv.getSource()));
		mi.arguments().add(ast.newName(recv.getDestination()));
		mi.arguments().add(ast.newName(recv.getPacketIn()));
		// mi.arguments().add(ast.newName(recv.getTimeIn()));

		ce.setExpression(mi);
		return ce;

	}

	private Expression generateFieldOf(LFFieldOf fieldOf) {
		// (IntExpr)src_port.apply(p_0)

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("apply"));
		mi.arguments().add(ast.newName(fieldOf.getUnit()));
		if ((fieldOf.getField().compareTo(Constants.PORT_SOURCE) != 0)
				&& (fieldOf.getField().compareTo(Constants.PORT_DESTINATION) != 0)) {

			// MODIFY: For delete if_type
			if ((fieldOf.getField().compareTo(Constants.IF_OUT) == 0)) {
				return null;
			}
			MethodInvocation innerMi = ast.newMethodInvocation();
			innerMi.setName(ast.newSimpleName("get"));

			FieldAccess fa = ast.newFieldAccess();
			fa.setName(ast.newSimpleName("pf"));
			fa.setExpression(ast.newName("nctx"));

			innerMi.setExpression(fa);
			StringLiteral sl = ast.newStringLiteral();
			sl.setLiteralValue(mapToZ3PacketField(fieldOf.getField()));
			innerMi.arguments().add(sl);

			mi.setExpression(innerMi);
		} else if (fieldOf.getField().compareTo(Constants.PORT_SOURCE) == 0) {
			FieldAccess fa = ast.newFieldAccess();
			fa.setName(ast.newSimpleName("src_port"));
			fa.setExpression(ast.newName("nctx"));

			mi.setExpression(fa);

		} else {
			FieldAccess fa = ast.newFieldAccess();
			fa.setName(ast.newSimpleName("dest_port"));
			fa.setExpression(ast.newName("nctx"));

			mi.setExpression(fa);
		}

		return mi;

	}

	private Expression generateIsInternal(LFIsInternal isInternal) {

		CastExpression ce = ast.newCastExpression();
		ce.setType(ast.newSimpleType(ast.newSimpleName("BoolExpr")));

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("apply"));
		mi.setExpression(ast.newName("isInternal"));

		MethodInvocation argumentMi = ast.newMethodInvocation();
		argumentMi.setName(ast.newSimpleName("apply"));
		argumentMi.arguments().add(ast.newName(isInternal.getFieldOf().getUnit()));

		MethodInvocation innerMi = ast.newMethodInvocation();
		innerMi.setName(ast.newSimpleName("get"));

		FieldAccess fa = ast.newFieldAccess();
		fa.setName(ast.newSimpleName("pf"));
		fa.setExpression(ast.newName("nctx"));

		innerMi.setExpression(fa);
		StringLiteral sl = ast.newStringLiteral();
		sl.setLiteralValue(mapToZ3PacketField(isInternal.getFieldOf().getField()));
		innerMi.arguments().add(sl);

		argumentMi.setExpression(innerMi);
		mi.arguments().add(argumentMi);
		ce.setExpression(mi);
		return ce;

	}

	private Expression generateMatchEntry(LFMatchEntry matchEntry) {

		CastExpression ce = ast.newCastExpression();
		ce.setType(ast.newSimpleType(ast.newSimpleName("BoolExpr")));

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("apply"));
		mi.setExpression(ast.newName("matchEntry"));

		ce.setExpression(mi);

		for (LFFieldOf temp : matchEntry.getValue()) {
			mi.arguments().add(generateFieldOf((LFFieldOf) temp));
			// System.out.println("----------------??????????????"+temp.getField());
			// System.out.println("------Z3----------??????????????"+mapToZ3PacketField(temp.getField()));
		}

		return ce;

	}

	private String mapToZ3PacketField(String field) {

		switch (field) {

		case Constants.IP_SOURCE:
			return Constants.Z3_IP_SOURCE;

		case Constants.IP_DESTINATION:
			return Constants.Z3_IP_DESTINATION;

		case Constants.PORT_SOURCE:
			return Constants.Z3_PORT_SOURCE;

		case Constants.PORT_DESTINATION:
			return Constants.Z3_PORT_DESTINATION;

		case Constants.PROTO:
			return Constants.Z3_PROTO;

		case Constants.ORIGIN:
			return Constants.Z3_ORIGIN;

		case Constants.ORIG_BODY:
			return Constants.Z3_ORIG_BODY;

		case Constants.BODY:
			return Constants.Z3_BODY;

		case Constants.SEQUENCE:
			return Constants.Z3_SEQUENCE;

		case Constants.EMAIL_FROM:
			return Constants.Z3_EMAIL_FROM;

		case Constants.URL:
			return Constants.Z3_URL;

		case Constants.OPTIONS:
			return Constants.Z3_OPTIONS;

		case Constants.INNER_SRC:
			return Constants.Z3_INNER_SRC;

		case Constants.INNER_DEST:
			return Constants.Z3_INNER_DEST;

		case Constants.ENCRYPTED:
			return Constants.Z3_ENCRYPTED;
		}

		return "null_field";
	}

	public ExpressionResult getReuslt() {
		return reuslt;
	}
}
