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
import it.polito.verigraph.mcnet.netobjs.Rule_Ids;
import it.polito.verigraph.mcnet.netobjs.Rule_Antispam;

/**
 * @author s211483
 *         <p/>
 *         Test_Case
 *         <p/>
 *         | A | <------> | fw1 | <------> | CF | <------> | IDS1 |<------> | fw2 | <------> | B |
 *										     |
 *											 |_ <------> | ASPAM | <------> | C |
 *
 *
 */
public class Test_Graph_B {
	
	public Checker check;
	public Context ctx;

	public PolitoEndHost a,b,c;
	public Rule_Classifier cf;
	public Rule_Antispam aspam;
	public Rule_AclFirewall fw1,fw2;
	public Rule_Ids ids1;
	
	public Test_Graph_B() {
		ctx = new Context();

		NetContext nctx = new NetContext(ctx, new String[] { "a", "b", "c", "cf", "aspam", "ids1","fw1","fw2"},
				new String[] { "ip_a", "ip_b", "ip_c", "ip_cf", "ip_aspam", "ip_ids1", "ip_fw1", "ip_fw2" });
		Network net = new Network(ctx, new Object[] { nctx });

		a = new PolitoEndHost(ctx, new Object[] { nctx.nm.get("a"), net, nctx });
		b = new PolitoEndHost(ctx, new Object[] { nctx.nm.get("b"), net, nctx });
		c = new PolitoEndHost(ctx, new Object[] { nctx.nm.get("c"), net, nctx });
		cf = new Rule_Classifier(ctx, new Object[] { nctx.nm.get("cf"), net, nctx });
		aspam = new Rule_Antispam(ctx, new Object[] { nctx.nm.get("aspam"), net, nctx });	
		ids1 = new Rule_Ids(ctx,new Object[] {nctx.nm.get("ids1"),net,nctx});
		fw1 = new Rule_AclFirewall(ctx,new Object[] {nctx.nm.get("fw1"),net,nctx});
		fw2 = new Rule_AclFirewall(ctx,new Object[] {nctx.nm.get("fw2"),net,nctx});
		

		ArrayList<Tuple<NetworkObject, ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject, ArrayList<DatatypeExpr>>>();
		ArrayList<DatatypeExpr> ala = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> alb = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> alc = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> alcf = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> alaspam = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> alids1 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> alfw1 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> alfw2 = new ArrayList<DatatypeExpr>();

		ala.add(nctx.am.get("ip_a"));
		alb.add(nctx.am.get("ip_b"));
		alc.add(nctx.am.get("ip_c"));
		alcf.add(nctx.am.get("ip_cf"));
		alaspam.add(nctx.am.get("ip_aspam"));
		alids1.add(nctx.am.get("ip_ids1"));
		alfw1.add(nctx.am.get("ip_fw1"));
		alfw2.add(nctx.am.get("ip_fw2"));

		adm.add(new Tuple<>(a, ala));
		adm.add(new Tuple<>(b, alb));
		adm.add(new Tuple<>(c, alc));
		adm.add(new Tuple<>(cf, alcf));
		adm.add(new Tuple<>(aspam, alaspam));
		adm.add(new Tuple<>(ids1, alids1));
		adm.add(new Tuple<>(fw1, alfw1));
		adm.add(new Tuple<>(fw2, alfw2));
		net.setAddressMappings(adm);

		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt1 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt1.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_b"), fw1));
		rt1.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_c"), fw1));
		rt1.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf"), fw1));
		rt1.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_aspam"), fw1));
		rt1.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_ids1"), fw1));	
		rt1.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw1"), fw1));
		rt1.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw2"), fw1));
		net.routingTable(a, rt1);
		
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt2 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt2.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_a"), fw2));
		rt2.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_ids1"), fw2));
		rt2.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf"), fw2));
		rt2.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_aspam"), fw2));	
		rt2.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_c"), fw2));
		rt2.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw1"), fw2));
		rt2.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw2"), fw2));
		net.routingTable(b, rt2);
		
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt3 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt3.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_a"), aspam));
		rt3.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_b"), aspam));
		rt3.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_aspam"), aspam));
		rt3.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf"), aspam));	
		rt3.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_ids1"), aspam));
		rt3.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw1"), aspam));
		rt3.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw2"), aspam));
		net.routingTable(c, rt3);

		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt4 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_a"), fw1));
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw1"), fw1));
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_b"), ids1));
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_c"), aspam));
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_aspam"), aspam));	
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_ids1"), ids1));
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw2"), ids1));
		net.routingTable(cf, rt4);
		
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt5 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt5.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_a"), cf));
		rt5.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_b"), cf));
		rt5.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_c"), c));
		rt5.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf"), cf));	
		rt5.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_ids1"), cf));
		rt5.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw1"), cf));
		rt5.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw2"), cf));
		net.routingTable(aspam, rt5);
		
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt6 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt6.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_a"), cf));
		rt6.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_b"), fw2));
		rt6.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_c"), cf));
		rt6.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf"), cf));	
		rt6.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_aspam"), cf));	
		rt6.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw1"), cf));
		rt6.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw2"), fw2));
		net.routingTable(ids1, rt6);
		
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt7 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt7.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_a"), a));
		rt7.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_b"), cf));
		rt7.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_c"), cf));
		rt7.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf"), cf));	
		rt7.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_aspam"), cf));	
		rt7.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_ids1"), cf));
		rt7.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw2"), cf));
		net.routingTable(fw1, rt7);		
		
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt8 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt8.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_a"), ids1));
		rt8.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_b"), b));
		rt8.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_c"), ids1));
		rt8.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf"), ids1));	
		rt8.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_aspam"), ids1));	
		rt8.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_ids1"), ids1));
		rt8.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_fw1"), ids1));
		net.routingTable(fw2, rt8);		
		

		net.attach(a, b, c, cf, aspam, ids1,fw1,fw2);

		
		// Configuration:
		cf.addEntry(ctx.mkInt(3)); // POP3_REQUEST
		cf.addEntry(ctx.mkInt(1)); // HTTP_REQUEST //if not add expeted_result=UNSAT
		cf.addEntry(ctx.mkInt(2)); // HTTP_RESPONSE
	    
	    aspam.addEntry(ctx.mkInt(3));
	    aspam.addEntry(ctx.mkInt(300));
	    aspam.addEntry(ctx.mkInt(42));
	    aspam.addEntry(ctx.mkInt(333));
	    aspam.addEntry(ctx.mkInt(33));
	    aspam.installAntispam();
	    
	    ids1.addEntry(ctx.mkInt(6));
	    ids1.addEntry(ctx.mkInt(66));
	    ids1.addEntry(ctx.mkInt(666));
	    ids1.addEntry(ctx.mkInt(16));
	    ids1.addEntry(ctx.mkInt(61));
	    ids1.addEntry(ctx.mkInt(601));
	    ids1.installIds();
	        
	    fw1.addEntry(nctx.am.get("ip_a"), nctx.am.get("ip_fw2"));
	    fw1.addEntry(nctx.am.get("ip_a"), nctx.am.get("ip_aspam"));
	    //fw1.addEntry(nctx.am.get("ip_a"), nctx.am.get("ip_b")); 
	    //fw1.addEntry(nctx.am.get("ip_b"), nctx.am.get("ip_a"));
	    //fw1.addEntry(nctx.am.get("ip_a"), nctx.am.get("ip_c"));
	    fw1.addEntry(nctx.am.get("ip_c"), nctx.am.get("ip_a"));
	    fw1.installAclFirewall();
	    
	    fw2.addEntry(nctx.am.get("ip_b"), nctx.am.get("ip_fw1"));
	    fw2.addEntry(nctx.am.get("ip_b"), nctx.am.get("ip_aspam"));
	    //fw2.addEntry(nctx.am.get("ip_a"), nctx.am.get("ip_b")); 
	    //fw2.addEntry(nctx.am.get("ip_b"), nctx.am.get("ip_a")); 
	    fw2.installAclFirewall();
	    
		PacketModel packet = new PacketModel();
			//packet.setProto(1);
		//packet.setProto(2);
			packet.setProto(3);
		packet.setEmailFrom(100);
			//packet.setEmailFrom(333);
		packet.setUrl(100);
			//packet.setUrl(666); 
		packet.setIp_dest(nctx.am.get("ip_a"));
			//packet.setIp_dest(nctx.am.get("ip_b"));
			//packet.setIp_dest(nctx.am.get("ip_c"));
		
		cf.installClassifier();
		
		a.installEndHost(packet);
		b.installEndHost(packet);
		c.installEndHost(packet);


		check = new Checker(ctx, nctx, net);
	}

	public void resetZ3() throws Z3Exception {
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");
		ctx = new Context(cfg);
	}


	public static void main(String[] args) {
		Test_Graph_B model = new Test_Graph_B();
		model.resetZ3();

		//IsolationResult ret = model.check.checkIsolationProperty(model.a, model.b); 
		//IsolationResult ret = model.check.checkIsolationProperty(model.b, model.a); 
		//IsolationResult ret = model.check.checkIsolationProperty(model.a, model.c); 
		IsolationResult ret = model.check.checkIsolationProperty(model.c, model.a); 
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
