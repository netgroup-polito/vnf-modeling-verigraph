package it.polito.rule.generator;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import it.polito.nfdev.jaxb.*;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.parser.Constants;
import it.polito.parser.Variable;
import it.polito.parser.context.ReturnSnapshot;
import it.polito.parser.context.TableEntryContext;

public class RuleContext {

	private ObjectFactory factory;
	private ExpressionObject result;
	private List<LogicalUnit> units;
	private List<TableEntryContext> entryList;
	private ReturnSnapshot returnSnapshot;
	private LogicalOperator entryPoint_p1;
	private LogicalOperator entryPoint_p2;
	private LogicalOperator entryPoint_p3;
	private LogicalOperator entryPoint_p4;
	private Map<String, List<Variable>> localVariable;
	private Map<String, List<Variable>> globalVariable;

	public boolean isDataDriven;
	public boolean ifSend;
	// public boolean isIndirectNF;/

	private String netFunction;

	private int packetCounter = -1;
	private int nodeCounter = -1;
	private int valueCounter = -1;

	public RuleContext(ObjectFactory factory, ReturnSnapshot returnSnapshot) {
		this.factory = factory;
		this.returnSnapshot = returnSnapshot;
		this.netFunction = "n_" + this.returnSnapshot.getMethodContext().getContext().getClassName();
		this.units = new ArrayList<>();
		this.entryList = returnSnapshot.getMethodContext().getEntryValues();
		Collections.sort(entryList);

		result = factory.createExpressionObject();

		this.localVariable = returnSnapshot.getMethodContext().getMethodVariablesMap();
		this.globalVariable = returnSnapshot.getMethodContext().getContext().getVariablesMap();
		this.isDataDriven = returnSnapshot.getMethodContext().getContext().isDataDriven();
		// this.isIndirectNF =
		// returnSnapshot.getMethodContext().getContext().isIndirectNF();
		this.ifSend = false;
		setDefaultRule(returnSnapshot.getMethodContext().getMethodName());

	}

	// ------ Generate_Rules:

	public void setDefaultRule(String methodName) {
		switch (methodName) {
		case Constants.MAIN_NF_METHOD:

			LUNode dest = factory.createLUNode();
			LUNode source = factory.createLUNode();
			LUPacket packet = factory.createLUPacket();

			dest.setName("n_" + ++nodeCounter);
			source.setName(netFunction);
			packet.setName("p_" + ++packetCounter);

			units.add(dest);
			units.add(source);
			units.add(packet);

			LFSend send = factory.createLFSend();
			send.setDestination(dest.getName());
			send.setSource(source.getName());
			send.setPacketOut(packet.getName());

			LOImplies implies = factory.createLOImplies();
			implies.setAntecedentExpression(factory.createExpressionObject());
			implies.setConsequentExpression(factory.createExpressionObject());

			if (returnSnapshot.getInterfaceName().compareTo(Constants.INTERNAL_INTERFACE) == 0) {
				LOAnd and = factory.createLOAnd();
				LFIsInternal isInternal = isInternalRule(packet.getName(), Constants.IP_DESTINATION);
				ExpressionObject temp = factory.createExpressionObject();

				temp.setSend(send);
				and.getExpression().add(temp);
				temp = factory.createExpressionObject();
				temp.setIsInternal(isInternal);
				and.getExpression().add(temp);
				implies.getAntecedentExpression().setAnd(and);

			} else if (returnSnapshot.getInterfaceName().compareTo(Constants.EXTERNAL_INTERFACE) == 0) {
				LOAnd and = factory.createLOAnd();
				LONot not = factory.createLONot();
				LFIsInternal isInternal = isInternalRule(packet.getName(), Constants.IP_DESTINATION);
				ExpressionObject temp = factory.createExpressionObject();

				temp.setSend(send);
				and.getExpression().add(temp);
				temp = factory.createExpressionObject();
				temp.setIsInternal(isInternal);
				not.setExpression(temp);
				temp = factory.createExpressionObject();
				temp.setNot(not);
				and.getExpression().add(temp);
				implies.getAntecedentExpression().setAnd(and);
			} else {
				implies.getAntecedentExpression().setSend(send);
			}

			LUNode n1 = factory.createLUNode();
			LUPacket p1 = factory.createLUPacket();

			n1.setName("n_" + ++nodeCounter); // --> p1
			p1.setName("p_" + ++packetCounter);
			;
			units.add(p1);
			units.add(n1);

			LOExist exist = factory.createLOExist();
			exist.getUnit().add(n1.getName());
			exist.getUnit().add(p1.getName());
			exist.setExpression(factory.createExpressionObject());

			LFRecv recv = factory.createLFRecv();
			recv.setSource(n1.getName());
			recv.setDestination(netFunction);
			recv.setPacketIn(p1.getName());

			LOAnd and = factory.createLOAnd();
			ExpressionObject temp = factory.createExpressionObject();
			temp.setRecv(recv);
			and.getExpression().add(temp);
			temp = factory.createExpressionObject();

			exist.getExpression().setAnd(and);
			implies.getConsequentExpression().setExist(exist);

			result.setImplies(implies);

			entryPoint_p1 = and;
			break;
		case Constants.DEFINE_SENDING_PACKET_METHOD:
			LUNode dest1 = factory.createLUNode();
			LUNode source1 = factory.createLUNode();
			LUPacket packet1 = factory.createLUPacket();

			dest1.setName("n_" + ++nodeCounter);
			source1.setName(netFunction);
			packet1.setName("p_" + ++packetCounter);

			units.add(dest1);
			units.add(source1);
			units.add(packet1);

			LFSend send1 = factory.createLFSend();
			send1.setDestination(dest1.getName());
			send1.setSource(source1.getName());
			send1.setPacketOut(packet1.getName());

			LOImplies implies1 = factory.createLOImplies();
			implies1.setAntecedentExpression(factory.createExpressionObject());
			implies1.setConsequentExpression(factory.createExpressionObject());

			LOAnd and1 = factory.createLOAnd();
			ExpressionObject temp1 = factory.createExpressionObject();
			temp1.setSend(send1);
			and1.getExpression().add(temp1);

			if (returnSnapshot.getInterfaceName().compareTo(Constants.INTERNAL_INTERFACE) == 0) {

				LFIsInternal isInternal = isInternalRule(packet1.getName(), Constants.IP_DESTINATION);

				temp1 = factory.createExpressionObject();
				temp1.setIsInternal(isInternal);
				and1.getExpression().add(temp1);
				implies1.getAntecedentExpression().setAnd(and1);

			} else if (returnSnapshot.getInterfaceName().compareTo(Constants.EXTERNAL_INTERFACE) == 0) {

				LONot not = factory.createLONot();
				LFIsInternal isInternal = isInternalRule(packet1.getName(), Constants.IP_DESTINATION);

				temp1 = factory.createExpressionObject();
				temp1.setIsInternal(isInternal);
				not.setExpression(temp1);
				temp1 = factory.createExpressionObject();
				temp1.setNot(not);
				and1.getExpression().add(temp1);
				implies1.getAntecedentExpression().setAnd(and1);

			} else {
				implies1.getAntecedentExpression().setSend(send1);
			}

			and1 = factory.createLOAnd();
			implies1.getConsequentExpression().setAnd(and1);

			result.setImplies(implies1);
			break;

		default:
			System.err.println("Unknown method: " + methodName);
		}
	}

