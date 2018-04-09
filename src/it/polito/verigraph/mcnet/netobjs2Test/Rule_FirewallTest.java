package it.polito.verigraph.mcnet.netobjs2Test;

import java.util.ArrayList;
import java.util.HashMap;

import com.microsoft.z3.Context;
import com.microsoft.z3.DatatypeExpr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Model;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

import it.polito.verigraph.mcnet.components.*;
import it.polito.verigraph.mcnet.netobjs.*;
import it.polito.verigraph.mcnet.netobjs2.Rule_AclFirewall;


/**
 * <p/>
 * Custom test	<p/>
 * 
 * | A | <------> | Firewall | <------> | B |
 */
public class Rule_FirewallTest {
	
	public Checker checker;
	public Context ctx;
	public PolitoEndHost a,b;
	public Rule_AclFirewall firewall;

	public	Rule_FirewallTest(){
		
		ctx = new Context();
		NetContext nctx = new NetContext (ctx,new String[]{"a", "b", "firewall"},
												new String[]{"ip_a", "ip_b", "ip_firewall"});
		Network net = new Network (ctx,new Object[]{nctx});
		
		a = new PolitoEndHost(ctx, new Object[]{nctx.nm.get("a"), net, nctx});
		b = new PolitoEndHost(ctx, new Object[]{nctx.nm.get("b"), net, nctx});
		firewall = new Rule_AclFirewall(ctx, new Object[]{nctx.nm.get("firewall"), net, nctx});
	    
		
		ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>>();
		ArrayList<DatatypeExpr> al1 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al2 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al3 = new ArrayList<DatatypeExpr>();
		al1.add(nctx.am.get("ip_a"));
		al2.add(nctx.am.get("ip_b"));
		al3.add(nctx.am.get("ip_firewall"));
		adm.add(new Tuple<>(a,al1));
	    adm.add(new Tuple<>(b, al2));
	    adm.add(new Tuple<>(firewall, al3));
	    net.setAddressMappings(adm);
	
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rta = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    	rta.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_b"), firewall));
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtb = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();	   
	    	rtb.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_a"), firewall));
	    
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rt2 = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rt2.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_a"),a));
	    rt2.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_b"),b));

	    net.routingTable(a, rta);
	    net.routingTable(b, rtb);
	    net.routingTable(firewall, rt2);

	    net.attach(a, b, firewall);
	    
	 //   firewall.addEntry(nctx.am.get("ip_a"), nctx.am.get("ip_b"));
	    firewall.addEntry(nctx.am.get("ip_b"), nctx.am.get("ip_a"));
	    firewall.installAclFirewall();
	    	    
	    checker = new Checker(ctx,nctx,net);
	}
	
	public static void main(String[] args) throws Z3Exception
    {
    	IsolationResult ret;
    	Rule_FirewallTest model = new Rule_FirewallTest();
    	
    	model.resetZ3();
    	
    	//IsolationResult ret
    	ret = model.checker.checkIsolationProperty(model.a,model.b );
    	
    	System.out.println("***********************assertions***************************");
    	//model.printVector(ret.assertions);
    	
    	if (ret.result == Status.UNSATISFIABLE){
     	   System.out.println("UNSAT"); // Nodes a and b are isolated
    	}else{
     		System.out.println("SAT ");
     		System.out.println("***********************model***************************");
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

