package ruiTests;

import java.util.ArrayList;
import java.util.HashMap;

import com.microsoft.z3.Context;
import com.microsoft.z3.DatatypeExpr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Model;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

import it.polito.verigraph.mcnet.components.Checker;
import it.polito.verigraph.mcnet.components.IsolationResult;
import it.polito.verigraph.mcnet.components.NetContext;
import it.polito.verigraph.mcnet.components.Network;
import it.polito.verigraph.mcnet.components.NetworkObject;
import it.polito.verigraph.mcnet.components.Tuple;
import it.polito.verigraph.mcnet.netobjs.PolitoEndHost;
import ruiNFs.Rule_GlobalDnsBalancer;
import ruiNFs.Rule_SipServer;
import ruiNFs.Rule_UAForTest;
import it.polito.verigraph.mcnet.netobjs.PacketModel;

/**
 * <p/>
 * 					|DNSserver|
 * 						||
 * VPN test				||												<p/>
 *| a 1| --------- | sipServerA 3|	---------- | sipServerB 4|	--------- | b 2|	<p/>
						|												<p/>
						|
					|	c 5	|
 */


public class Test_SIP2 {

    public Checker check;
    public Context ctx;
    public Rule_UAForTest a, b, c;
    public Rule_SipServer sipA, sipB;
    public Rule_GlobalDnsBalancer dns;
    

    public  Test_SIP2(){
        ctx = new Context();

        NetContext nctx = new NetContext (ctx,new String[]{"a", "b", "c", "sipA", "sipB", "dns"},
                                                new String[]{"ip_a", "ip_b", "ip_c", "ip_sipA", "ip_sipB", "ip_dns"});
        Network net = new Network (ctx,new Object[]{nctx});

        a = new Rule_UAForTest(ctx, new Object[]{nctx.nm.get("a"), net, nctx});
        b = new Rule_UAForTest(ctx, new Object[]{nctx.nm.get("b"), net, nctx});
        c = new Rule_UAForTest(ctx, new Object[]{nctx.nm.get("c"), net, nctx});
        sipA = new Rule_SipServer(ctx, new Object[]{nctx.nm.get("sipA"), net, nctx});
        sipB = new Rule_SipServer(ctx, new Object[]{nctx.nm.get("sipB"), net, nctx});
        dns = new Rule_GlobalDnsBalancer(ctx, new Object[]{nctx.nm.get("dns"), net, nctx});

        ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>>();
        ArrayList<DatatypeExpr> al1 = new ArrayList<DatatypeExpr>();
        ArrayList<DatatypeExpr> al2 = new ArrayList<DatatypeExpr>();
        ArrayList<DatatypeExpr> al3 = new ArrayList<DatatypeExpr>();
        ArrayList<DatatypeExpr> al4 = new ArrayList<DatatypeExpr>();
        ArrayList<DatatypeExpr> al5 = new ArrayList<DatatypeExpr>();
        ArrayList<DatatypeExpr> al6 = new ArrayList<DatatypeExpr>();
     
        al1.add(nctx.am.get("ip_a"));
        al2.add(nctx.am.get("ip_b"));
        al3.add(nctx.am.get("ip_sipA"));
        al4.add(nctx.am.get("ip_sipB"));
        al5.add(nctx.am.get("ip_c"));
        al6.add(nctx.am.get("ip_dns"));
     
        adm.add(new Tuple<>(a, al1));
        adm.add(new Tuple<>(b, al2));
        adm.add(new Tuple<>(sipA, al3));
        adm.add(new Tuple<>(sipB, al4));
        adm.add(new Tuple<>(c, al5));
        adm.add(new Tuple<>(dns, al6));
       
        net.setAddressMappings(adm);

        ArrayList<Tuple<DatatypeExpr,NetworkObject>> rt1 = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
        rt1.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_sipA"), sipA));
        rt1.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_b"), sipA));
        rt1.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_c"), sipA));
       
        net.routingTable(a, rt1);

        ArrayList<Tuple<DatatypeExpr,NetworkObject>> rt2 = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
        rt2.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_a"), sipB));
        rt2.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_c"), sipB));
        rt2.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_sipB"), sipB));
      
        net.routingTable(b, rt2);
        
        ArrayList<Tuple<DatatypeExpr,NetworkObject>> rt3 = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
        rt3.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_a"), sipA));
        rt3.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_b"), sipA));
        rt3.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_sipA"), sipA));
      
        net.routingTable(c, rt3);

        ArrayList<Tuple<DatatypeExpr,NetworkObject>> rt4 = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
        rt4.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_a"),a));
        rt4.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_b"), sipB));
        rt4.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_c"),c));
        rt4.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_sipB"), sipB));
        rt4.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_dns"), dns));
   
        net.routingTable(sipA, rt4);
        
        ArrayList<Tuple<DatatypeExpr,NetworkObject>> rt5 = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
        rt5.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_a"),sipA));
        rt5.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_b"), b));
        rt5.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_c"),sipA));
        rt5.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_sipA"), sipA));
       
        net.routingTable(sipB, rt5);
        
        ArrayList<Tuple<DatatypeExpr,NetworkObject>> rt6 = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
        rt6.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_sipA"),sipA));
        
        net.routingTable(dns, rt6);
        
        net.attach(a, b, c, sipA, sipB, dns);

        //Configuring middleboxes
        a.installUA(nctx.am.get("ip_a"), nctx.am.get("ip_sipA"), ctx.mkInt(1), ctx.mkInt(4), ctx.mkInt(2));	  //a-->b
        b.installUA(nctx.am.get("ip_b"), nctx.am.get("ip_sipB"), ctx.mkInt(2), ctx.mkInt(3), ctx.mkInt(1));	  //b-->a
        c.installUA(nctx.am.get("ip_c"), nctx.am.get("ip_sipA"), ctx.mkInt(5), ctx.mkInt(3), ctx.mkInt(1));  //c-->a
       
	    sipA.installSipServer(nctx.am.get("ip_sipA"), ctx.mkInt(3), nctx.am.get("ip_dns"));
	    sipB.installSipServer(nctx.am.get("ip_sipB"), ctx.mkInt(4), nctx.am.get("ip_dns"));
	    dns.addEntry(nctx.am.get("ip_a"), ctx.mkInt(4));
	    dns.installGlobalDnsBalancer(nctx.am.get("ip_sipB"));
	 
        check = new Checker(ctx,nctx,net);
}
    
    public static void main(String[] args) throws Z3Exception
    {
        Test_SIP2 model = new Test_SIP2();
        model.resetZ3();
        
        IsolationResult ret =model.check.checkIsolationProperty(model.a,model.b);
   //   model.printVector(ret.assertions);
      System.out.println("==============end assertions===========");
    	if (ret.result == Status.UNSATISFIABLE){
     	   System.out.println("a-->b  UNSAT"); // Nodes a and b are isolated
    	}else{
     		System.out.println("a-->b  SAT ");
     		System.out.println(ret.model);
     		
     	}
    	model.resetZ3();
    	ret =model.check.checkIsolationProperty(model.c,model.a);
    	   //   model.printVector(ret.assertions);
    	      System.out.println("==============end assertions===========");
    	    	if (ret.result == Status.UNSATISFIABLE){
    	     	   System.out.println("c-->a  UNSAT"); // Nodes a and b are isolated
    	    	}else{
    	     		System.out.println("c-->a  SAT ");
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