	public void setExitPacketConditions(List<String> removeField) {

		List<String> field_names = new ArrayList<String>();
		field_names.add(Constants.IP_SOURCE);
		field_names.add(Constants.IP_DESTINATION);
		field_names.add(Constants.PROTO);
		field_names.add(Constants.ORIGIN);
		field_names.add(Constants.ORIG_BODY);
		field_names.add(Constants.BODY);
		field_names.add(Constants.SEQUENCE);
		field_names.add(Constants.EMAIL_FROM);
		field_names.add(Constants.URL);
		field_names.add(Constants.OPTIONS);
		field_names.add(Constants.INNER_SRC);
		field_names.add(Constants.INNER_DEST);
		field_names.add(Constants.ENCRYPTED);

		if (removeField != null) {
			if (!removeField.isEmpty()) {
				field_names.removeAll(removeField);
			}
		}

		for (String field : field_names) {
			ExpressionObject temp = factory.createExpressionObject();

			LOEquals equals_temp = factory.createLOEquals();
			equals_temp.setLeftExpression(factory.createExpressionObject());
			equals_temp.setRightExpression(factory.createExpressionObject());
			equals_temp.getLeftExpression().setFieldOf(fieldOf("p_0", field));
			equals_temp.getRightExpression().setFieldOf(fieldOf("p_1", field));
			temp.setEqual(equals_temp);
			setLastExpression(temp);
		}

	}

	public boolean generateRuleForExitingPacket(String packetField, String value) { // -->packet_in.setField(PacketField.IP_SRC,
																					// natIp);

		if (checkPacketField(packetField) && containsLogicalUnit("p_0")) {
			LOEquals equal = factory.createLOEquals();
			equal.setLeftExpression(factory.createExpressionObject());
			equal.setRightExpression(factory.createExpressionObject());

			LFFieldOf fieldOf = factory.createLFFieldOf();
			fieldOf.setUnit("p_0");
			fieldOf.setField(packetField);

			equal.getLeftExpression().setFieldOf(fieldOf);

			if (value.compareTo("NOTNULL") == 0 || value.compareTo("NULL") == 0) {
				System.out.println("ruleContext line543 >>>>>>>>>>>>>>>>>>>found value = NOTNULL OR NULL");
				ExpressionObject temp = factory.createExpressionObject();
				if (value.compareTo("NOTNULL") == 0) {
					LONot not = factory.createLONot();
					equal.getRightExpression().setParam("null");
					temp.setEqual(equal);
					not.setExpression(temp);
					temp = factory.createExpressionObject();
					temp.setNot(not);
				} else {
					equal.getRightExpression().setParam("null");
					temp.setEqual(equal);
				}
				// TODO 1-NOTNULL/NULL INNER_SRC
				if (packetField.compareTo(Constants.INNER_SRC) == 0) { // must point out only when p0.inner_src==null,
																		// then can put the equal object in
																		// Implies.AntecedentExpression
					if (result.getImplies().getAntecedentExpression().getAnd() != null) {

						System.out
								.println("------------>RC line560 implies.and !=null    inner_src ==null or !=null  ");
						result.getImplies().getAntecedentExpression().getAnd().getExpression().add(temp);
					}

					else {
						System.out.println(
								"------------>RC line565 implies.and ==null  reconstruct 'And'  inner_src ==null or !=null  ");
						LFSend send = result.getImplies().getAntecedentExpression().getSend();
						LOAnd and = factory.createLOAnd();
						ExpressionObject temp1 = factory.createExpressionObject();
						temp1.setSend(send);
						and.getExpression().add(temp1);
						and.getExpression().add(temp);
						result.getImplies().getAntecedentExpression().setAnd(and);
						result.getImplies().getAntecedentExpression().setSend(null);
					}
				}
				if (packetField.compareTo(Constants.INNER_DEST) == 0) {
					setLastExpression(temp);
				}

			} else if (value.compareTo("isInternalAdderss") == 0) { // it means the value is only for specifying
																	// internal Address, it commes from
																	// ExpressionVisitor line 127
				LFIsInternal internal = isInternalRule("p_0", packetField);
				ExpressionObject exp = factory.createExpressionObject();
				exp.setIsInternal(internal);
				setLastExpression(exp);
				System.out.println(
						"----->OK, COME TO RuleContext line 584 generateRuleForExitingPacket(packetField,value) method-----");

			}

			else {
				equal.getRightExpression().setParam(value);

				ExpressionObject exp = factory.createExpressionObject();
				exp.setEqual(equal);
				if (packetField.compareTo(Constants.URL) == 0 || packetField.compareTo(Constants.PROTO) == 0) {
					// TODO if URL/PROTO put in Antecedent
					constructImpliesAntecedent(exp);
					return true;
				}
				if (!returnSnapshot.isInitialPacket()) {
					setLastExpression(exp);
				} else {
					System.out.println(">>>>>>>>>>>>seting for the initial packet fields now\n");
					if (result.getImplies() != null) {
						result.getImplies().getConsequentExpression().getAnd().getExpression().add(exp); // if it is a
																											// initial
																											// packet
																											// from a
																											// endhHost,
																											// set up
																											// for p0

					}
				}
			}
			// }
			return true;
		}

		return false;
	}

