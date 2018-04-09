
package it.polito.verigraph.mcnet.netobjs2Test;

import java.util.ArrayList;
import java.util.HashMap;

import com.microsoft.z3.Context;
import com.microsoft.z3.DatatypeExpr;
import com.microsoft.z3.EnumSort;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Model;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

import it.polito.verigraph.mcnet.components.*;
import it.polito.verigraph.mcnet.netobjs2.Rule_WebCache;
import it.polito.verigraph.mcnet.netobjs2.Rule_WebClient;
import it.polito.verigraph.mcnet.netobjs2.Rule_WebServer;

/**
 * EndHost  - WebCache  - WebServer   test												<p/>
 * 
 *    | HOST_A | ------| cdnCache_1 | ----| WebServer |	<p/>

 */
public class Rule_WebclientCacheServerTest {
	
	public Checker checker;
	public Context ctx;
	public Rule_WebClient hostA;
	public Rule_WebCache cache;
	public Rule_WebServer webServer;
	public IntNum port;
	
	public	Rule_WebclientCacheServerTest(){
		ctx = new Context();
		port = ctx.mkInt(99);
		
		NetContext nctx = new NetContext (ctx,new String[]{"hostA", "cache", "webServer"},
				new String[]{"ip_hostA", "ip_cache","ip_webServer"});
		Network net = new Network (ctx,new Object[]{nctx});

		hostA = new Rule_WebClient(ctx, new Object[]{nctx.nm.get("hostA"), net, nctx});
		cache = new Rule_WebCache(ctx, new Object[]{nctx.nm.get("cache"), net, nctx});
		
		webServer = new Rule_WebServer(ctx, new Object[]{nctx.nm.get("webServer"), net, nctx});
	
		ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>>();
			ArrayList<DatatypeExpr> al1 = new ArrayList<DatatypeExpr>();			
			ArrayList<DatatypeExpr> al2 = new ArrayList<DatatypeExpr>();
			ArrayList<DatatypeExpr> al3 = new ArrayList<DatatypeExpr>();
			
			
			al1.add(nctx.am.get("ip_hostA"));
			al2.add(nctx.am.get("ip_cache"));
			al3.add(nctx.am.get("ip_webServer"));
			
			
		adm.add(new Tuple<>(hostA, al1));
	    adm.add(new Tuple<>(cache, al2));
	    adm.add(new Tuple<>(webServer, al3));

	    net.setAddressMappings(adm);
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtHostA = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtHostA.add(new Tuple<>(nctx.am.get("ip_cache"), cache));
	    rtHostA.add(new Tuple<>(nctx.am.get("ip_webServer"), cache));
  
    	ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtCache = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
    	rtCache.add(new Tuple<>(nctx.am.get("ip_hostA"), hostA));
    	rtCache.add(new Tuple<>(nctx.am.get("ip_webServer"), webServer));

    	ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtWebServer = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
    	rtWebServer.add(new Tuple<>(nctx.am.get("ip_cache"), cache));
    	rtWebServer.add(new Tuple<>(nctx.am.get("ip_hostA"), cache));   //assume

    	net.routingTable(hostA, rtHostA);
	    net.routingTable(cache, rtCache);
	    net.routingTable(webServer, rtWebServer);
	    
	    net.attach(hostA, cache, webServer); //aggiunge i nodi alla rete
	
	    hostA.installWebClient(nctx.am.get("ip_hostA"),nctx.am.get("ip_webServer"),ctx.mkInt(1));
 
	    ArrayList<DatatypeExpr> ia = new ArrayList<DatatypeExpr>();
	    ia.add(nctx.am.get("ip_hostA"));
	    
	    cache.setInternalAddress(ia);
	    
	    
	    cache.installWebCache();
	    
	    
	    webServer.addEntry(ctx.mkInt(1));
	    webServer.installWebServer();
	    
	    checker = new Checker(ctx,nctx,net);
	}
	
	public static void main(String[] args) throws Z3Exception
    {
    	
    	IsolationResult ret;
    	
    	Rule_WebclientCacheServerTest model = new Rule_WebclientCacheServerTest();
    	model.resetZ3();
    	
    	//IsolationResult ret
    	ret = model.checker.checkIsolationProperty(model.hostA,model.webServer );
    	
    	//model.printVector(ret.assertions);
    	if (ret.result == Status.UNSATISFIABLE){
     	   System.out.println("UNSAT"); // Nodes a and b are isolated
    	}else{
     		System.out.println("SAT ");
     	
     	}
    	
    	model.resetZ3();
    	ret = model.checker.checkIsolationProperty(model.webServer,model.hostA );   	
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


	

