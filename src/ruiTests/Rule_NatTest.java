package ruiTests;

import java.util.ArrayList;
import java.util.HashMap;

import com.microsoft.z3.Context;
import com.microsoft.z3.DatatypeExpr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Model;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

import it.polito.verigraph.mcnet.components.*;
import ruiNFs.*;
/**
 * <p/>
 * Cache - Nat -     test												<p/>
 * 
 *    | ENDHOST_A | ----| CACHE |----- | NAT | ------- | SERVER_B |		<p/>
  *    ...................|													<p/>
 *    | SERVER_C | --------													<p/>
 *
 *
 */
public class Rule_NatTest {
	
	public Checker checker;
	public Context ctx;
	public Rule_WebClient endHostA;
	public Rule_Nat nat;
//	public MyNAT nat;
	public Rule_WebServer serverB,serverC;
	public Rule_WebCache cache;
	
	public	Rule_NatTest(){
	
		ctx = new Context();
		NetContext nctx = new NetContext (ctx,new String[]{"endHostA", "cache", "nat","Ids","Fw","serverB","serverC"},
												new String[]{"ip_endHostA", "ip_cache", "ip_nat","ip_ids","ip_Fw","ip_serverB","ip_serverC"});
		Network net = new Network (ctx,new Object[]{nctx});
		
		endHostA = new Rule_WebClient(ctx, new Object[]{nctx.nm.get("endHostA"), net, nctx});
		serverB = new Rule_WebServer(ctx, new Object[]{nctx.nm.get("serverB"), net, nctx});
		serverC = new Rule_WebServer(ctx, new Object[]{nctx.nm.get("serverC"), net, nctx});
		cache = new Rule_WebCache(ctx, new Object[]{nctx.nm.get("cache"), net, nctx});
		nat = new Rule_Nat(ctx, new Object[]{nctx.nm.get("nat"), net, nctx});
	//	nat = new MyNAT(ctx, new Object[]{nctx.nm.get("nat"), net, nctx});
		
	    
		ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>>();
		ArrayList<DatatypeExpr> al1 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al2 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al3 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al6 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al7 = new ArrayList<DatatypeExpr>();
		
		al1.add(nctx.am.get("ip_endHostA"));
		al2.add(nctx.am.get("ip_cache"));
		al3.add(nctx.am.get("ip_nat"));
		al6.add(nctx.am.get("ip_serverB"));
		al7.add(nctx.am.get("ip_serverC"));
		
	    adm.add(new Tuple<>(endHostA, al1));
	    adm.add(new Tuple<>(cache, al2));
	    adm.add(new Tuple<>(nat, al3));
	    adm.add(new Tuple<>(serverB,al6));
	    adm.add(new Tuple<>(serverC,al7));
	    
	    net.setAddressMappings(adm);
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtA = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtA.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverB"), cache));
	    rtA.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverC"), cache));
	    rtA.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_cache"), cache));
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtB = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtB.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_endHostA"), nat));
	    rtB.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverC"), nat));
	    rtB.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_cache"), nat));
	    rtB.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_nat"), nat));
	   
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtC = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtC.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_endHostA"), cache));
	    rtC.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverB"), cache));
	    rtC.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_cache"), cache));
	    rtC.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_nat"), cache));
//	   
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtCache = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtCache.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_endHostA"), endHostA));
	    rtCache.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverB"), nat));
	    rtCache.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverC"), serverC));
	    rtCache.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_nat"), nat));
	
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtNat = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtNat.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_endHostA"), cache));
	    rtNat.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverB"), serverB));
	    rtNat.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverC"), cache));
	    rtNat.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_cache"), cache));
	 
	  
	    net.routingTable(nat, rtNat);
	    net.routingTable(cache, rtCache);
	    net.routingTable(endHostA, rtA);
	    net.routingTable(serverC, rtC);
	    net.routingTable(serverB, rtB);
	
	    net.attach(endHostA, serverB, serverC, cache, nat);

	    IntNum srcPort = ctx.mkInt(90);
	    endHostA.installWebClient(nctx.am.get("ip_endHostA"), nctx.am.get("ip_serverB"),srcPort);
	    
	    ArrayList<DatatypeExpr> ia = new ArrayList<DatatypeExpr>();
	    ia.add(nctx.am.get("ip_endHostA"));	    
	    cache.setInternalAddress(ia);
	    cache.installWebCache();	    
	    
	    serverB.addEntry(ctx.mkInt(1));  // By default, endHostA will request the URL to webServer
	    serverB.installWebServer();
	    
	    serverC.addEntry(ctx.mkInt(1));
	    serverC.installWebServer();
	    	  
	    
	    ArrayList<DatatypeExpr> natList = new ArrayList<DatatypeExpr>();
	    natList.add(nctx.am.get("ip_endHostA"));
	    natList.add(nctx.am.get("ip_serverC"));
	    
	    IntNum port = ctx.mkInt(99);
	    nat.setInternalAddress(natList);
	    nat.installNat(nctx.am.get("ip_nat"), port);
	    
	    checker = new Checker(ctx,nctx,net);

	}
	
	public static void main(String[] args) throws Z3Exception
    {
    	
    	IsolationResult ret;
    	
    	Rule_NatTest model = new Rule_NatTest();
    	model.resetZ3();
    	
    	ret = model.checker.checkIsolationProperty(model.endHostA, model.serverB );
    	
    	//model.printVector(ret.assertions);
    	if (ret.result == Status.UNSATISFIABLE){
     	   System.out.println("UNSAT"); // Nodes a and b are isolated
    	}else{
     		System.out.println("SAT ");
     	
     	}
    	
    	model = new Rule_NatTest();
    	model.resetZ3();
    	ret = model.checker.checkIsolationProperty(model.serverB,model.endHostA );
    	
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




