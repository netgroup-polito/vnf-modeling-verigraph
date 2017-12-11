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
	//private LogicalOperator entryPoint_p0;
	//private LogicalOperator entryPoint_p3;
	private Map<String,List<Variable>> localVariable;
	private Map<String,List<Variable>> globalVariable;
	
	public boolean isDataDriven;
	//public boolean isIndirectNF;/
	
	private String netFunction;
	
	private int packetCounter = -1;
	private int nodeCounter = -1;
	private int timeCounter = -1;
	private int valueCounter = -1;
	
	public RuleContext(ObjectFactory factory, ReturnSnapshot returnSnapshot){
		this.factory = factory;
		this.returnSnapshot = returnSnapshot;
		this.netFunction = "n_"+this.returnSnapshot.getMethodContext().getContext().getClassName();
		this.units = new ArrayList<>();
		this.entryList = returnSnapshot.getMethodContext().getEntryValues();
		Collections.sort(entryList);
		
		result = factory.createExpressionObject();
		
		this.localVariable = returnSnapshot.getMethodContext().getMethodVariablesMap();
		this.globalVariable = returnSnapshot.getMethodContext().getContext().getVariablesMap();
		this.isDataDriven = returnSnapshot.getMethodContext().getContext().isDataDriven();
	//	this.isIndirectNF = returnSnapshot.getMethodContext().getContext().isIndirectNF();
		setDefaultRule(returnSnapshot.getMethodContext().getMethodName());
		
		
	}
	
	public void setDefaultRule(String methodName){
		switch(methodName){
			case Constants.MAIN_NF_METHOD:				//------------------------------------------
				
				LUNode dest = factory.createLUNode();
				LUNode source = factory.createLUNode();
				LUPacket packet = factory.createLUPacket();
				LUTime time = factory.createLUTime();
				
				dest.setName("n_"+ ++nodeCounter);
				source.setName(netFunction);
				packet.setName("p_"+ ++packetCounter);
				time.setName("t_"+ ++timeCounter);
				
				units.add(dest);
				units.add(source);
				units.add(time);
				units.add(packet);
				
				LFSend send = factory.createLFSend();
				send.setDestination(dest.getName());
				send.setSource(source.getName());
				send.setPacketOut(packet.getName());
				send.setTimeOut(time.getName());
				
				LOImplies implies = factory.createLOImplies();
				implies.setAntecedentExpression(factory.createExpressionObject());
				implies.setConsequentExpression(factory.createExpressionObject());
				
				
				if(returnSnapshot.getInterfaceName().compareTo(Constants.INTERNAL_INTERFACE)==0){		//------------------in case normal "forwardInterface ?????"
					LOAnd and = factory.createLOAnd();				
					LFIsInternal isInternal = isInternalRule(packet.getName(), Constants.IP_DESTINATION);
					ExpressionObject temp = factory.createExpressionObject();		
					
					temp.setSend(send);
					and.getExpression().add(temp);
					temp = factory.createExpressionObject();
					temp.setIsInternal(isInternal);
					and.getExpression().add(temp);
					implies.getAntecedentExpression().setAnd(and);
					
				}else if(returnSnapshot.getInterfaceName().compareTo(Constants.EXTERNAL_INTERFACE)==0){
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
				}else{
					implies.getAntecedentExpression().setSend(send);
				}
				
				LUNode n1 = factory.createLUNode();
				LUPacket p1 = factory.createLUPacket();
				LUTime t1 = factory.createLUTime();
				
				n1.setName("n_"+ ++nodeCounter);		//--> p1
				p1.setName("p_"+ ++packetCounter);
				t1.setName("t_"+ ++timeCounter);
				
				units.add(t1);
				units.add(p1);
				units.add(n1);
				
				LOExist exist = factory.createLOExist();
				exist.getUnit().add(n1.getName());
				exist.getUnit().add(p1.getName());
				exist.getUnit().add(t1.getName());
				exist.setExpression(factory.createExpressionObject());
				
				LFRecv recv = factory.createLFRecv();
				recv.setSource(n1.getName());
				recv.setDestination(netFunction);
				recv.setPacketIn(p1.getName());
				recv.setTimeIn(t1.getName());
				
				LOLessThan less = factory.createLOLessThan();
				less.setLeftExpression(factory.createExpressionObject());
				less.setRightExpression(factory.createExpressionObject());
				less.getLeftExpression().setParam(t1.getName());
				less.getRightExpression().setParam(time.getName());
				
				LOAnd and = factory.createLOAnd();
				ExpressionObject temp = factory.createExpressionObject();
				temp.setRecv(recv);	// 'recv'
				and.getExpression().add(temp);
				temp = factory.createExpressionObject();
				temp.setLessThan(less);	// 'time less than'
				and.getExpression().add(temp);
				
				exist.getExpression().setAnd(and);
				implies.getConsequentExpression().setExist(exist);
				
				result.setImplies(implies);
				
				entryPoint_p1 = and;
				break;
			case Constants.DEFINE_SENDING_PACKET_METHOD:
				LUNode dest1 = factory.createLUNode();
				LUNode source1 = factory.createLUNode();
				LUPacket packet1 = factory.createLUPacket();
				LUTime time1 = factory.createLUTime();
				
				dest1.setName("n_"+ ++nodeCounter);
				source1.setName(netFunction);
				packet1.setName("p_"+ ++packetCounter);
				time1.setName("t_"+ ++timeCounter);
				
				units.add(dest1);
				units.add(source1);
				units.add(time1);
				units.add(packet1);
				
				LFSend send1 = factory.createLFSend();
				send1.setDestination(dest1.getName());
				send1.setSource(source1.getName());
				send1.setPacketOut(packet1.getName());
				send1.setTimeOut(time1.getName());
				
				LOImplies implies1 = factory.createLOImplies();
				implies1.setAntecedentExpression(factory.createExpressionObject());
				implies1.setConsequentExpression(factory.createExpressionObject());
				
				LOAnd and1 = factory.createLOAnd();
				ExpressionObject temp1 = factory.createExpressionObject();
				temp1.setSend(send1);
				and1.getExpression().add(temp1);
				
				if(returnSnapshot.getInterfaceName().compareTo(Constants.INTERNAL_INTERFACE)==0){		//------------------in case normal "forwardInterface ?????"
								
					LFIsInternal isInternal = isInternalRule(packet1.getName(), Constants.IP_DESTINATION);
				
					temp1 = factory.createExpressionObject();
					temp1.setIsInternal(isInternal);
					and1.getExpression().add(temp1);
					implies1.getAntecedentExpression().setAnd(and1);
					
				}else if(returnSnapshot.getInterfaceName().compareTo(Constants.EXTERNAL_INTERFACE)==0){
				
					LONot not = factory.createLONot();
					LFIsInternal isInternal = isInternalRule(packet1.getName(), Constants.IP_DESTINATION);
					
					temp1 = factory.createExpressionObject();
					temp1.setIsInternal(isInternal);
					not.setExpression(temp1);
					temp1 = factory.createExpressionObject();
					temp1.setNot(not);
					and1.getExpression().add(temp1);	
					implies1.getAntecedentExpression().setAnd(and1);
					
				}else{			
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
	
	public void setExitPacketConditions(List<String> removeField){
		
		List<String> field_names = new ArrayList<String>();
		field_names.add(Constants.IP_SOURCE);
		field_names.add(Constants.IP_DESTINATION);
		field_names.add(Constants.PORT_SOURCE);
		field_names.add(Constants.PORT_DESTINATION);
		field_names.add(Constants.TRANSPORT_PROTOCOL);
		field_names.add(Constants.APPLICATION_PROTOCOL);
		field_names.add(Constants.L7DATA);
		field_names.add(Constants.OLD_SRC);
		field_names.add(Constants.OLD_DST);
		
	//  if(!returnSnapshot.isIndirectSnapshot()){
		 // System.out.println("line 248: <<<<<<,,,,,NOT idirectsnapshot  removeField= "+ removeField);
		if(removeField != null){
			if(!removeField.isEmpty()){
				field_names.removeAll(removeField);
			}
		}
		
		
		for(String field : field_names){
			ExpressionObject temp = factory.createExpressionObject();
			
			LOEquals equals_temp = factory.createLOEquals();
			equals_temp.setLeftExpression(factory.createExpressionObject());
			equals_temp.setRightExpression(factory.createExpressionObject());		
			equals_temp.getLeftExpression().setFieldOf(fieldOf("p_0",field));
			equals_temp.getRightExpression().setFieldOf(fieldOf("p_1",field));
			temp.setEqual(equals_temp);
			setLastExpression(temp);
		}
	/*}else{
		if(removeField != null){
			if(!removeField.isEmpty()){
				removeField.add(Constants.L7DATA);
				removeField.add(Constants.APPLICATION_PROTOCOL);
				field_names.removeAll(removeField);
			}
		}
		System.out.println("line 276: <<<<<<,,,,,YES idirectsnapshot  removeField= "+ removeField);
		
		for(String field : field_names){
			ExpressionObject temp = factory.createExpressionObject();
			
			LOEquals equals_temp = factory.createLOEquals();
			equals_temp.setLeftExpression(factory.createExpressionObject());
			equals_temp.setRightExpression(factory.createExpressionObject());
			if(field.equals(Constants.IP_SOURCE)){
				equals_temp.getLeftExpression().setFieldOf(fieldOf("p_0",field));
				equals_temp.getRightExpression().setFieldOf(fieldOf("p_2",Constants.IP_DESTINATION));
			}
			else if(field.equals(Constants.IP_DESTINATION)){
				equals_temp.getLeftExpression().setFieldOf(fieldOf("p_0",field));
				equals_temp.getRightExpression().setFieldOf(fieldOf("p_2",Constants.IP_SOURCE));
			}
			else if(field.equals(Constants.PORT_SOURCE)){
				equals_temp.getLeftExpression().setFieldOf(fieldOf("p_0",field));
				equals_temp.getRightExpression().setFieldOf(fieldOf("p_2",Constants.PORT_DESTINATION));
			}
			else if(field.equals(Constants.PORT_DESTINATION)){
				equals_temp.getLeftExpression().setFieldOf(fieldOf("p_0",field));
				equals_temp.getRightExpression().setFieldOf(fieldOf("p_2",Constants.PORT_SOURCE));
			}
			else{
				equals_temp.getLeftExpression().setFieldOf(fieldOf("p_0",field));
				equals_temp.getRightExpression().setFieldOf(fieldOf("p_2",field));
			}
			temp.setEqual(equals_temp);
			setExpressionForPacket(temp,"p_2");
			System.out.println("line 286:>>>>>>>>>>>>>>>>>>>>..set left fields p0=p2" +field);
		}
	}	*/
		
	}
	
	public LFFieldOf fieldOf(String elementName, String fieldName){
		
		if(containsLogicalUnit(elementName)){
			LFFieldOf fieldOf = factory.createLFFieldOf();
			fieldOf.setUnit(elementName);
			fieldOf.setField(fieldName);
			return fieldOf;
		}
		
		return null;
	}
	
	public LFIsInternal isInternalRule(String packetName, String packetField){
		
		if(containsLogicalUnit(packetName) && checkPacketField(packetField)){
			LFFieldOf fieldOf = factory.createLFFieldOf();
			LFIsInternal isInternal = factory.createLFIsInternal();
			
			fieldOf.setUnit(packetName);
			fieldOf.setField(packetField);
			isInternal.setFieldOf(fieldOf);
			
			return isInternal;
		}
		
		return null;
		
	}
	
		
	private boolean containsLogicalUnit(String name){
		
		for(LogicalUnit lu : units){
			if(lu.getName().compareTo(name)==0)
				return true;
		}
		return false;
	}
	
	boolean setLastExpression(ExpressionObject expression){
		//TODO evaluate carefully how to improve 
		
		if(entryPoint_p1!=null && entryPoint_p1 instanceof LOAnd){
			if(expression.getAnd()!=null)
				((LOAnd)entryPoint_p1).getExpression().addAll(expression.getAnd().getExpression());
			else
				((LOAnd)entryPoint_p1).getExpression().add(expression);
			return true;
		}else
			return false;
	}
	
	private boolean setExpressionForPacket(ExpressionObject expression, String packetName){
		if(containsLogicalUnit(packetName)){
			
			if(packetName.compareTo("p_2") == 0){
				if(entryPoint_p2!=null && entryPoint_p2 instanceof LOAnd){
					if(expression.getAnd()!=null)
						((LOAnd)entryPoint_p2).getExpression().addAll(expression.getAnd().getExpression());
					else
						((LOAnd)entryPoint_p2).getExpression().add(expression);
					return true;
				}
			}else if(packetName.compareTo("p_1") == 0){
				return setLastExpression(expression);
				
			}else if(packetName.compareTo("p_0") == 0){
				//TODO evaluate if necessary
				return setLastExpression(expression);
			}
			
		}
		
		return false;
	}

	public ExpressionObject getResult() {
		return result;
	}
	
	public List<LogicalUnit> getLogicaUnits(){
		return units;
	}
	
	public LogicalOperator getEntryPoint() {
		return entryPoint_p1;
	}
	
	
	public boolean generateRuleForExitingPacket(String packetField, String value){   //-->packet_in.setField(PacketField.IP_SRC, natIp);
		
		if(checkPacketField(packetField) && containsLogicalUnit("p_0")){
			LOEquals equal = factory.createLOEquals();
			equal.setLeftExpression(factory.createExpressionObject());
			equal.setRightExpression(factory.createExpressionObject());
			
			LFFieldOf fieldOf = factory.createLFFieldOf();
			fieldOf.setUnit("p_0");
			fieldOf.setField(packetField);
			
			equal.getLeftExpression().setFieldOf(fieldOf);
	// p0 = p2		
	/*		List<Variable> vars = globalVariable.get(value);
		/*	if(returnSnapshot.isIndirectSnapshot()){
				System.out.println("line 395:>>>>.>..value = "+value+"  vars size="+vars.size());
				System.out.println("vars(0)="+vars.get(0).getExp()+"  vars(1)= "+ vars.get(1).getExp());
			}
		
			if(vars!=null && vars.size()==2){
				Variable var = vars.get(1);	   // vars.get(0) = null if it is a global variable;
				if(var.getExp()!=null){
					StringBuilder methodName = new StringBuilder();
					var.getExp().accept(new ASTVisitor(){
						public boolean visit(MethodInvocation node){
							methodName.append(node.getName());
							return false;
						}
					});
				
					if(methodName.toString().compareTo(Constants.GET_FIELD_METHOD)==0){
						StringBuilder fieldName = new StringBuilder();
						var.getExp().accept(new ASTVisitor(){
							public boolean visit(MethodInvocation node){
								QualifiedName field = (QualifiedName)node.arguments().get(0);
								fieldName.append(field.getName().getFullyQualifiedName());
							return false;
							}
						});
					
					
						fieldOf = factory.createLFFieldOf();
						fieldOf.setUnit("p_2");
						fieldOf.setField(fieldName.toString());
					
						equal.getRightExpression().setFieldOf(fieldOf);
						System.out.println("'line 423' >>>>>>>>>>>>>>>>>>>running p0_ip = p2_ip  =>"+packetField+"= p2"+fieldName);
					}else{
						equal.getRightExpression().setParam(value);
						if(returnSnapshot.isIndirectSnapshot())
						System.out.println("line426 >>>>>>>>>>>>>>>>>>>running p0_ip = value");
				     }
				}else{
					equal.getRightExpression().setParam(value);
					if(returnSnapshot.isIndirectSnapshot())
					System.out.println("line429 >>>>>>>>>>>>>>>>>>>running p0_ip = value");
				}	
			}else{ */
					equal.getRightExpression().setParam(value);
			//		if(returnSnapshot.isIndirectSnapshot())
			//		System.out.println("line432 >>>>>>>>>>>>>>>>>>>running p0_ip = value "+value);
		//	}
						
			//equal.getRightExpression().setParam(value);
			
			ExpressionObject exp = factory.createExpressionObject();
			exp.setEqual(equal);
		/*	if(returnSnapshot.isIndirectSnapshot()){
				setExpressionForPacket(exp, "p_2");	
				System.out.println("--------------seting p0_ip=p2_ip for entryPoint_p2 ");
			}
			else{   */
				if(!returnSnapshot.isInitialPacket()){			
					setLastExpression(exp);
				}
				else{
					System.out.println(">>>>>>>>>>>>seting for the initial packet fields now\n");
					if(result.getImplies()!=null){
					result.getImplies().getConsequentExpression().getAnd().getExpression().add(exp);   // if it is a initial packet from a endhHost, set up for p0
					
					}	
				}
		//	}	
			return true;
		}
		
		return false;
	}
	
	public boolean generateRuleForExitingPacket(String packetField, MethodInvocation node){
		
		if(checkPacketField(packetField) && containsLogicalUnit("p_0")){
			String methodName = node.getName().getFullyQualifiedName();		//-->p.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
			String name;
			StringBuilder builder = new StringBuilder();
			
			node.getExpression().accept(new ASTVisitor() {
				
				public boolean visit(SimpleName node){		
					builder.append(node.getFullyQualifiedName());
					return false;
				}
			});
						
			name = builder.toString();		//-->name='packet'
			
			if(methodName.compareTo(Constants.GET_FIELD_METHOD)==0 && name.compareTo(Constants.PACKET_PARAMETER)==0){
				
				QualifiedName field = (QualifiedName)node.arguments().get(0);
				String fieldName = field.getName().getFullyQualifiedName();
				
				if(checkPacketField(fieldName)){	//-->IP_DST
					LOEquals equal = factory.createLOEquals();
					equal.setLeftExpression(factory.createExpressionObject());
					equal.setRightExpression(factory.createExpressionObject());
					
					LFFieldOf fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_0");
					fieldOf.setField(packetField);
					
					equal.getLeftExpression().setFieldOf(fieldOf);
					
					fieldOf = factory.createLFFieldOf();
					fieldOf.setUnit("p_1");
					fieldOf.setField(fieldName);
					
					equal.getRightExpression().setFieldOf(fieldOf);
					
					ExpressionObject exp = factory.createExpressionObject();
					exp.setEqual(equal);
					setExpressionForPacket(exp,"p_1");
					return true;
				}
				
			}else if(methodName.compareTo(Constants.ENTRY_GETTER)==0 && checkVariable(name).compareTo(Constants.TABLE_ENTRY_TYPE)==0 && isDataDriven){
				
				boolean p2 = false;
				
				@SuppressWarnings("unchecked")
				List<Expression> args = (List<Expression>)node.arguments();
				if(args.size()==1){
					builder.delete(0, builder.length());
					
					Expression expression = args.get(0);
					expression.accept(new ASTVisitor() {
						
						public boolean visit(NumberLiteral node){
							builder.append(node.getToken());
							return false;
						}
					});
					int index = Integer.parseInt(builder.toString());
					List<TableEntryContext> list = returnSnapshot.getMethodContext().getEntryValues();
					String value = list.get(index).getValue();   //--> value is a packet field
					if(checkPacketField(value) && containsLogicalUnit("p_0")){
						LOEquals equal = factory.createLOEquals();
						equal.setLeftExpression(factory.createExpressionObject());
						equal.setRightExpression(factory.createExpressionObject());
						
						LFFieldOf fieldOf = factory.createLFFieldOf();
						fieldOf.setUnit("p_0");
						fieldOf.setField(packetField);
						
						equal.getLeftExpression().setFieldOf(fieldOf);
						
						fieldOf = factory.createLFFieldOf();
						//TODO unsure if this is a right implementation, check if it works in other cases
						if(containsLogicalUnit("p_2")){
							fieldOf.setUnit("p_2");
							p2 = true;
						}
						else if(containsLogicalUnit("p_1"))
							fieldOf.setUnit("p_1");			
						else
							return generateRuleForExitingPacket(packetField, value);
						
						fieldOf.setField(value);
						
						equal.getRightExpression().setFieldOf(fieldOf);
						
						ExpressionObject exp = factory.createExpressionObject();
						exp.setEqual(equal);
						
						if(p2){
							setExpressionForPacket(exp, "p_2");
						}else
							setExpressionForPacket(exp, "p_1");
						
						return true;
					}
					else
						return generateRuleForExitingPacket(packetField, value);	// when value is a new defined variable: new_port or natIp	
				}
				
			}else{		//--> when it is used
		
				@SuppressWarnings("unchecked")
				List<Expression> list = (List<Expression>)node.arguments();
				if(list!=null && !list.isEmpty() && isDataDriven){
					String value = list.get(0).toString();
					return generateRuleForExitingPacket(packetField, value);
				}
				else{
					return generateRuleForExitingPacket(packetField, generateNewValue());
				}
				
			}

		}
		
		return false;
	}
								//--> iface.isInternal() or equalsField()
	public ExpressionObject generateRuleForMethod(String variableName,MethodInvocation method){
		
		if(checkVariable(variableName).compareTo(Constants.NONE)==0)
			return null;
		
		ExpressionObject exp = factory.createExpressionObject();
		
		String methodName = method.getName().getFullyQualifiedName();
		switch(methodName){
		case Constants.IS_INTERNAL_METHOD:
			if(variableName.compareTo(Constants.INTERFACE_PARAMETER)!=0)
				return null;
			
			LFIsInternal temp = isInternalRule("p_"+packetCounter, Constants.IP_SOURCE);
			if(temp != null){
				exp.setIsInternal(temp);
				return exp;
			}
			
			break;
			
		case Constants.EQUALS_FIELD_METHOD:
		
			if(variableName.compareTo(Constants.PACKET_PARAMETER)!=0)
				return null;
			
			QualifiedName field = (QualifiedName)method.arguments().get(0);
			String fieldValue = field.getName().getFullyQualifiedName();
			StringBuilder builder = new StringBuilder();			
			
			ASTNode node = (ASTNode)method.arguments().get(1);	//-->Packet.POP3_REQUEST
			node.accept(new ASTVisitor() {
				
				public boolean visit(QualifiedName node){
					builder.append(node.getName().getFullyQualifiedName());
					return false;
				}
				
				public boolean visit(SimpleName node){
					builder.append(node.getFullyQualifiedName());
					return false;
				}
			});
			String value = builder.toString();		//-->POP3_REQUEST
			
			LOEquals equals = factory.createLOEquals();
			equals.setLeftExpression(factory.createExpressionObject());
			equals.setRightExpression(factory.createExpressionObject());
			LFFieldOf fieldOf = factory.createLFFieldOf();
			fieldOf.setUnit("p_1");
			fieldOf.setField(fieldValue);
			
			equals.getLeftExpression().setFieldOf(fieldOf);
			equals.getRightExpression().setParam(value);
			
			exp.setEqual(equals);
			
/*>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>* generate entry_point_p3 */

		//	if(!((value.equals(Constants.DNS_RESPONSE) || value.equals(Constants.HTTP_RESPONSE)) && returnSnapshot.getInterfaceName().equals(Constants.INTERNAL_INTERFACE))){
				return exp;
		/*	}
			else{
				this.returnSnapshot.setIndirectSnapshot();
				System.out.println("line 647~~~~~~~~~~~~~equals field~~~~~~~~~~~~~~~~returnSnapshot.setIndirectSnapshot();");
				
				LUNode dest = factory.createLUNode();
				LUNode source = factory.createLUNode();
				LUPacket packet = factory.createLUPacket();
				LUTime time = factory.createLUTime();
				
				dest.setName("n_0");
				source.setName(netFunction);
				packet.setName("p_0");
				time.setName("t_0");
				
				units.add(dest);
				units.add(source);
				units.add(time);
				units.add(packet);
				
				LFSend send = factory.createLFSend();
				send.setDestination(dest.getName());
				send.setSource(source.getName());
				send.setPacketOut(packet.getName());
				send.setTimeOut(time.getName());
				
				LOImplies implies = factory.createLOImplies();
				implies.setAntecedentExpression(factory.createExpressionObject());
				implies.setConsequentExpression(factory.createExpressionObject());
								
					LOAnd and = factory.createLOAnd();				
					LFIsInternal isInternal = isInternalRule(packet.getName(), Constants.IP_DESTINATION);
					ExpressionObject temp1 = factory.createExpressionObject();		
					
					temp1.setSend(send);
					and.getExpression().add(temp1);
					temp1 = factory.createExpressionObject();
					temp1.setIsInternal(isInternal);
					and.getExpression().add(temp1);
					implies.getAntecedentExpression().setAnd(and);
				
				
				LUNode n1 = factory.createLUNode();
				LUPacket p1 = factory.createLUPacket();
				LUTime t1 = factory.createLUTime();
				
				n1.setName("n_1");		
				p1.setName("p_1");
				t1.setName("t_1");
				
				units.add(t1);
				units.add(p1);
				units.add(n1);
				
				LOExist exist = factory.createLOExist();
				exist.getUnit().add(n1.getName());
				exist.getUnit().add(p1.getName());
				exist.getUnit().add(t1.getName());
				exist.setExpression(factory.createExpressionObject());
				
				LFRecv recv = factory.createLFRecv();
				recv.setSource(n1.getName());
				recv.setDestination(netFunction);
				recv.setPacketIn(p1.getName());
				recv.setTimeIn(t1.getName());
				
				LOLessThan less = factory.createLOLessThan();
				less.setLeftExpression(factory.createExpressionObject());
				less.setRightExpression(factory.createExpressionObject());
				less.getLeftExpression().setParam(t1.getName());
				less.getRightExpression().setParam(time.getName());
				
				and = factory.createLOAnd();
				temp1 = factory.createExpressionObject();
				temp1.setRecv(recv);	// 'recv'
				and.getExpression().add(temp1);
				temp1 = factory.createExpressionObject();
				temp1.setLessThan(less);	// 'time less than'
				and.getExpression().add(temp1);
				
				LONot not = factory.createLONot();
				isInternal = isInternalRule(p1.getName(), Constants.IP_SOURCE);
				temp1 = factory.createExpressionObject();
				temp1.setIsInternal(isInternal);
				not.setExpression(temp1);
				temp1 = factory.createExpressionObject();
				temp1.setNot(not);
				and.getExpression().add(temp1);
				
				equals = factory.createLOEquals();
				equals.setLeftExpression(factory.createExpressionObject());
				equals.setRightExpression(factory.createExpressionObject());
				fieldOf = factory.createLFFieldOf();
				fieldOf.setUnit("p_1");
				fieldOf.setField(fieldValue);
				
				equals.getLeftExpression().setFieldOf(fieldOf);
				equals.getRightExpression().setParam(value);
				temp1 = factory.createExpressionObject();
				temp1.setEqual(equals);
				and.getExpression().add(temp1);
		//----		
				LUNode n2 = factory.createLUNode();
				LUPacket p2 = factory.createLUPacket();
				LUTime t2 = factory.createLUTime();
				
				n2.setName("n_2");		
				p2.setName("p_2");
				t2.setName("t_2");
				
				units.add(t2);
				units.add(p2);
				units.add(n2);
				
				LOExist exist1 = factory.createLOExist();
				exist1.getUnit().add(n2.getName());
				exist1.getUnit().add(p2.getName());
				exist1.getUnit().add(t2.getName());
				exist1.setExpression(factory.createExpressionObject());
				
				recv = factory.createLFRecv();
				recv.setSource(n2.getName());
				recv.setDestination(netFunction);
				recv.setPacketIn(p2.getName());
				recv.setTimeIn(t2.getName());
				
				less = factory.createLOLessThan();
				less.setLeftExpression(factory.createExpressionObject());
				less.setRightExpression(factory.createExpressionObject());
				less.getLeftExpression().setParam(t2.getName());
				less.getRightExpression().setParam(t1.getName());
				
				LOAnd and1 = factory.createLOAnd();
				temp1 = factory.createExpressionObject();
				temp1.setRecv(recv);	// 'recv'
				and1.getExpression().add(temp1);
				temp1 = factory.createExpressionObject();
				temp1.setLessThan(less);	// 'time less than'
				and1.getExpression().add(temp1);
								
				isInternal = isInternalRule(p2.getName(), Constants.IP_SOURCE);
			    temp1 = factory.createExpressionObject();		
				temp1.setIsInternal(isInternal);
				and1.getExpression().add(temp1);
		*/		
		/*		equals = factory.createLOEquals();
				equals.setLeftExpression(factory.createExpressionObject());
				equals.setRightExpression(factory.createExpressionObject());
				fieldOf = factory.createLFFieldOf();
				fieldOf.setUnit("p_0");
				fieldOf.setField(Constants.IP_SOURCE);
				
				equals.getLeftExpression().setFieldOf(fieldOf);
				fieldOf = factory.createLFFieldOf();
				fieldOf.setUnit("p_2");
				fieldOf.setField(Constants.IP_DESTINATION);
				equals.getRightExpression().setFieldOf(fieldOf);
				temp1 = factory.createExpressionObject();
				temp1.setEqual(equals);
				and1.getExpression().add(temp1);
				
				equals = factory.createLOEquals();
				equals.setLeftExpression(factory.createExpressionObject());
				equals.setRightExpression(factory.createExpressionObject());
				fieldOf = factory.createLFFieldOf();
				fieldOf.setUnit("p_0");
				fieldOf.setField(Constants.PORT_SOURCE);
				
				equals.getLeftExpression().setFieldOf(fieldOf);
				fieldOf = factory.createLFFieldOf();
				fieldOf.setUnit("p_2");
				fieldOf.setField(Constants.PORT_DESTINATION);
				equals.getRightExpression().setFieldOf(fieldOf);
				temp1 = factory.createExpressionObject();
				temp1.setEqual(equals);
				and1.getExpression().add(temp1);
				
		/*		equals = factory.createLOEquals();
				equals.setLeftExpression(factory.createExpressionObject());
				equals.setRightExpression(factory.createExpressionObject());
				fieldOf = factory.createLFFieldOf();
				fieldOf.setUnit("p_0");
				fieldOf.setField(Constants.IP_DESTINATION);
				
				equals.getLeftExpression().setFieldOf(fieldOf);
				fieldOf = factory.createLFFieldOf();
				fieldOf.setUnit("p_2");
				fieldOf.setField(Constants.IP_SOURCE);
				equals.getRightExpression().setFieldOf(fieldOf);
				temp1 = factory.createExpressionObject();
				temp1.setEqual(equals);
				and1.getExpression().add(temp1);
				
				equals = factory.createLOEquals();
				equals.setLeftExpression(factory.createExpressionObject());
				equals.setRightExpression(factory.createExpressionObject());
				fieldOf = factory.createLFFieldOf();
				fieldOf.setUnit("p_0");
				fieldOf.setField(Constants.PORT_DESTINATION);
				
				equals.getLeftExpression().setFieldOf(fieldOf);
				fieldOf = factory.createLFFieldOf();
				fieldOf.setUnit("p_2");
				fieldOf.setField(Constants.PORT_DESTINATION);
				equals.getRightExpression().setFieldOf(fieldOf);
				temp1 = factory.createExpressionObject();
				temp1.setEqual(equals);
				and1.getExpression().add(temp1);
		*/		
		/*		entryPoint_p2 = and1;
				System.out.println("line:829>>>>>>>>>>>>>>>>>entryPoint_p2 = and1;");
				
				exist1.getExpression().setAnd(and1);
				
				temp1 = factory.createExpressionObject();
				temp1.setExist(exist1);
				and.getExpression().add(temp1);
				
	//(p_0.APPLICATION_PROTOCOL == p_1.APPLICATION_PROTOCOL) && (p_0.L7DATA == p_1.L7DATA)			
				equals = factory.createLOEquals();
				equals.setLeftExpression(factory.createExpressionObject());
				equals.setRightExpression(factory.createExpressionObject());
				fieldOf = factory.createLFFieldOf();
				fieldOf.setUnit("p_0");
				fieldOf.setField(Constants.L7DATA);
				
				equals.getLeftExpression().setFieldOf(fieldOf);
				fieldOf = factory.createLFFieldOf();
				fieldOf.setUnit("p_1");
				fieldOf.setField(Constants.L7DATA);
				equals.getRightExpression().setFieldOf(fieldOf);
				temp1 = factory.createExpressionObject();
				temp1.setEqual(equals);
				and.getExpression().add(temp1);
				
				equals = factory.createLOEquals();
				equals.setLeftExpression(factory.createExpressionObject());
				equals.setRightExpression(factory.createExpressionObject());
				fieldOf = factory.createLFFieldOf();
				fieldOf.setUnit("p_0");
				fieldOf.setField(Constants.APPLICATION_PROTOCOL);
				
				equals.getLeftExpression().setFieldOf(fieldOf);
				fieldOf = factory.createLFFieldOf();
				fieldOf.setUnit("p_1");
				fieldOf.setField(Constants.APPLICATION_PROTOCOL);
				equals.getRightExpression().setFieldOf(fieldOf);
				temp1 = factory.createExpressionObject();
				temp1.setEqual(equals);
				and.getExpression().add(temp1);
				
				entryPoint_p1 = and;
				System.out.println("line:871>>>>>>>>>>>>>>>>>>>>.entryPoint_p1 = and;");
				
				exist.getExpression().setAnd(and);
				implies.getConsequentExpression().setExist(exist);
				
				result.setImplies(implies);
			}
		*/	
			
			
		
	/*	case Constants.ENTRY_GETTER:
			@SuppressWarnings("unchecked") 
			List<Expression> args = (List<Expression>)method.arguments();
			builder = new StringBuilder();
				if(args.size()==1){
					builder.delete(0, builder.length());
					
					Expression expression = args.get(0);
					expression.accept(new ASTVisitor() {
						
						public boolean visit(NumberLiteral node){
							builder.append(node.getToken());
							return false;
						}
					});
					int index = Integer.parseInt(builder.toString());
					List<TableEntryContext> list = returnSnapshot.getMethodContext().getEntryValues();

				    value=null;
				    for(TableEntryContext tnc: list){						
						if(tnc.getPosition()==index){		    
				            value = tnc.getValue(); 
				            if(!checkPacketField(value) && containsLogicalUnit("p_2")){
				            	LOEquals equal = factory.createLOEquals();
				            	equal.setLeftExpression(factory.createExpressionObject());
				            	equal.setRightExpression(factory.createExpressionObject());
						
				            	fieldOf = factory.createLFFieldOf();
				            	fieldOf.setUnit("p_2");
				            	fieldOf.setField(Constants.L7DATA);
						
								equal.getLeftExpression().setFieldOf(fieldOf);
								equal.getRightExpression().setParam(value);;
								
								exp = factory.createExpressionObject();
								exp.setEqual(equal);
								setExpressionForPacket(exp,"p_2");
				            }
						}
					break;
				    }
				}	*/
		default:
			break;
		}
		
		return null;
	}
														//-->example: entry  !=null
	public ExpressionObject generateRuleForVariable(String variableName, Operator operator, int startPosition){
		boolean negated = false;
		List<Variable> vars = getVariable(variableName);
		
		if(vars == null)
			return null;
		
		Variable var = null;
		for(Variable temp : vars){
			if(temp.getExp()!=null && temp.getExp().getStartPosition() < startPosition)
				var = temp;
		}
		if(var == null)
			return null;
		
		switch(var.getTypeName()){
			case Constants.TABLE_ENTRY_TYPE:				
				if(isDataDriven){
					
					if(operator.equals(Operator.EQUALS))
						negated=true;
					
					ExpressionObject exp = factory.createExpressionObject();
					ExpressionObject end = exp;
					
					if(negated){
						LONot not = factory.createLONot();
						not.setExpression(factory.createExpressionObject());
						exp.setNot(not);
						exp = not.getExpression();
					}
					
					LUNode node = factory.createLUNode();
					LUPacket packet = factory.createLUPacket();
					LUTime time = factory.createLUTime();
					
					node.setName("n_"+ ++nodeCounter);
					packet.setName("p_"+ ++packetCounter);
					time.setName("t_"+ ++timeCounter);
					
					units.add(node);
					units.add(packet);
					units.add(time);
					
					LOExist exist = factory.createLOExist();
					exist.getUnit().add(node.getName());
					exist.getUnit().add(packet.getName());		
					exist.getUnit().add(time.getName());
					exist.setExpression(factory.createExpressionObject());
					
					LOAnd and = factory.createLOAnd();
					exist.getExpression().setAnd(and);
					
					LOLessThan less = factory.createLOLessThan();
					less.setLeftExpression(factory.createExpressionObject());
					less.setRightExpression(factory.createExpressionObject());
					less.getLeftExpression().setParam(time.getName());
					less.getRightExpression().setParam("t_"+ (timeCounter-1));
					
					LFRecv recv = factory.createLFRecv();
					recv.setSource(node.getName());
					recv.setDestination(netFunction);
					recv.setPacketIn(packet.getName());
					recv.setTimeIn(time.getName());
		
					ExpressionObject temp = factory.createExpressionObject();
					temp.setRecv(recv);
					and.getExpression().add(temp);
					
					temp = factory.createExpressionObject();
					temp.setLessThan(less);
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
					
					if(var.getMatchedFieldName().size()!=0){
						int i = 0;
						for(String expr : var.getMatchedFieldName()){
							if(expr.compareTo(Constants.ANY_VALUE)!=0 && checkPacketField(expr) && i <= entryList.size()){
								
								LOOr internalOr = factory.createLOOr();
								LOOr externalOr = factory.createLOOr();
								
								for(TableEntryContext entry : entryList){
	
									if(entry.getPosition() == i){
										LOEquals eq = factory.createLOEquals();
										eq.setLeftExpression(factory.createExpressionObject());
										eq.setRightExpression(factory.createExpressionObject());
										
										//My hypothesis is that the check on the entry is made on the incoming packet,this is why "p_1" is hard coded
										LFFieldOf field = factory.createLFFieldOf();
										field.setUnit("p_1");
										field.setField(expr);
										
										eq.getLeftExpression().setFieldOf(field);
										
										String value = entry.getValue();
										
										if(checkPacketField(value)){
											field = factory.createLFFieldOf();
											field.setUnit(packet.getName());
											field.setField(value);
											eq.getRightExpression().setFieldOf(field);
										}else if(value.compareTo(Constants.PLACE_HOLDER)==0){
											eq.getRightExpression().setParam(generateNewValue());
										}else{
											eq.getRightExpression().setParam(value);
										}
									
										temp = factory.createExpressionObject();
										temp.setEqual(eq);
										
										if(entry.getType().compareTo(Constants.INTERNAL)==0){
											internalOr.getExpression().add(temp);
											
										}else if(entry.getType().compareTo(Constants.EXTERNAL)==0){
											externalOr.getExpression().add(temp);
										}
										
									}
									
								}
								
								if(internalOr.getExpression().size()>1){
									temp = factory.createExpressionObject();
									temp.setOr(internalOr);
									internalAnd.getExpression().add(temp);
								}else if(internalOr.getExpression().size()==1){							
									internalAnd.getExpression().add(internalOr.getExpression().get(0));
								}
								
								if(externalOr.getExpression().size()>1){
									temp = factory.createExpressionObject();
									temp.setOr(externalOr);
									externalAnd.getExpression().add(temp);
								}else if(externalOr.getExpression().size()==1){							
									externalAnd.getExpression().add(externalOr.getExpression().get(0));
								}
								
							}
							i++;
						}
						
					}
					
					if(externalAnd.getExpression().size()>externalSize && internalAnd.getExpression().size()>internalSize){
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
						
					}else if(externalAnd.getExpression().size()>externalSize && internalAnd.getExpression().size()<=internalSize){
						and.getExpression().addAll(externalAnd.getExpression());
						
					}else if(externalAnd.getExpression().size()<=externalSize && internalAnd.getExpression().size()>internalSize){					
						and.getExpression().addAll(internalAnd.getExpression());
						
					}
					
					exp.setExist(exist);
					//setLastExpression(end);
					entryPoint_p2 = and;
					return end;
				}else{		//--> !isDataDriven
				
					if(operator.equals(Operator.EQUALS))	
						negated=true;
					
					ExpressionObject exp = factory.createExpressionObject();
					ExpressionObject end = exp;
					
					if(negated){
						LONot not = factory.createLONot();
						not.setExpression(factory.createExpressionObject());
						exp.setNot(not);
						exp = not.getExpression();
					}
					
					
					if(var.getMatchedFieldName().size()>0 && containsLogicalUnit("p_1")){
						LFMatchEntry entry = factory.createLFMatchEntry();
						for(String temp : var.getMatchedFieldName()){
							if(temp.compareTo(Constants.ANY_VALUE)!=0){
								LFFieldOf fo = factory.createLFFieldOf();
								fo.setUnit("p_1");
								fo.setField(temp);
								
								entry.getValue().add(fo);
								
							}
						}
						
						if(entry.getValue().size()>0){
							exp.setMatchEntry(entry);
							//setLastExpression(end);
							return end;
						}
					}
					
				}
								
				return null;
			// case Constants.STRING_TYPE:
			default:
				return null;
		}
		
	}
	
	public boolean checkPacketField(String field){
		
		switch(field){
			case Constants.IP_SOURCE:
				return true;
			case Constants.IP_DESTINATION:
				return true;
			case Constants.PORT_SOURCE:
				return true;
			case Constants.PORT_DESTINATION:
				return true;
			case Constants.TRANSPORT_PROTOCOL:
				return true;
			case Constants.APPLICATION_PROTOCOL:
				return true;
			case Constants.L7DATA:
				return true;
			case Constants.OLD_SRC:
				return true;
			case Constants.OLD_DST:
				return true;
			default:
				return false;
		}
		
	}
	
	public List<Variable> getVariable(String variable){
		
		List<Variable> var = localVariable.get(variable);
		if(var == null){
			var = globalVariable.get(variable);
			if(var == null)
				return null;
		}
		
		String type = var.get(0).getTypeName();
			
		switch(type){
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
	
	public String checkVariable(String variable){
	
		List<Variable> var = localVariable.get(variable);
		if(var == null){
			var = globalVariable.get(variable);
			if(var == null)
				return Constants.NONE;
		}
		
		String type = var.get(0).getTypeName();
			
		switch(type){
			case Constants.INTERFACE_TYPE:
				return Constants.INTERFACE_TYPE;	
			case Constants.PACKET_TYPE:
				return Constants.PACKET_TYPE;
			case Constants.TABLE_TYPE:
				return Constants.TABLE_TYPE;
			case Constants.TABLE_ENTRY_TYPE:
				return Constants.TABLE_ENTRY_TYPE;
			default:
				return Constants.NONE;
		}
		
	}
	
	public String generateNewValue(){
		return "value_"+ ++valueCounter;
	}
	
	public String getNetFunction(){
		return this.netFunction;
	}
	
	public ObjectFactory getObjectFactory(){
		return factory;
	}
	
}
