
package ruiTests;

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
import it.polito.verigraph.mcnet.netobjs.PolitoWebClient;
import ruiNFs.*;


/**
 * PolitoClient  - Ids  - WebServerForTest   test												<p/>
 * 
 *    | PolitoClient | ------| Ids | ----| WebServerForTest |	<p/>

 */
public class Test {
	
	public Checker checker;
	public Context ctx;
	//public PolitoWebClient client;
	public Rule_WebClient client;
	public Rule_Ids ids;
	public Rule_WebServerForTest webServer;
	
	public	Test(){
		ctx = new Context();
		
		NetContext nctx = new NetContext (ctx,new String[]{"client", "ids", "webServer"},
				new String[]{"ip_client", "ip_ids","ip_webServer"});
		Network net = new Network (ctx,new Object[]{nctx});

	//	client = new PolitoWebClient(ctx, new Object[]{nctx.nm.get("client"), net, nctx, nctx.am.get("ip_webServer")});
		client = new Rule_WebClient(ctx, new Object[]{nctx.nm.get("client"), net, nctx});
		ids = new Rule_Ids(ctx, new Object[]{nctx.nm.get("ids"), net, nctx});
		
		webServer = new Rule_WebServerForTest(ctx, new Object[]{nctx.nm.get("webServer"), net, nctx});
	
		ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>>();
			ArrayList<DatatypeExpr> al1 = new ArrayList<DatatypeExpr>();			
			ArrayList<DatatypeExpr> al2 = new ArrayList<DatatypeExpr>();
			ArrayList<DatatypeExpr> al3 = new ArrayList<DatatypeExpr>();
			
			
			al1.add(nctx.am.get("ip_client"));
			al2.add(nctx.am.get("ip_ids"));
			al3.add(nctx.am.get("ip_webServer"));
			
			
		adm.add(new Tuple<>(client, al1));
	    adm.add(new Tuple<>(ids, al2));
	    adm.add(new Tuple<>(webServer, al3));

	    net.setAddressMappings(adm);
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtClient = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	   // rtClient.add(new Tuple<>(nctx.am.get("ip_ids"), ids));
	    rtClient.add(new Tuple<>(nctx.am.get("ip_webServer"), webServer));
  
//    	ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtIds = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
//    	rtIds.add(new Tuple<>(nctx.am.get("ip_client"), client));
//    	rtIds.add(new Tuple<>(nctx.am.get("ip_webServer"), webServer));

    	ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtWebServer = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
   // 	rtWebServer.add(new Tuple<>(nctx.am.get("ip_ids"), ids));
    	rtWebServer.add(new Tuple<>(nctx.am.get("ip_client"), client));   //assume

    	net.routingTable(client, rtClient);
	 //   net.routingTable(ids, rtIds);
	    net.routingTable(webServer, rtWebServer);
	    
	    net.attach(client,  webServer); //aggiunge i nodi alla rete
	
	//    ids.installIds();
	 //   ids.addEntry(ctx.mkInt(1));
	    
	    client.installWebClient( nctx.am.get("ip_client"),  nctx.am.get("ip_webServer"), ctx.mkInt(1));
	   
	    webServer.installWebServer();
	    webServer.addConstraintForTest();
	    
	    checker = new Checker(ctx,nctx,net);
	}
	
	public static void main(String[] args) throws Z3Exception
    {
    	
    	IsolationResult ret;
    	
    	Test model = new Test();
    	model.resetZ3();
    	
    	//IsolationResult ret
    	ret = model.checker.checkIsolationProperty(model.client,model.webServer );
    	
    	//model.printVector(ret.assertions);
    	if (ret.result == Status.UNSATISFIABLE){
     	   System.out.println("UNSAT"); // Nodes a and b are isolated
    	}else{
     		System.out.println("SAT ");
     	
     	}
    	
    	model.resetZ3();
    	ret = model.checker.checkIsolationProperty(model.webServer,model.client );   	
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


	

