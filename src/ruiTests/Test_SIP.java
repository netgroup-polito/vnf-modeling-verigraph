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
import ruiNFs.Rule_SipServer;
import it.polito.verigraph.mcnet.netobjs.PacketModel;

/**
 * <p/>
 * VPN test													<p/>
 *| a | --------- | sipServer|	--------- | b |	<p/>
																	<p/>
 */


public class Test_SIP {

    public Checker check;
    public Context ctx;
    public PolitoEndHost a;
    public PolitoEndHost b;
    public Rule_SipServer sip;
    

    public  Test_SIP(){
        ctx = new Context();

        NetContext nctx = new NetContext (ctx,new String[]{"a", "b", "sip"},
                                                new String[]{"ip_a", "ip_b", "ip_sip"});
        Network net = new Network (ctx,new Object[]{nctx});

        a = new PolitoEndHost(ctx, new Object[]{nctx.nm.get("a"), net, nctx});
        b = new PolitoEndHost(ctx, new Object[]{nctx.nm.get("b"), net, nctx});
        sip = new Rule_SipServer(ctx, new Object[]{nctx.nm.get("sip"), net, nctx});
    

        ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject,ArrayList<DatatypeExpr>>>();
        ArrayList<DatatypeExpr> al1 = new ArrayList<DatatypeExpr>();
        ArrayList<DatatypeExpr> al2 = new ArrayList<DatatypeExpr>();
        ArrayList<DatatypeExpr> al3 = new ArrayList<DatatypeExpr>();
     
        al1.add(nctx.am.get("ip_a"));
        al2.add(nctx.am.get("ip_b"));
        al3.add(nctx.am.get("ip_sip"));
     
        adm.add(new Tuple<>(a, al1));
        adm.add(new Tuple<>(b, al2));
        adm.add(new Tuple<>(sip, al3));
       
        net.setAddressMappings(adm);

        ArrayList<Tuple<DatatypeExpr,NetworkObject>> rt1 = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
        rt1.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_sip"), sip));
        rt1.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_b"), sip));
       
        net.routingTable(a, rt1);

        ArrayList<Tuple<DatatypeExpr,NetworkObject>> rt2 = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
        rt2.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_a"), sip));
        rt2.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_sip"), sip));
      
        net.routingTable(b, rt2);

        ArrayList<Tuple<DatatypeExpr,NetworkObject>> rt3 = new ArrayList<Tuple<DatatypeExpr,NetworkObject>>();
        rt3.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_a"),a));
        rt3.add(new Tuple<DatatypeExpr,NetworkObject>(nctx.am.get("ip_b"),b));
    
        net.routingTable(sip, rt3);
        
        net.attach(a, b, sip);

        //Configuring middleboxes
        
	    sip.addEntry(ctx.mkInt(1), nctx.am.get("ip_b"));
	    sip.installSipServer(nctx.am.get("ip_sip"));
	    
        PacketModel packet = new PacketModel();
        packet.setEmailFrom(4);
        packet.setBody(1);
        packet.setProto(nctx.SIP_INVITE);
        packet.setIp_dest(nctx.am.get("ip_sip"));
        a.installEndHost(packet);
        b.installAsWebServer(new PacketModel());
        check = new Checker(ctx,nctx,net);
}
    
    public static void main(String[] args) throws Z3Exception
    {
        Test_SIP model = new Test_SIP();
        model.resetZ3();
        
        IsolationResult ret =model.check.checkIsolationProperty(model.a,model.b);
   //   model.printVector(ret.assertions);
      System.out.println("==============end assertions===========");
    	if (ret.result == Status.UNSATISFIABLE){
     	   System.out.println("a-->b  UNSAT"); // Nodes a and b are isolated
    	}else{
     		System.out.println("a-->b  SAT ");
     //		System.out.println(ret.model);
     		
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