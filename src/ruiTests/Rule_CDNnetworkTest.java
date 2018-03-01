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
import ruiNFs.Rule_GlobalDnsBalancer;
import ruiNFs.Rule_LocalDnsServer;
import ruiNFs.Rule_WebClient;
import ruiNFs.Rule_WebServer;
import ruiNFs.Rule_CDNcache;

/**
 * EndHost - DNSserver - GlobalBalancer - cdnCache  - PolitoWebServer   test												<p/>
 * 
 *    | HOST_A | ---| DNSserver |--- | GlobalBalancer|---| cdnCache_1 | ----| PolitoWebServer |	<p/>
 *    										|---------------------------------------|
 *    	   									|------------| cdnCache_2 |-------------	|	<p/>
 */
public class Rule_CDNnetworkTest {
	
	public Checker checker;
	public Context ctx;
	public Rule_WebClient hostA;
	public Rule_LocalDnsServer dnsServer;
	public Rule_GlobalDnsBalancer balancer;
	public Rule_CDNcache cdnCache_1,cdnCache_2;
	public Rule_WebServer webServer;
	
	public	Rule_CDNnetworkTest(){
		ctx = new Context();
		
		NetContext nctx = new NetContext (ctx,new String[]{"hostA", "dnsServer", "cdnCache_1", "cdnCache_2", "webServer","balancer"},
				new String[]{"ip_hostA", "ip_DNSserver", "ip_cdnCache_1", "ip_cdnCache_2","ip_webServer","ip_balancer"});
		Network net = new Network (ctx,new Object[]{nctx});

		hostA = new Rule_WebClient(ctx, new Object[]{nctx.nm.get("hostA"), net, nctx});
		dnsServer = new Rule_LocalDnsServer(ctx, new Object[]{nctx.nm.get("dnsServer"), net, nctx});
		balancer = new Rule_GlobalDnsBalancer(ctx, new Object[]{nctx.nm.get("balancer"), net, nctx});
		cdnCache_1 = new Rule_CDNcache(ctx, new Object[]{nctx.nm.get("cdnCache_1"), net, nctx});
		cdnCache_2 = new Rule_CDNcache(ctx, new Object[]{nctx.nm.get("cdnCache_2"), net, nctx});
		webServer = new Rule_WebServer(ctx, new Object[]{nctx.nm.get("webServer"), net, nctx});
		
		ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>>();
		ArrayList<DatatypeExpr> al1 = new ArrayList<DatatypeExpr>();
			ArrayList<DatatypeExpr> al2 = new ArrayList<DatatypeExpr>();
			ArrayList<DatatypeExpr> al3 = new ArrayList<DatatypeExpr>();
			ArrayList<DatatypeExpr> al4 = new ArrayList<DatatypeExpr>();
			ArrayList<DatatypeExpr> al5 = new ArrayList<DatatypeExpr>();
			ArrayList<DatatypeExpr> al6 = new ArrayList<DatatypeExpr>();
			al1.add(nctx.am.get("ip_hostA"));
			al2.add(nctx.am.get("ip_DNSserver"));
			al3.add(nctx.am.get("ip_cdnCache_1"));
			al4.add(nctx.am.get("ip_cdnCache_2"));
			al5.add(nctx.am.get("ip_webServer"));
			al6.add(nctx.am.get("ip_balancer"));
		adm.add(new Tuple<>(hostA, al1));
	    adm.add(new Tuple<>(dnsServer, al2));
	    adm.add(new Tuple<>(cdnCache_1, al3));
	    adm.add(new Tuple<>(cdnCache_2, al4));
	    adm.add(new Tuple<>(webServer, al5));
	    adm.add(new Tuple<>(balancer, al6));

	    net.setAddressMappings(adm);
	    
	    ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtHostA = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    rtHostA.add(new Tuple<>(nctx.am.get("ip_DNSserver"), dnsServer));
	    rtHostA.add(new Tuple<>(nctx.am.get("ip_balancer"), dnsServer));
	    rtHostA.add(new Tuple<>(nctx.am.get("ip_cdnCache_1"), dnsServer));
	    rtHostA.add(new Tuple<>(nctx.am.get("ip_cdnCache_2"), dnsServer));
	    rtHostA.add(new Tuple<>(nctx.am.get("ip_webServer"), dnsServer));
    
    	ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtDnsServer = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
    	rtDnsServer.add(new Tuple<>(nctx.am.get("ip_hostA"), hostA));
    	rtDnsServer.add(new Tuple<>(nctx.am.get("ip_balancer"), balancer));
    	rtDnsServer.add(new Tuple<>(nctx.am.get("ip_cdnCache_1"), balancer));
    	rtDnsServer.add(new Tuple<>(nctx.am.get("ip_cdnCache_2"), balancer));
    	rtDnsServer.add(new Tuple<>(nctx.am.get("ip_webServer"), balancer));
    
    	ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtBalancer = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
    	rtBalancer.add(new Tuple<>(nctx.am.get("ip_DNSserver"), dnsServer));
    	rtBalancer.add(new Tuple<>(nctx.am.get("ip_cdnCache_1"), cdnCache_1));
    	rtBalancer.add(new Tuple<>(nctx.am.get("ip_cdnCache_2"), cdnCache_2));
    	rtBalancer.add(new Tuple<>(nctx.am.get("ip_hostA"), dnsServer));
    	rtBalancer.add(new Tuple<>(nctx.am.get("ip_webServer"), cdnCache_1));  // assume
    	
    	
    	ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtCdnCache_1 = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
    	rtCdnCache_1.add(new Tuple<>(nctx.am.get("ip_hostA"), balancer));
    	rtCdnCache_1.add(new Tuple<>(nctx.am.get("ip_webServer"), webServer));
    	rtCdnCache_1.add(new Tuple<>(nctx.am.get("ip_balancer"), balancer));
    	rtCdnCache_1.add(new Tuple<>(nctx.am.get("ip_DNSserver"), balancer));
    	
    	ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtCdnCache_2 = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
    	rtCdnCache_2.add(new Tuple<>(nctx.am.get("ip_hostA"), balancer));
    	rtCdnCache_2.add(new Tuple<>(nctx.am.get("ip_webServer"), webServer));
    	rtCdnCache_2.add(new Tuple<>(nctx.am.get("ip_balancer"), balancer));
    	rtCdnCache_2.add(new Tuple<>(nctx.am.get("ip_DNSserver"), balancer));
    
    	ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtWebServer = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
    	rtWebServer.add(new Tuple<>(nctx.am.get("ip_cdnCache_1"), cdnCache_1));
    	rtWebServer.add(new Tuple<>(nctx.am.get("ip_cdnCache_2"), cdnCache_2));
    	rtWebServer.add(new Tuple<>(nctx.am.get("ip_hostA"), cdnCache_1));   //assume
    	rtWebServer.add(new Tuple<>(nctx.am.get("ip_DNSserver"), cdnCache_1));	//assume
    	rtWebServer.add(new Tuple<>(nctx.am.get("ip_balancer"), cdnCache_1));	//assume
    	
    	
    	net.routingTable(hostA, rtHostA);
	    net.routingTable(dnsServer, rtDnsServer);
	    net.routingTable(balancer, rtBalancer);
	    net.routingTable(cdnCache_1, rtCdnCache_1);
	    net.routingTable(cdnCache_2, rtCdnCache_2);
	    net.routingTable(webServer, rtWebServer);
	    
	    net.attach(hostA, dnsServer, balancer, cdnCache_1, cdnCache_2, webServer); //aggiunge i nodi alla rete
	
	    ArrayList<DatatypeExpr> ia = new ArrayList<DatatypeExpr>();
	    ia.add(nctx.am.get("ip_hostA"));
	    
	    hostA.installWebClient(nctx.am.get("ip_hostA"), nctx.am.get("ip_webServer"), ctx.mkInt(1));
	    
	    dnsServer.setInternalAddress(ia);
	    dnsServer.installLocalDnsServer(nctx.am.get("ip_balancer"));
	   
	    balancer.addEntry(nctx.am.get("ip_hostA"), ctx.mkInt(1), nctx.am.get("ip_cdnCache_1"));	
	    balancer.addEntry(nctx.am.get("ip_hostA"), ctx.mkInt(2), nctx.am.get("ip_cdnCache_2"));	
	    balancer.installGlobalDnsBalancer();;
	    
	    
	    
	    cdnCache_1.setInternalAddress(ia);
	    cdnCache_2.setInternalAddress(ia);
	    
	    cdnCache_1.installCDNcache();
	    cdnCache_2.installCDNcache();
	    
	    webServer.addEntry(ctx.mkInt(1));
	    webServer.installWebServer();
	    
	    checker = new Checker(ctx,nctx,net);
	}
	
	
	public static void main(String[] args) throws Z3Exception
    {
    	
    	IsolationResult ret;
    	
    	Rule_CDNnetworkTest model = new Rule_CDNnetworkTest();
    	model.resetZ3();
    	ret = model.checker.checkIsolationProperty(model.hostA, model.webServer );
    	
    	if (ret.result == Status.UNSATISFIABLE){
     	   System.out.println("hostA--SreverB  UNSAT");
    	}else{
     		System.out.println("hostA--SreverB  SAT ");
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




