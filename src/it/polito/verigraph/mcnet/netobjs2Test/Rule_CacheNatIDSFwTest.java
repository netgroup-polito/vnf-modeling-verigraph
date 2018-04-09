package it.polito.verigraph.mcnet.netobjs2Test;

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
import it.polito.verigraph.mcnet.netobjs2.*;
/**
 * <p/>
 * Cache - Nat - Fw     test												<p/>
 * 
 *    | Webclient_A | ----| CACHE |----- | NAT | ----|IDS|-----| FW |----- | SERVER_B |		<p/>
  *    ...................|													<p/>
 *    | SERVER_C | --------													<p/>
 *
 *
 */
public class Rule_CacheNatIDSFwTest {
	
	public Checker checker;
	public Context ctx;
	public Rule_WebClient endHostA;
	public Rule_Nat nat;
	public Rule_WebServerForTest serverB,serverC;
	public Rule_Ids Ids;
	public Rule_AclFirewall Fw;
	public Rule_WebCache cache;
	
	public	Rule_CacheNatIDSFwTest(){
	
		ctx = new Context();
		NetContext nctx = new NetContext (ctx,new String[]{"endHostA", "cache", "nat","Ids","Fw","serverB","serverC"},
												new String[]{"ip_endHostA", "ip_cache", "ip_nat","ip_ids","ip_Fw","ip_serverB","ip_serverC"});
		Network net = new Network (ctx,new Object[]{nctx});
		
		endHostA = new Rule_WebClient(ctx, new Object[]{nctx.nm.get("endHostA"), net, nctx});
		serverB = new Rule_WebServerForTest(ctx, new Object[]{nctx.nm.get("serverB"), net, nctx});
		serverC = new Rule_WebServerForTest(ctx, new Object[]{nctx.nm.get("serverC"), net, nctx});
		Ids = new Rule_Ids(ctx, new Object[]{nctx.nm.get("Ids"), net, nctx});
		cache = new Rule_WebCache(ctx, new Object[]{nctx.nm.get("cache"), net, nctx});
		nat = new Rule_Nat(ctx, new Object[]{nctx.nm.get("nat"), net, nctx});
		Fw = new Rule_AclFirewall(ctx, new Object[]{nctx.nm.get("Fw"), net, nctx});
	    
		ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>>();
		ArrayList<DatatypeExpr> al1 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al2 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al3 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al4 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al5 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al6 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al7 = new ArrayList<DatatypeExpr>();
		
		al1.add(nctx.am.get("ip_endHostA"));
		al2.add(nctx.am.get("ip_cache"));
		al3.add(nctx.am.get("ip_nat"));
		al4.add(nctx.am.get("ip_ids"));
		al5.add(nctx.am.get("ip_Fw"));
		al6.add(nctx.am.get("ip_serverB"));
		al7.add(nctx.am.get("ip_serverC"));
		
	    adm.add(new Tuple<>(endHostA, al1));
	    adm.add(new Tuple<>(cache, al2));
	    adm.add(new Tuple<>(nat, al3));
	    adm.add(new Tuple<>(Ids, al4));
	    adm.add(new Tuple<>(Fw, al5));
	    adm.add(new Tuple<>(serverB,al6));
	    adm.add(new Tuple<>(serverC,al7));
	    
	    net.setAddressMappings(adm);
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtA = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtA.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverB"), cache));
	    rtA.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverC"), cache));
	    rtA.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_cache"), cache));
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtB = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtB.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_endHostA"), Fw));
	    rtB.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverC"), Fw));
	    rtB.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_cache"), Fw));
	    rtB.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_nat"), Fw));
	    rtB.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_Fw"), Fw));
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtC = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtC.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_endHostA"), cache));
	    rtC.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverB"), cache));
	    rtC.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_cache"), cache));
	    rtC.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_nat"), cache));
	    rtC.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_Fw"), cache));
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtCache = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtCache.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_endHostA"), endHostA));
	    rtCache.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverB"), nat));
	    rtCache.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverC"), serverC));
	    rtCache.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_nat"), nat));
	    rtCache.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_Fw"), nat));
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtNat = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtNat.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_endHostA"), cache));
	    rtNat.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverB"), Ids));
	    rtNat.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverC"), cache));
	    rtNat.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_cache"), cache));
	    rtNat.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_Fw"), Ids));
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtFw = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtFw.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_endHostA"), Ids));
	    rtFw.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverB"), serverB));
	    rtFw.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverC"), Ids));
	    rtFw.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_cache"), Ids));
	    rtFw.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_nat"), Ids));
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtIds = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtIds.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_endHostA"), nat));
	    rtIds.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverB"), Fw));
	    rtIds.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_serverC"), nat));
	    rtIds.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_nat"), nat));
	    rtIds.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_Fw"), Fw));
	    rtIds.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_cache"), nat));
	    
	    //Configuring routing tables of middleboxes
	    net.routingTable(Fw, rtFw);
	    net.routingTable(nat, rtNat);
	    net.routingTable(cache, rtCache);
	    net.routingTable(endHostA, rtA);
	    net.routingTable(serverC, rtC);
	    net.routingTable(serverB, rtB);
	    net.routingTable(Ids, rtIds);
	    
	    //Attaching nodes to network
	    net.attach(endHostA, serverB, serverC, cache, nat, Fw, Ids);

	    //Configuring middleboxes
	   
	    endHostA.installWebClient(nctx.am.get("ip_endHostA"), nctx.am.get("ip_serverB"), ctx.mkInt(1));
	    
	    ArrayList<DatatypeExpr> ia = new ArrayList<DatatypeExpr>();
	    ia.add(nctx.am.get("ip_endHostA"));	    
	    cache.setInternalAddress(ia);
	    cache.installWebCache();	    
	    
	    serverB.addEntry(ctx.mkInt(1));  // By default, endHostA will request the URL to webServer
	    serverB.installWebServer();
	    serverB.addConstraintForTest();
	    
	    serverC.addEntry(ctx.mkInt(1));
	    serverC.installWebServer();
	    serverC.addConstraintForTest();
	    
	    Fw.setInternalAddress(al3);    // ip_nat
	  //  Fw.addEntry(nctx.am.get("ip_nat"), nctx.am.get("ip_serverB"));
	
	    Fw.installAclFirewall();
	    
	   // Ids.addEntry(ctx.mkInt(1));
	    Ids.installIds();
	    
	    ArrayList<DatatypeExpr> natList = new ArrayList<DatatypeExpr>();
	    natList.add(nctx.am.get("ip_endHostA"));
	    natList.add(nctx.am.get("ip_cache"));
	    
	    
	    IntNum port = ctx.mkInt(99);
	    nat.setInternalAddress(natList);
	    nat.installNat(nctx.am.get("ip_nat"), port);
	    
	    checker = new Checker(ctx,nctx,net);

	}
	
