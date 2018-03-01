package ruiTests;

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
import ruiNFs.Rule_AclFirewall;
import ruiNFs.Rule_SipServer2;
import ruiNFs.Rule_UserAgentClient;


/**
 * <p/>
 * Custom test	<p/>
 * 
 * | URC_A | <------> | SipServer | <------> | URC_B |
 */
public class Rule_SipServerTest {
	
	public Checker checker;
	public Context ctx;
	public Rule_UserAgentClient a,b;
	public Rule_SipServer2 sipServer;

	public	Rule_SipServerTest(){
		
		ctx = new Context();
		NetContext nctx = new NetContext (ctx,new String[]{"a", "b", "sipServer"},
												new String[]{"ip_a", "ip_b", "ip_sipServer"});
		Network net = new Network (ctx,new Object[]{nctx});
		
		a = new Rule_UserAgentClient(ctx, new Object[]{nctx.nm.get("a"), net, nctx});
		b = new Rule_UserAgentClient(ctx, new Object[]{nctx.nm.get("b"), net, nctx});
		sipServer = new Rule_SipServer2(ctx, new Object[]{nctx.nm.get("sipServer"), net, nctx});
	    
		
		ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>>();
		ArrayList<DatatypeExpr> al1 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al2 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al3 = new ArrayList<DatatypeExpr>();
		al1.add(nctx.am.get("ip_a"));
		al2.add(nctx.am.get("ip_b"));
		al3.add(nctx.am.get("ip_sipServer"));
		adm.add(new Tuple<>(a,al1));
	    adm.add(new Tuple<>(b, al2));
	    adm.add(new Tuple<>(sipServer, al3));
	    net.setAddressMappings(adm);
	
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rta = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    	rta.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_b"), sipServer));
	    	rta.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_sipServer"), sipServer));
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtb = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();	   
	    	rtb.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_a"), sipServer));
	    	rta.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_sipServer"), sipServer));
	    
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rt2 = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rt2.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_a"),a));
	    rt2.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_b"),b));

	    net.routingTable(a, rta);
	    net.routingTable(b, rtb);
	    net.routingTable(sipServer, rt2);

	    net.attach(a, b, sipServer);
	    a.installUserAgentClient(nctx.am.get("ip_sipServer"), ctx.mkInt(1));
	    b.installUserAgentClient(nctx.am.get("ip_sipServer"), ctx.mkInt(2));
	    
	    sipServer.installSipServer2(nctx.am.get("ip_sipServer"));
	    	    
	    checker = new Checker(ctx,nctx,net);
	}
	
	public static void main(String[] args) throws Z3Exception
    {
    	IsolationResult ret;
    	Rule_FirewallTest model = new Rule_FirewallTest();
    	
    	model.resetZ3();
    	
    	//IsolationResult ret
    	ret = model.checker.checkIsolationProperty(model.b,model.a );
    	
    	//model.printVector(ret.assertions);
    	if (ret.result == Status.UNSATISFIABLE){
     	   System.out.println("UNSAT"); // Nodes a and b are isolated
    	}else{
     		System.out.println("SAT ");
     		
     	
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

