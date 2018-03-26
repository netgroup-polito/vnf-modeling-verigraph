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
import it.polito.verigraph.mcnet.netobjs.PolitoMailClient;
import ruiNFs.*;

/**
 * <p/>
 * Antispam test													<p/>
 *| CLIENT | --------- | ANTISPAM | --------- | MAIL SERVER |		<p/>
																	<p/>
 */
public class Rule_MailclientAntispamMailserverTest {
	
	public Checker checker;
	public Context ctx;
	public Rule_Antispam antispam;
	public PolitoMailClient politoMailClient;
	public Rule_MailServer mailServer;

	
	public	Rule_MailclientAntispamMailserverTest(){
		
		ctx = new Context();
		
			NetContext nctx = new NetContext (ctx,
					new String[]{"politoMailClient", "antispam", "mailServer"},
					new String[]{"ip_client", "ip_antispam", "ip_mailServer"});  //create the lists of nodes and addresses
			
			Network net = new Network (ctx,new Object[]{nctx}); //create the network
			
			//create the nodes and add them to the network
			politoMailClient = new PolitoMailClient(ctx, new Object[]{nctx.nm.get("politoMailClient"), net, nctx, nctx.am.get("ip_mailServer")}); 
			antispam = new Rule_Antispam(ctx, new Object[]{nctx.nm.get("antispam"), net, nctx});
			mailServer = new Rule_MailServer(ctx, new Object[]{nctx.nm.get("mailServer"), net, nctx});
			
			//create the couples node-address
			ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>>();
			ArrayList<DatatypeExpr> al1 = new ArrayList<DatatypeExpr>();
  			ArrayList<DatatypeExpr> al2 = new ArrayList<DatatypeExpr>();
  			ArrayList<DatatypeExpr> al3 = new ArrayList<DatatypeExpr>();
  			al1.add(nctx.am.get("ip_client"));
  			al2.add(nctx.am.get("ip_antispam"));
  			al3.add(nctx.am.get("ip_mailServer"));
			adm.add(new Tuple<>(politoMailClient, al1));
		    adm.add(new Tuple<>(antispam, al2));
		    adm.add(new Tuple<>(mailServer, al3));

		    net.setAddressMappings(adm); //link nodes and addresses
		
		    //create routing tables
		    /**/
			ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtClient = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    	rtClient.add(new Tuple<>(nctx.am.get("ip_mailServer"), antispam));
	    
	    	ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtAnti = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    	rtAnti.add(new Tuple<>(nctx.am.get("ip_mailServer"), mailServer));
	    	rtAnti.add(new Tuple<>(nctx.am.get("ip_client"), politoMailClient));
	    
	    	ArrayList<Tuple<DatatypeExpr,NetworkObject>> rtServ = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
	    	rtServ.add(new Tuple<>(nctx.am.get("ip_client"), antispam));
	    	/**/

	    	//set the routing tables
	    	net.routingTable(politoMailClient, rtClient);
		    net.routingTable(antispam, rtAnti);
		    net.routingTable(mailServer, rtServ);
		    
		    net.attach(politoMailClient, antispam, mailServer); //add the nodes to the network
		    
		    		
		    antispam.addEntry( ctx.mkInt(1) );
		    antispam.installAntispam();
		  
		    mailServer.installMailServer(ctx.mkInt(1));
		    
		   
		    checker = new Checker(ctx,nctx,net);
	}
	
	public static void main(String[] args) throws Z3Exception
    {
    	
    	IsolationResult ret;
    	
    	Rule_MailclientAntispamMailserverTest model = new Rule_MailclientAntispamMailserverTest();
    	model.resetZ3();
    	
    	//IsolationResult ret
    	ret = model.checker.checkIsolationProperty(model.mailServer,model.politoMailClient );
    	
    	//model.printVector(ret.assertions);
    	if (ret.result == Status.UNSATISFIABLE){
     	   System.out.println("UNSAT"); // Nodes a and b are isolated
    	}else{
     		System.out.println("SAT ");
     		
     	/*	System.out.print( "Model -> "); p.printModel(ret.model);
    	    System.out.println( "Violating packet -> " +ret.violating_packet);
    	    System.out.println("Last hop -> " +ret.last_hop);
    	    System.out.println("Last send_time -> " +ret.last_send_time);
    	    System.out.println( "Last recv_time -> " +ret.last_recv_time);
     	*/
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


	