	public boolean generateRuleForExitingPacket(String packetField, MethodInvocation node) {

		if (checkPacketField(packetField) && containsLogicalUnit("p_0")) {
			String methodName = node.getName().getFullyQualifiedName(); // -->p.setField(PacketField.IP_SRC,
																		// packet.getField(PacketField.IP_DST));
			String name = null;
			StringBuilder builder = new StringBuilder();

			if (node.getExpression() != null) {
				node.getExpression().accept(new ASTVisitor() {

					public boolean visit(SimpleName node) {
						builder.append(node.getFullyQualifiedName());
						return false;
					}
				});

				name = builder.toString(); // -->name='packet'
			}
			if (methodName
					.compareTo(Constants.GET_FIELD_METHOD) == 0 /*
																 * && name.compareTo(Constants.PACKET_PARAMETER)==0
																 */) { /*
																		 * remove it, because for p0.body = p0.orig_body
																		 * in EndHost, the packet name can be anyone
																		 */

				QualifiedName field = (QualifiedName) node.arguments().get(0);
				String fieldName = field.getName().getFullyQualifiedName();

				if (checkPacketField(fieldName)) { // -->IP_DST
					LOEquals equal = factory.createLOEquals();
					equal.setLeftExpression(factory.createExpressionObject());
					equal.setRightExpression(factory.createExpressionObject());

					LFFieldOf fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_0");
					fieldOf.setField(packetField);

					equal.getLeftExpression().setFieldOf(fieldOf);

					fieldOf = factory.createLFFieldOf();
					if (returnSnapshot.isInitialPacket()) {
						fieldOf.setUnit("p_0"); /* used for p0.body = p0.orig_body in EndHost */
						System.out.println("-------p0----------???????  packetField= " + packetField + "   fieldName = "
								+ fieldName);
					} else {
						fieldOf.setUnit("p_1");
						System.out.println("-------p1----------???????  packetField= " + packetField + "   fieldName = "
								+ fieldName);
					}
					fieldOf.setField(fieldName);

					equal.getRightExpression().setFieldOf(fieldOf);

					ExpressionObject exp = factory.createExpressionObject();
					exp.setEqual(equal);
					setExpressionForPacket(exp, "p_1");
					return true;
				}

			} else if (methodName.compareTo(Constants.ENTRY_GETTER) == 0
					&& checkVariable(name).compareTo(Constants.TABLE_ENTRY_TYPE) == 0 && isDataDriven) {

				boolean p2 = false;

				@SuppressWarnings("unchecked")
				List<Expression> args = (List<Expression>) node.arguments();
				if (args.size() == 1) {
					builder.delete(0, builder.length());

					Expression expression = args.get(0);
					expression.accept(new ASTVisitor() {

						public boolean visit(NumberLiteral node) {
							builder.append(node.getToken());
							return false;
						}
					});
					int index = Integer.parseInt(builder.toString());
					List<TableEntryContext> list = returnSnapshot.getMethodContext().getEntryValues();
					String value = list.get(index).getValue(); // --> value is a packet field
					if (checkPacketField(value) && containsLogicalUnit("p_0")) {
						LOEquals equal = factory.createLOEquals();
						equal.setLeftExpression(factory.createExpressionObject());
						equal.setRightExpression(factory.createExpressionObject());

						LFFieldOf fieldOf = factory.createLFFieldOf();
						fieldOf.setUnit("p_0");
						fieldOf.setField(packetField);

						equal.getLeftExpression().setFieldOf(fieldOf);

						fieldOf = factory.createLFFieldOf();
						// TODO unsure if this is a right implementation, check if it works in other
						// cases
						if (containsLogicalUnit("p_2")) {
							fieldOf.setUnit("p_2");
							p2 = true;
						} else if (containsLogicalUnit("p_1"))
							fieldOf.setUnit("p_1");
						else
							return generateRuleForExitingPacket(packetField, value);

						fieldOf.setField(value);

						equal.getRightExpression().setFieldOf(fieldOf);

						ExpressionObject exp = factory.createExpressionObject();
						exp.setEqual(equal);

						if (p2) {
							setExpressionForPacket(exp, "p_2");
						} else
							setExpressionForPacket(exp, "p_1");

						return true;
					} else
						return generateRuleForExitingPacket(packetField, value); // when value is a new defined
																					// variable: new_port or natIp
				}

			} else if (methodName.compareTo(Constants.SEARCH_IP_METHOD) == 0 && isDataDriven) {
				// searchIP(packet.getField(PacketField.URL),ip_sipServer, ip_dns)
				// TODO SEARCH_IP_METHOD analyse
				String pname;
				builder.delete(0, builder.length());
				((Expression) node.arguments().get(0)).accept(new ASTVisitor() {

					public boolean visit(MethodInvocation node) {
						node.getExpression().accept(new ASTVisitor() {

							public boolean visit(SimpleName node) {
								builder.append(node.getFullyQualifiedName()); // = packet
								return false;
							}
						});
						return false;
					}
				});
				pname = builder.toString();
				SimpleName v1 = (SimpleName) node.arguments().get(1);
				String ipServer = v1.getFullyQualifiedName(); // ip_sipServer
				SimpleName v2 = (SimpleName) node.arguments().get(2);
				String ipDns = v2.getFullyQualifiedName(); // ip_dns

				if (pname.compareTo(Constants.PACKET_PARAMETER) == 0) {
					// TODO 23-construct p2,p3
					ExpressionObject temp = factory.createExpressionObject();

					LOExist exist2 = factory.createLOExist();
					exist2.setExpression(factory.createExpressionObject());
					LOAnd and2 = factory.createLOAnd();
					exist2.getExpression().setAnd(and2);
					entryPoint_p3 = and2;

					LFSend send2 = factory.createLFSend();

					LFFieldOf fieldOf = factory.createLFFieldOf();

					if (!containsLogicalUnit("p_2")) {
						LUNode node2 = factory.createLUNode();
						LUPacket packet2 = factory.createLUPacket();

						node2.setName("n_" + ++nodeCounter); // n_2
						packet2.setName("p_" + ++packetCounter); // p_2
						System.out.println(
								"====line796=========> node/packet Counter-1= " + nodeCounter + " " + packetCounter);
						units.add(node2);
						;
						units.add(packet2);

						exist2.getUnit().add(node2.getName());
						exist2.getUnit().add(packet2.getName());
					} else {
						exist2.getUnit().add("n_2");
						exist2.getUnit().add("p_2");
					}
					send2.setSource(netFunction);
					send2.setDestination("n_2");
					send2.setPacketOut("p_2");
					temp = factory.createExpressionObject();
					temp.setSend(send2);
					and2.getExpression().add(temp);

					LOEquals equals = factory.createLOEquals();
					equals.setLeftExpression(factory.createExpressionObject());
					equals.setRightExpression(factory.createExpressionObject());

					fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_2");
					fieldOf.setField(Constants.IP_SOURCE);

					equals.getLeftExpression().setFieldOf(fieldOf);
					equals.getRightExpression().setParam(ipServer);
					temp = factory.createExpressionObject();
					temp.setEqual(equals);
					and2.getExpression().add(temp);
					// ---
					equals = factory.createLOEquals();
					equals.setLeftExpression(factory.createExpressionObject());
					equals.setRightExpression(factory.createExpressionObject());

					fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_2");
					fieldOf.setField(Constants.IP_DESTINATION);

					equals.getLeftExpression().setFieldOf(fieldOf);
					equals.getRightExpression().setParam(ipDns);
					temp = factory.createExpressionObject();
					temp.setEqual(equals);
					and2.getExpression().add(temp);
					// ---
					equals = factory.createLOEquals();
					equals.setLeftExpression(factory.createExpressionObject());
					equals.setRightExpression(factory.createExpressionObject());

					fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_2");
					fieldOf.setField(Constants.PROTO);

					equals.getLeftExpression().setFieldOf(fieldOf);
					equals.getRightExpression().setParam(Constants.DNS_REQUEST);
					temp = factory.createExpressionObject();
					temp.setEqual(equals);
					and2.getExpression().add(temp);
					// ---
					equals = factory.createLOEquals();
					equals.setLeftExpression(factory.createExpressionObject());
					equals.setRightExpression(factory.createExpressionObject());

					fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_2");
					fieldOf.setField(Constants.URL);

					equals.getLeftExpression().setFieldOf(fieldOf);
					fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_1");
					fieldOf.setField(Constants.URL);
					equals.getRightExpression().setFieldOf(fieldOf);
					temp = factory.createExpressionObject();
					temp.setEqual(equals);
					and2.getExpression().add(temp);

					// TODO p3-Construct p3
					LOExist exist3 = factory.createLOExist();
					exist3.setExpression(factory.createExpressionObject());
					LOAnd and3 = factory.createLOAnd();
					exist3.getExpression().setAnd(and3);
					entryPoint_p4 = and3;

					LUPacket packet3 = factory.createLUPacket();
					packet3.setName("p_3");
					units.add(packet3);
					exist3.getUnit().add(packet3.getName());

					LFRecv recv3 = factory.createLFRecv();
					recv3.setSource("n_2");
					recv3.setDestination(netFunction);
					recv3.setPacketIn(packet3.getName());
					temp = factory.createExpressionObject();
					temp.setRecv(recv3);
					and3.getExpression().add(temp);

					equals = factory.createLOEquals();
					equals.setLeftExpression(factory.createExpressionObject());
					equals.setRightExpression(factory.createExpressionObject());

					fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_3");
					fieldOf.setField(Constants.IP_SOURCE);

					equals.getLeftExpression().setFieldOf(fieldOf);
					fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_2");
					fieldOf.setField(Constants.IP_DESTINATION);
					equals.getRightExpression().setFieldOf(fieldOf);
					temp = factory.createExpressionObject();
					temp.setEqual(equals);
					and3.getExpression().add(temp);
					// ---
					equals = factory.createLOEquals();
					equals.setLeftExpression(factory.createExpressionObject());
					equals.setRightExpression(factory.createExpressionObject());

					fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_3");
					fieldOf.setField(Constants.IP_DESTINATION);

					equals.getLeftExpression().setFieldOf(fieldOf);
					fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_2");
					fieldOf.setField(Constants.IP_SOURCE);
					equals.getRightExpression().setFieldOf(fieldOf);
					temp = factory.createExpressionObject();
					temp.setEqual(equals);
					and3.getExpression().add(temp);
					// ---
					equals = factory.createLOEquals();
					equals.setLeftExpression(factory.createExpressionObject());
					equals.setRightExpression(factory.createExpressionObject());

					fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_3");
					fieldOf.setField(Constants.PROTO);

					equals.getLeftExpression().setFieldOf(fieldOf);
					equals.getRightExpression().setParam(Constants.DNS_RESPONSE);
					temp = factory.createExpressionObject();
					temp.setEqual(equals);
					and3.getExpression().add(temp);
					// ---
					equals = factory.createLOEquals();
					equals.setLeftExpression(factory.createExpressionObject());
					equals.setRightExpression(factory.createExpressionObject());

					fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_3");
					fieldOf.setField(Constants.URL);

					equals.getLeftExpression().setFieldOf(fieldOf);
					fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_2");
					fieldOf.setField(Constants.URL);
					equals.getRightExpression().setFieldOf(fieldOf);
					temp = factory.createExpressionObject();
					temp.setEqual(equals);
					and3.getExpression().add(temp);
					// ----
					equals = factory.createLOEquals(); // p3.inner_dest != null
					equals.setLeftExpression(factory.createExpressionObject());
					equals.setRightExpression(factory.createExpressionObject());

					fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_3");
					fieldOf.setField(Constants.INNER_DEST);

					equals.getLeftExpression().setFieldOf(fieldOf);
					equals.getRightExpression().setParam("null");
					;
					temp = factory.createExpressionObject();
					temp.setEqual(equals);
					LONot not3 = factory.createLONot();
					not3.setExpression(temp);
					temp = factory.createExpressionObject();
					temp.setNot(not3);
					and3.getExpression().add(temp);
					// -----
					equals = factory.createLOEquals();
					equals.setLeftExpression(factory.createExpressionObject());
					equals.setRightExpression(factory.createExpressionObject());

					fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_0");
					fieldOf.setField(packetField); // Constants.IP_DESTINATION

					equals.getLeftExpression().setFieldOf(fieldOf);
					fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_3");
					fieldOf.setField(Constants.INNER_DEST);
					equals.getRightExpression().setFieldOf(fieldOf);
					temp = factory.createExpressionObject();
					temp.setEqual(equals);
					and3.getExpression().add(temp);

					// TODO PUT exist2/exist3 in entryPoint_p1
					temp = factory.createExpressionObject();
					temp.setExist(exist2);
					setLastExpression(temp);
					temp = factory.createExpressionObject();
					temp.setExist(exist3);
					setLastExpression(temp);
					return true;
				}

			} else if (methodName.compareTo(Constants.NOT_EQUALS_FIELD_METHOD) == 0) { // p.notEqualsField(PacketField.URL,
																						// domain);
																						// TODO
																						// 4-NOT_EQUALS_FIELD_METHOD
																						// analyse when arg(1) is a
																						// getField() method
				SimpleName v1 = (SimpleName) node.arguments().get(1);
				String v2 = v1.getFullyQualifiedName(); // domain
				if (name.compareTo(this.returnSnapshot.getPacketName()) == 0) {
					LOEquals equal = factory.createLOEquals();
					equal.setLeftExpression(factory.createExpressionObject());
					equal.setRightExpression(factory.createExpressionObject());

					LFFieldOf fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_0");
					fieldOf.setField(packetField);

					equal.getLeftExpression().setFieldOf(fieldOf);
					equal.getRightExpression().setParam(v2);
					ExpressionObject temp = factory.createExpressionObject();
					temp.setEqual(equal);
					LONot not = factory.createLONot();
					not.setExpression(temp);
					temp = factory.createExpressionObject();
					temp.setNot(not);
					System.out.println("==========>LINE1017 not equal method !=" + v2);
					constructImpliesAntecedent(temp);

					return true;
				}

			} else { // --> when it is used

				@SuppressWarnings("unchecked")
				List<Expression> list = (List<Expression>) node.arguments();
				if (list != null && !list.isEmpty() /* && isDataDriven */) { // if isDataDriven is here , this case will
																				// only for nat in current NFs
					String value = list.get(0).toString();
					return generateRuleForExitingPacket(packetField, value);
				} else {
					return generateRuleForExitingPacket(packetField, generateNewValue());
				}

			}

		}

		return false;
	}