	public static void main(String[] args) throws Z3Exception
    {
    	
    	IsolationResult ret;
    	
    	Rule_CacheNatIDSFwTest model = new Rule_CacheNatIDSFwTest();
    	model.resetZ3();
    	ret = model.checker.checkIsolationProperty(model.endHostA, model.serverB );
    	
    	//model.printVector(ret.assertions);
    	if (ret.result == Status.UNSATISFIABLE){
     	   System.out.println("hostA--SreverB  UNSAT"); // Nodes a and b are isolated
    	}else{
     		System.out.println("hostA--SreverB  SAT ");
     	}
    	
    	model = new Rule_CacheNatIDSFwTest();
    	model.resetZ3();
    	ret = model.checker.checkIsolationProperty(model.serverB,model.endHostA );
    	
    	//model.printVector(ret.assertions);
    	if (ret.result == Status.UNSATISFIABLE){
     	   System.out.println("SreverB--clientA UNSAT"); // Nodes a and b are isolated
    	}else{
     		System.out.println("SreverB--clientA  SAT ");
     	}
    	
    	model = new Rule_CacheNatIDSFwTest();
    	model.resetZ3();    	
    	ret = model.checker.checkIsolationProperty(model.endHostA,model.serverC );
    	
    	//model.printVector(ret.assertions);
    	if (ret.result == Status.UNSATISFIABLE){
     	   System.out.println("hostA--SreverC  UNSAT"); // Nodes a and b are isolated
    	}else{
     		System.out.println("hostA--SreverC  SAT ");
     	}
    	
    	model = new Rule_CacheNatIDSFwTest();
    	model.resetZ3();
    	ret = model.checker.checkIsolationProperty(model.serverC,model.endHostA );
    	
    	//model.printVector(ret.assertions);
    	if (ret.result == Status.UNSATISFIABLE){
     	   System.out.println("SreverC--clientA UNSAT"); // Nodes a and b are isolated
    	}else{
     		System.out.println("SreverC--clientA SAT ");
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




