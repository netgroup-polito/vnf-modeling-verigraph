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
import ruiNFs.*;
/**
 * Client - Router - Fw     test												<p/>
 * 
 *    | WebClient | ----- | vRouter | ----| FW |----- | WebServer_A |		<p/>
 *    ...................|													<p/>
 *    | WebServer_B | --------													<p/>
 *
 */
public class Rule_RouterTest{

	public Checker checker;
	public Context ctx;
	public PolitoWebClient WebClient;
	public PolitoWebServer WebServer_A, WebServer_B;
	public Rule_AclFirewall PolitoFw;
	public Rule_Router Router;
	
	public Rule_RouterTest(){
		
		ctx = new Context();
		NetContext nctx = new NetContext (ctx,new String[]{"WebClient", "WebServer_A", "WebServer_B","PolitoFw","Router"},
				new String[]{"ip_WebClient", "ip_WebServer_A", "ip_WebServer_B","ip_PolitoFw","ip_Router"});
		Network net = new Network (ctx,new Object[]{nctx});
		
		WebClient = new PolitoWebClient(ctx, new Object[]{nctx.nm.get("WebClient"), net, nctx,nctx.am.get("ip_WebServer_A")});
		WebServer_A = new PolitoWebServer(ctx, new Object[]{nctx.nm.get("WebServer_A"), net, nctx});
		WebServer_B = new PolitoWebServer(ctx, new Object[]{nctx.nm.get("WebServer_B"), net, nctx});
		Router = new Rule_Router(ctx, new Object[]{nctx.nm.get("Router"), net, nctx});
		PolitoFw = new Rule_AclFirewall(ctx, new Object[]{nctx.nm.get("PolitoFw"), net, nctx});
		
		ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>>();
		ArrayList<DatatypeExpr> al1 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al2 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al3 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al4 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al5 = new ArrayList<DatatypeExpr>();
		
		al1.add(nctx.am.get("ip_WebClient"));
		al2.add(nctx.am.get("ip_WebServer_A"));
		al3.add(nctx.am.get("ip_WebServer_B"));
		al4.add(nctx.am.get("ip_PolitoFw"));
		al5.add(nctx.am.get("ip_Router"));
		
	    adm.add(new Tuple<>(WebClient, al1));
	    adm.add(new Tuple<>(WebServer_A, al2));
	    adm.add(new Tuple<>(WebServer_B, al3));
	    adm.add(new Tuple<>(PolitoFw, al4));
	    adm.add(new Tuple<>(Router, al5));
	    
	    net.setAddressMappings(adm);
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtClient = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtClient.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_Router"), Router));
	    rtClient.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_PolitoFw"), Router));
	    rtClient.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_WebServer_A"), Router));
	    rtClient.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_WebServer_B"), Router));
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtServerA = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtServerA.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_Router"), PolitoFw));
	    rtServerA.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_PolitoFw"), PolitoFw));
	    rtServerA.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_WebClient"), PolitoFw));
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtServerB = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtServerB.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_Router"), Router));
	    rtServerB.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_WebClient"), Router));
	    rtServerB.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_PolitoFw"), Router));
	    
	    

	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtRouter = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtRouter.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_WebServer_A"), PolitoFw));
	    rtRouter.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_WebServer_B"), WebServer_B));
	    rtRouter.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_WebClient"), WebClient));
	    rtRouter.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_PolitoFw"), PolitoFw));
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtFW = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtFW.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_WebServer_A"), WebServer_A));
	    rtFW.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_Router"), Router));
	    rtFW.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_WebClient"), Router));
	    rtFW.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_WebServer_B"), Router));
	    
	    net.routingTable(WebClient, rtClient);
	    net.routingTable(WebServer_A, rtServerA);
	    net.routingTable(WebServer_B, rtServerB);
	    net.routingTable(Router, rtRouter);
	    net.routingTable(PolitoFw, rtFW);
	    
	    
	  
	    Router.addEntry(nctx.am.get("ip_WebServer_A"));
	    Router.installRouter();
	    PolitoFw.setInternalAddress(al1);
	//    PolitoFw.addEntry(nctx.am.get("ip_WebClient"),nctx.am.get("ip_WebServer_A"));;
	    PolitoFw.installAclFirewall();
	    //Attaching nodes to network
	    net.attach(WebClient, WebServer_A, WebServer_B, Router, PolitoFw);
	    
	    
	    checker = new Checker(ctx,nctx,net);
	}
	
	public static void main(String[] args) throws Z3Exception
    {
    	IsolationResult ret;
    	Rule_RouterTest model = new Rule_RouterTest();
    	
    	model.resetZ3();
    	
    	//IsolationResult ret
    	ret = model.checker.checkIsolationProperty(model.WebClient,model.WebServer_A );
    	
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