	// --> iface.isInternal() or equalsField()
	public ExpressionObject generateRuleForMethod(String variableName, MethodInvocation method) {

		if (checkVariable(variableName).compareTo(Constants.NONE) == 0)
			return null;

		ExpressionObject exp = factory.createExpressionObject();

		String methodName = method.getName().getFullyQualifiedName();
		switch (methodName) {
		case Constants.IS_INTERNAL_METHOD:
			if (variableName.compareTo(Constants.INTERFACE_PARAMETER) != 0)
				return null;

			LFIsInternal temp = isInternalRule("p_" + packetCounter, Constants.IP_SOURCE);
			if (temp != null) {
				exp.setIsInternal(temp);
				return exp;
			}

			break;

		case Constants.EQUALS_FIELD_METHOD:

			if (variableName.compareTo(Constants.PACKET_PARAMETER) != 0)
				return null;
			System.out.println("///////////start to find equalsField method now ");
			QualifiedName field = (QualifiedName) method.arguments().get(0);
			String fieldValue = field.getName().getFullyQualifiedName();
			StringBuilder builder = new StringBuilder();

			ASTNode node = (ASTNode) method.arguments().get(1); // -->Packet.POP3_REQUEST
			node.accept(new ASTVisitor() {

				public boolean visit(QualifiedName node) {
					builder.append(node.getName().getFullyQualifiedName());
					return false;
				}

				public boolean visit(SimpleName node) {
					builder.append(node.getFullyQualifiedName());
					return false;
				}

				// for analyse packet.equalsField(PacketField.ENCRYPTED, String.valueOf(true))
				// or packet.equalsField(PacketField.INNER_SRC,String.valueOf(null))
				public boolean visit(MethodInvocation node) {
					if (node.arguments().get(0).toString().compareTo("true") == 0) {
						builder.append("true");
						System.out.println("///////////ruleContex line 766 methodInvocation encrypted value = true");
					}
					if (node.arguments().get(0).toString().compareTo("false") == 0) {
						builder.append("false");
					}
					if (node.arguments().get(0).toString().compareTo("null") == 0) {
						builder.append("null");
					}
					return false;
				}

			});
			String value = builder.toString(); // -->POP3_REQUEST

			LOEquals equals = factory.createLOEquals();
			equals.setLeftExpression(factory.createExpressionObject());
			equals.setRightExpression(factory.createExpressionObject());
			LFFieldOf fieldOf = factory.createLFFieldOf();
			fieldOf.setUnit("p_1");
			fieldOf.setField(fieldValue);

			equals.getLeftExpression().setFieldOf(fieldOf);
			equals.getRightExpression().setParam(value);
			System.out.println(
					"line 853~~~~~~~~~~~~~found equal: " + fieldValue + " = " + equals.getRightExpression().getParam());
			exp.setEqual(equals);

			return exp;
		default:
			break;
		}

		return null;
	}

