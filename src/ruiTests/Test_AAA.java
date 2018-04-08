
package ruiTests;

import java.util.ArrayList;
import java.util.HashMap;

import com.microsoft.z3.Context;
import com.microsoft.z3.DatatypeExpr;
import com.microsoft.z3.EnumSort;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Model;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

import it.polito.verigraph.mcnet.components.*;
import it.polito.verigraph.mcnet.netobjs.PacketModel;
import it.polito.verigraph.mcnet.netobjs.PolitoEndHost;
import ruiNFs.*;


/**
 *  Test AAA												<p/>
 * 
 *    | AAAClient a | ------| firewall | ----| AAA Server |	<p/>

 */
public class Test_AAA {
	
	public Checker checker;
	public Context ctx;
	public Rule_AAAClient a;
	public Rule_AclFirewall fw;
	public Rule_AAA aaa;
	
	public	Test_AAA(){
		ctx = new Context();
		
		NetContext nctx = new NetContext (ctx,new String[]{"a", "fw", "aaa"},
				new String[]{"ip_a", "ip_fw","ip_aaa"});
		Network net = new Network (ctx,new Object[]{nctx});

		a = new Rule_AAAClient(ctx, new Object[]{nctx.nm.get("a"), net, nctx});
		fw = new Rule_AclFirewall(ctx, new Object[]{nctx.nm.get("fw"), net, nctx});
		
		aaa = new Rule_AAA(ctx, new Object[]{nctx.nm.get("aaa"), net, nctx});
	
		ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>>();
			ArrayList<DatatypeExpr> al1 = new ArrayList<DatatypeExpr>();			
			ArrayList<DatatypeExpr> al2 = new ArrayList<DatatypeExpr>();
			ArrayList<DatatypeExpr> al3 = new ArrayList<DatatypeExpr>();
			
			
			al1.add(nctx.am.get("ip_a"));
			al2.add(nctx.am.get("ip_fw"));
			al3.add(nctx.am.get("ip_aaa"));
			
			
		adm.add(new Tuple<>(a, al1));
	    adm.add(new Tuple<>(fw, al2));
	    adm.add(new Tuple<>(aaa, al3));

	    net.setAddressMappings(adm);
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtA = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtA.add(new Tuple<>(nctx.am.get("ip_aaa"), fw));
	    
    	ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtfw = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
    	rtfw.add(new Tuple<>(nctx.am.get("ip_a"), a));
    	rtfw.add(new Tuple<>(nctx.am.get("ip_aaa"), aaa));

    	ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtaaa = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
    	rtaaa.add(new Tuple<>(nctx.am.get("ip_a"), fw));

    	net.routingTable(a, rtA);
	    net.routingTable(fw, rtfw);
	    net.routingTable(aaa, rtaaa);
	    
	    net.attach(a, fw, aaa); 
	
	    fw.installAclFirewall();
	    
	    aaa.addEntry(ctx.mkInt(1));  // body
	    aaa.installAAA(ctx.mkInt(2), ctx.mkInt(3), ctx.mkInt(4), ctx.mkInt(5), ctx.mkInt(6));
	    
	    						//(Expr ip_aaa, Expr new_port, Expr namePw)
	    a.installAAAClient(nctx.am.get("ip_aaa"), ctx.mkInt(2), ctx.mkInt(1));
	    
	    checker = new Checker(ctx,nctx,net);
	}
	
	public static void main(String[] args) throws Z3Exception
    {
    	
    	IsolationResult ret;
    	
    	Test_AAA model = new Test_AAA();
    	model.resetZ3();
    	
    	//IsolationResult ret
    	ret = model.checker.checkIsolationProperty(model.a,model.aaa );
    	
    	//model.printVector(ret.assertions);
    	if (ret.result == Status.UNSATISFIABLE){
     	   System.out.println("UNSAT"); // Nodes a and b are isolated
    	}else{
     		System.out.println("SAT ");
     		System.out.println(ret.model);
     	
     	}
    	System.out.println("=====================================");
    	model.resetZ3();
    	ret = model.checker.checkIsolationProperty(model.aaa,model.a );   	
    	//model.printVector(ret.assertions);
    	if (ret.result == Status.UNSATISFIABLE){
     	   System.out.println("UNSAT"); // expected result is UNSAT, because of flow isolation, AAA has never before sent a packet to client a
    	}else{
     		System.out.println("SAT ");
     		System.out.println(ret.model);
     	}
    	
    }

	
	public void resetZ3() throws Z3Exception{
	    HashMap<String, String> cfg = new HashMap<String, String>();
	    cfg.put("model", "true");
	     ctx = new Context(cfg);
	}
	
	public void printVector (Object[] array){
	    int i=0;
	    System.out.println( "*** Printing vector ***");
	    for (Object a : array){
	        i+=1;
	        System.out.println( "#"+i);
	        System.out.println(a);
	        System.out.println(  "*** "+ i+ " elements printed! ***");
	    }
	}
	
	public void printModel (Model model) throws Z3Exception{
	    for (FuncDecl d : model.getFuncDecls()){
	    	System.out.println(d.getName() +" = "+ d.toString());
	    	  System.out.println("");
	    }
	}

}


	

