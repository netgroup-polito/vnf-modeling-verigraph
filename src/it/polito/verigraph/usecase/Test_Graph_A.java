package it.polito.verigraph.usecase;

import java.util.ArrayList;
import java.util.HashMap;

import com.microsoft.z3.Context;
import com.microsoft.z3.DatatypeExpr;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

import it.polito.verigraph.mcnet.components.Checker;
import it.polito.verigraph.mcnet.components.IsolationResult;
import it.polito.verigraph.mcnet.components.NetContext;
import it.polito.verigraph.mcnet.components.Network;
import it.polito.verigraph.mcnet.components.NetworkObject;
import it.polito.verigraph.mcnet.components.Tuple;
import it.polito.verigraph.mcnet.netobjs.PacketModel;
import it.polito.verigraph.mcnet.netobjs.PolitoEndHost;
import it.polito.verigraph.mcnet.netobjs.Rule_AclFirewall;
import it.polito.verigraph.mcnet.netobjs.Rule_Classifier;
import it.polito.verigraph.mcnet.netobjs.Rule_Antispam;

/**
 * @author s211483
 *         <p/>
 *         Test_Case
 *         <p/>
 *         | A | <------> | FW | <------> | ASPAM | <------> | CF | <------> | B |
 *
 */
public class Test_Graph_A {
	
	public Checker check;
	public Context ctx;

	public PolitoEndHost a;
	public PolitoEndHost b;
	public Rule_Classifier cf;
	public Rule_AclFirewall fw;
	public Rule_Antispam aspam;
	
	public Test_Graph_A() {
		ctx = new Context();

		NetContext nctx = new NetContext(ctx, new String[] { "a", "b", "fw", "aspam", "cf" },
				new String[] { "ip_a", "ip_b", "ip_fw", "ip_aspam", "ip_cf" });
		Network net = new Network(ctx, new Object[] { nctx });

		a = new PolitoEndHost(ctx, new Object[] { nctx.nm.get("a"), net, nctx });
		b = new PolitoEndHost(ctx, new Object[] { nctx.nm.get("b"), net, nctx });
		fw = new Rule_AclFirewall(ctx, new Object[] { nctx.nm.get("fw"), net, nctx });
		aspam = new Rule_Antispam(ctx, new Object[] { nctx.nm.get("aspam"), net, nctx });
		cf = new Rule_Classifier(ctx, new Object[] { nctx.nm.get("cf"), net, nctx });
		

		ArrayList<Tuple<NetworkObject, ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject, ArrayList<DatatypeExpr>>>();
		ArrayList<DatatypeExpr> al1 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al2 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al3 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al4 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al5 = new ArrayList<DatatypeExpr>();
		
		al1.add(nctx.am.get("ip_a"));
		al2.add(nctx.am.get("ip_b"));
		al3.add(nctx.am.get("ip_fw"));
		al4.add(nctx.am.get("ip_aspam"));
		al5.add(nctx.am.get("ip_cf"));
		adm.add(new Tuple<>(a, al1));
		adm.add(new Tuple<>(b, al2));
		adm.add(new Tuple<>(fw, al3));
		adm.add(new Tuple<>(aspam, al4));
		adm.add(new Tuple<>(cf, al5));
		net.setAddressMappings(adm);

		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt1 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt1.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_b"), fw));
		rt1.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw"), fw));
		rt1.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_aspam"), fw));
		rt1.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf"), fw));	
		net.routingTable(a, rt1);

		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt2 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt2.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_a"), a));
		rt2.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_b"), aspam));
		rt2.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_aspam"), aspam));
		rt2.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf"), aspam));
		net.routingTable(fw, rt2);

		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt3 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt3.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_a"), fw));
		rt3.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_b"), cf));
		rt3.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf"), cf));
		rt3.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw"), fw));
		net.routingTable(aspam, rt3);

		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt4 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_a"), aspam));
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw"), aspam));
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_aspam"), aspam));
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_b"), b));
		net.routingTable(cf, rt4);
		
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt5 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_a"), cf));
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw"), cf));
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_aspam"), cf));
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf"), cf));
		net.routingTable(b, rt5);

		net.attach(a, b, fw, aspam, cf);

		// Configuration:

		cf.addEntry(ctx.mkInt(3)); // POP3_REQUEST
		cf.addEntry(ctx.mkInt(1)); // HTTP_REQUEST //if not add expeted_result=UNSAT
		cf.addEntry(ctx.mkInt(2)); // HTTP_RESPONSE
		
	    fw.addEntry(nctx.am.get("ip_a"), nctx.am.get("ip_fw")); //if (ip_a,ip_b) expeted_result=UNSAT
	    fw.addEntry(nctx.am.get("ip_fw"), nctx.am.get("ip_cf"));  
	    fw.installAclFirewall();
	    
	    aspam.addEntry(ctx.mkInt(3));
	    aspam.addEntry(ctx.mkInt(300));
	    aspam.addEntry(ctx.mkInt(42));
	    aspam.installAntispam();
	    
		PacketModel packet = new PacketModel();
		packet.setProto(3);
		packet.setIp_dest(nctx.am.get("ip_b"));
		
		cf.installClassifier();
		
		a.installEndHost(packet);
		b.installEndHost(packet);


		check = new Checker(ctx, nctx, net);
	}

	public void resetZ3() throws Z3Exception {
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");
		ctx = new Context(cfg);
	}


	public static void main(String[] args) {
		Test_Graph_A model = new Test_Graph_A();
		model.resetZ3();

		IsolationResult ret = model.check.checkIsolationProperty(model.a, model.b);
		if (ret.result == Status.UNSATISFIABLE) {
			System.out.println("UNSAT"); // Nodes a and b are isolated
		} else {
			System.out.println("**************************************************");
			System.out.println("SAT ");
			System.out.print("\n\t_Model: ");
			System.out.println(ret.model); // Find Send to check
		}


	}

}