	// -->example: entry !=null
	public ExpressionObject generateRuleForVariable(String variableName, Operator operator, int startPosition) {
		boolean negated = false;
		List<Variable> vars = getVariable(variableName);

		if (vars == null)
			return null;

		Variable var = null;
		for (Variable temp : vars) {
			if (temp.getExp() != null && temp.getExp().getStartPosition() < startPosition)
				var = temp;
		}
		if (var == null)
			return null;

		switch (var.getTypeName()) {
		case Constants.TABLE_ENTRY_TYPE:
			if (isDataDriven) {

				if (operator.equals(Operator.EQUALS))
					negated = true;

				ExpressionObject exp = factory.createExpressionObject();
				ExpressionObject end = exp;

				if (negated) {
					LONot not = factory.createLONot();
					not.setExpression(factory.createExpressionObject());
					exp.setNot(not);
					exp = not.getExpression();
				}

				LUNode node = factory.createLUNode();
				LUPacket packet = factory.createLUPacket();

				node.setName("n_" + ++nodeCounter);
				packet.setName("p_" + ++packetCounter);

				units.add(node);
				units.add(packet);

				LOExist exist = factory.createLOExist();
				exist.getUnit().add(node.getName());
				exist.getUnit().add(packet.getName());
				exist.setExpression(factory.createExpressionObject());

				LOAnd and = factory.createLOAnd();
				exist.getExpression().setAnd(and);

				LFRecv recv = factory.createLFRecv();
				recv.setSource(node.getName());
				recv.setDestination(netFunction);
				recv.setPacketIn(packet.getName());

				ExpressionObject temp = factory.createExpressionObject();
				temp.setRecv(recv);
				and.getExpression().add(temp);

				LOAnd internalAnd = factory.createLOAnd();
				LOAnd externalAnd = factory.createLOAnd();

				LFIsInternal internal = isInternalRule("p_2", Constants.IP_SOURCE);
				internalAnd.getExpression().add(factory.createExpressionObject());
				internalAnd.getExpression().get(0).setIsInternal(internal);

				LONot notInternal = factory.createLONot();
				notInternal.setExpression(factory.createExpressionObject());
				notInternal.getExpression().setIsInternal(internal);
				externalAnd.getExpression().add(factory.createExpressionObject());
				externalAnd.getExpression().get(0).setNot(notInternal);

				int internalSize = internalAnd.getExpression().size();
				int externalSize = externalAnd.getExpression().size();

				if (var.getMatchedFieldName().size() != 0) {
					int i = 0;
					for (String expr : var.getMatchedFieldName()) {
						if (expr.compareTo(Constants.ANY_VALUE) != 0 && checkPacketField(expr)
								&& i <= entryList.size()) {

							LOOr internalOr = factory.createLOOr();
							LOOr externalOr = factory.createLOOr();

							for (TableEntryContext entry : entryList) {

								if (entry.getPosition() == i) {
									LOEquals eq = factory.createLOEquals();
									eq.setLeftExpression(factory.createExpressionObject());
									eq.setRightExpression(factory.createExpressionObject());

									// My hypothesis is that the check on the entry is made on the incoming
									// packet,this is why "p_1" is hard coded
									LFFieldOf field = factory.createLFFieldOf();
									field.setUnit("p_1");
									field.setField(expr);

									eq.getLeftExpression().setFieldOf(field);

									String value = entry.getValue();

									if (checkPacketField(value)) {
										field = factory.createLFFieldOf();
										field.setUnit(packet.getName());
										field.setField(value);
										eq.getRightExpression().setFieldOf(field);
									} else if (value.compareTo(Constants.PLACE_HOLDER) == 0) {
										eq.getRightExpression().setParam(generateNewValue());
									} else {
										eq.getRightExpression().setParam(value);
									}

									temp = factory.createExpressionObject();
									temp.setEqual(eq);

									if (entry.getType().compareTo(Constants.INTERNAL) == 0) {
										internalOr.getExpression().add(temp);

									} else if (entry.getType().compareTo(Constants.EXTERNAL) == 0) {
										externalOr.getExpression().add(temp);
									}

								}

							}

							if (internalOr.getExpression().size() > 1) {
								temp = factory.createExpressionObject();
								temp.setOr(internalOr);
								internalAnd.getExpression().add(temp);
							} else if (internalOr.getExpression().size() == 1) {
								internalAnd.getExpression().add(internalOr.getExpression().get(0));
							}

							if (externalOr.getExpression().size() > 1) {
								temp = factory.createExpressionObject();
								temp.setOr(externalOr);
								externalAnd.getExpression().add(temp);
							} else if (externalOr.getExpression().size() == 1) {
								externalAnd.getExpression().add(externalOr.getExpression().get(0));
							}

						}
						i++;
					}

				}

				if (externalAnd.getExpression().size() > externalSize
						&& internalAnd.getExpression().size() > internalSize) {
					LOOr or = factory.createLOOr();
					temp = factory.createExpressionObject();
					temp.setAnd(externalAnd);
					or.getExpression().add(temp);
					temp = factory.createExpressionObject();
					temp.setAnd(internalAnd);
					or.getExpression().add(temp);

					temp = factory.createExpressionObject();
					temp.setOr(or);

					and.getExpression().add(temp);

				} else if (externalAnd.getExpression().size() > externalSize
						&& internalAnd.getExpression().size() <= internalSize) {
					and.getExpression().addAll(externalAnd.getExpression());

				} else if (externalAnd.getExpression().size() <= externalSize
						&& internalAnd.getExpression().size() > internalSize) {
					and.getExpression().addAll(internalAnd.getExpression());

				}

				exp.setExist(exist);
				// setLastExpression(end);
				entryPoint_p2 = and;
				return end;
			} else { // --> !isDataDriven

				if (operator.equals(Operator.EQUALS))
					negated = true;

				ExpressionObject exp = factory.createExpressionObject();
				ExpressionObject end = exp;

				if (negated) {
					LONot not = factory.createLONot();
					not.setExpression(factory.createExpressionObject());
					exp.setNot(not);
					exp = not.getExpression();
				}

				if (var.getMatchedFieldName().size() > 0 && containsLogicalUnit("p_1")) {
					LFMatchEntry entry = factory.createLFMatchEntry();
					for (String temp : var.getMatchedFieldName()) {
						if (temp.compareTo(Constants.ANY_VALUE) != 0) {
							LFFieldOf fo = factory.createLFFieldOf();
							fo.setUnit("p_1");
							fo.setField(temp);

							entry.getValue().add(fo);

						}
					}

					if (entry.getValue().size() > 0) {
						exp.setMatchEntry(entry);
						// setLastExpression(end);
						return end;
					}
				}

			}

			return null;
		// case Constants.STRING_TYPE:

		// MODIFY_01: #------
		case Constants.INTERFACE_TYPE:
			if (!ifSend) {
				if (operator.equals(Operator.EQUALS))
					negated = true;

				ExpressionObject exp = factory.createExpressionObject();

				if (negated) {
					LONot not = factory.createLONot();
					not.setExpression(factory.createExpressionObject());
					exp.setNot(not);
					exp = not.getExpression();
				}

				if (containsLogicalUnit(netFunction)) {
					LOEquals equals = factory.createLOEquals();
					equals.setLeftExpression(factory.createExpressionObject());
					equals.setRightExpression(factory.createExpressionObject());
					if (checkVariable(Constants.INTERFACE_ID).compareTo(Constants.NONE) != 0) {
						equals.getLeftExpression().setFieldOf(fieldOf(netFunction, Constants.IF_OUT));
						equals.getRightExpression().setParam(Constants.INTERFACE_ID);
						exp.setEqual(equals);

						setLastExpression(exp);
						this.ifSend = true; // FIXME: the flag isn't tradesafe!
					}
				}
			}

			// ------ #

		default:
			return null;
		}

	}

