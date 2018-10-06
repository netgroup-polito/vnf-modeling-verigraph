package it.polito.verigraph.usecase;

import java.util.ArrayList;
import java.util.HashMap;

import com.microsoft.z3.Context;
import com.microsoft.z3.DatatypeExpr;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

import it.polito.verigraph.mcnet.components.*;
import it.polito.verigraph.mcnet.netobjs.PolitoMailClient;
import it.polito.verigraph.mcnet.netobjs.Rule_Antispam;
import it.polito.verigraph.mcnet.netobjs.Rule_MailServer;

/**
 * <p/>
 * Network Graph node: a, VNF Antispam(as) and VNF MailServer(ms)	
 * <p/>											
 * | a | <--> | as | <--> | ms |		
 * 
 */
public class Test_MailClientAntispamMailServer {
	
	public Checker checker;
	public Context ctx;
	public Rule_Antispam as;
	public PolitoMailClient a;
	public Rule_MailServer ms;

	
	public	Test_MailClientAntispamMailServer(){
		
		ctx = new Context();
		
			NetContext nctx = new NetContext (ctx,
					new String[]{"a", "as", "ms"},
					new String[]{"ip_client", "ip_as", "ip_ms"});  //create the lists of nodes and addresses
			
			Network net = new Network (ctx,new Object[]{nctx}); //create the network
			
			//create the nodes and add them to the network
			a = new PolitoMailClient(ctx, new Object[]{nctx.nm.get("a"), net, nctx, nctx.am.get("ip_ms")}); 
			as = new Rule_Antispam(ctx, new Object[]{nctx.nm.get("as"), net, nctx});
			ms = new Rule_MailServer(ctx, new Object[]{nctx.nm.get("ms"), net, nctx});
			
			ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>>();
			ArrayList<DatatypeExpr> al1 = new ArrayList<DatatypeExpr>();
  			ArrayList<DatatypeExpr> al2 = new ArrayList<DatatypeExpr>();
  			ArrayList<DatatypeExpr> al3 = new ArrayList<DatatypeExpr>();
  			
  			al1.add(nctx.am.get("ip_client"));
  			al2.add(nctx.am.get("ip_as"));
  			al3.add(nctx.am.get("ip_ms"));
  			
			adm.add(new Tuple<>(a, al1));
		    adm.add(new Tuple<>(as, al2));
		    adm.add(new Tuple<>(ms, al3));
		    net.setAddressMappings(adm);
		
			ArrayList<Tuple<DatatypeExpr,NetworkObject>> rta = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    	rta.add(new Tuple<>(nctx.am.get("ip_ms"), as));  	
	    	net.routingTable(a, rta);
	    
	    	ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtas = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    	rtas.add(new Tuple<>(nctx.am.get("ip_ms"), ms));
	    	rtas.add(new Tuple<>(nctx.am.get("ip_client"), a));    	
	    	net.routingTable(as, rtas);
	    
	    	ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtms = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    	rtms.add(new Tuple<>(nctx.am.get("ip_client"), as));
	    	net.routingTable(ms, rtms);
    
		    net.attach(a, as, ms); //add the nodes to the network
		    
		    //Configuration-Installation:   
		    as.addEntry( ctx.mkInt(3) );
		    as.addEntry( ctx.mkInt(33) );
		    as.addEntry( ctx.mkInt(333) );
		    as.installAntispam();
		  
		    ms.installMailServer(ctx.mkInt(1)); //1=RESPONSE
		    
		   
		    checker = new Checker(ctx,nctx,net);
	}
	
	public static void main(String[] args) throws Z3Exception
    {
    	
    	IsolationResult ret;
    	
    	Test_MailClientAntispamMailServer model = new Test_MailClientAntispamMailServer();
    	model.resetZ3();
    	
    	ret = model.checker.checkIsolationProperty(model.a,model.as);
    	
    	//model.printVector(ret.assertions);
    	if (ret.result == Status.UNSATISFIABLE){
     	   System.out.println("UNSAT"); // Nodes a and b are isolated
    	}else{
     		System.out.println("SAT ");
     		System.out.print( "Model -> "); 
     		System.out.println(ret.model);
     	}
    }

	public void resetZ3() throws Z3Exception{
	    HashMap<String, String> cfg = new HashMap<String, String>();
	    cfg.put("model", "true");
	     ctx = new Context(cfg);
	}

}


	