	// ------ Make Expression

	boolean setLastExpression(ExpressionObject expression) {
		// TODO evaluate carefully how to improve
		if (entryPoint_p1 != null && entryPoint_p1 instanceof LOAnd) {
			if (expression.getAnd() != null)
				((LOAnd) entryPoint_p1).getExpression().addAll(expression.getAnd().getExpression());
			else {
				((LOAnd) entryPoint_p1).getExpression().add(expression);
			}
			return true;
		} else
			return false;
	}

	private boolean setExpressionForPacket(ExpressionObject expression, String packetName) {
		if (containsLogicalUnit(packetName)) {

			if (packetName.compareTo("p_2") == 0) {
				if (entryPoint_p2 != null && entryPoint_p2 instanceof LOAnd) {
					if (expression.getAnd() != null)
						((LOAnd) entryPoint_p2).getExpression().addAll(expression.getAnd().getExpression());
					else
						((LOAnd) entryPoint_p2).getExpression().add(expression);
					return true;
				}
			} else if (packetName.compareTo("p_1") == 0) {
				return setLastExpression(expression);

			} else if (packetName.compareTo("p_0") == 0) {
				// TODO evaluate if necessary
				return setLastExpression(expression);
			}

		}

		return false;
	}

	public void constructImpliesAntecedent(ExpressionObject exp) {
		if (result.getImplies().getAntecedentExpression().getAnd() != null) {

			System.out.println("------------>RC line1532 constructImpliesAntecedent left");
			result.getImplies().getAntecedentExpression().getAnd().getExpression().add(exp);
		}

		else {
			System.out.println("------------>RC line1536 constructImpliesAntecedent left and");
			LFSend send = result.getImplies().getAntecedentExpression().getSend();
			LOAnd and = factory.createLOAnd();
			ExpressionObject temp1 = factory.createExpressionObject();
			temp1.setSend(send);
			and.getExpression().add(temp1);
			and.getExpression().add(exp);
			result.getImplies().getAntecedentExpression().setAnd(and);
			result.getImplies().getAntecedentExpression().setSend(null);
			System.out.println("----imply,antece and size= "
					+ result.getImplies().getAntecedentExpression().getAnd().getExpression().size());
			System.out.println("------ok finish------>RC line1547 constructImpliesAntecedent left and");
		}
	}

	// ------

	public LFFieldOf fieldOf(String elementName, String fieldName) {

		if (containsLogicalUnit(elementName)) {
			LFFieldOf fieldOf = factory.createLFFieldOf();
			fieldOf.setUnit(elementName);
			fieldOf.setField(fieldName);
			return fieldOf;
		}

		return null;
	}

	public LFIsInternal isInternalRule(String packetName, String packetField) {

		if (containsLogicalUnit(packetName) && checkPacketField(packetField)) {
			LFFieldOf fieldOf = factory.createLFFieldOf();
			LFIsInternal isInternal = factory.createLFIsInternal();

			fieldOf.setUnit(packetName);
			fieldOf.setField(packetField);
			isInternal.setFieldOf(fieldOf);

			return isInternal;
		}

		return null;

	}

	private boolean containsLogicalUnit(String name) {

		for (LogicalUnit lu : units) {
			if (lu.getName().compareTo(name) == 0)
				return true;
		}
		return false;
	}

	public String generateNewValue() {
		return "value_" + ++valueCounter;
	}

	// ------ Checks:

	public boolean checkPacketField(String field) {

		switch (field) {
		case Constants.IP_SOURCE:
			return true;
		case Constants.IP_DESTINATION:
			return true;
		case Constants.PORT_SOURCE:
			return true;
		case Constants.PORT_DESTINATION:
			return true;
		case Constants.PROTO:
			return true;
		case Constants.ORIGIN:
			return true;
		case Constants.ORIG_BODY:
			return true;
		case Constants.BODY:
			return true;
		case Constants.INNER_SRC:
			return true;
		case Constants.INNER_DEST:
			return true;
		case Constants.SEQUENCE:
			return true;
		case Constants.EMAIL_FROM:
			return true;
		case Constants.URL:
			return true;
		case Constants.OPTIONS:
			return true;
		case Constants.ENCRYPTED:
			return true;

		default:
			return false;
		}
	}

	public String checkVariable(String variable) {

		List<Variable> var = localVariable.get(variable);
		if (var == null) {
			var = globalVariable.get(variable);
			if (var == null)
				return Constants.NONE;
		}

		String type = var.get(0).getTypeName();

		switch (type) {
		case Constants.INTERFACE_TYPE:
			return Constants.INTERFACE_TYPE;
		case Constants.PACKET_TYPE:
			return Constants.PACKET_TYPE;
		case Constants.TABLE_TYPE:
			return Constants.TABLE_TYPE;
		case Constants.TABLE_ENTRY_TYPE:
			return Constants.TABLE_ENTRY_TYPE;
		case Constants.INTEGER_TYPE:
			return Constants.INTERFACE_ID;
		default:
			return Constants.NONE;
		}
	}

	// ------ Getters:

	public ExpressionObject getResult() {
		return result;
	}

	public List<LogicalUnit> getLogicaUnits() {
		return units;
	}

	public LogicalOperator getEntryPoint() {
		return entryPoint_p1;
	}

	public String getNetFunction() {
		return this.netFunction;
	}

	public ObjectFactory getObjectFactory() {
		return factory;
	}

	public List<Variable> getVariable(String variable) {

		List<Variable> var = localVariable.get(variable);
		if (var == null) {
			var = globalVariable.get(variable);
			if (var == null)
				return null;
		}

		String type = var.get(0).getTypeName();

		switch (type) {
		case Constants.INTERFACE_TYPE:
			return var;
		case Constants.PACKET_TYPE:
			return var;
		case Constants.TABLE_TYPE:
			return var;
		case Constants.TABLE_ENTRY_TYPE:
			return var;
		default:
			return null;
		}
	}

}
