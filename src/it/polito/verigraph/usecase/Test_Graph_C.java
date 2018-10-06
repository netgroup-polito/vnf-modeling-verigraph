package it.polito.verigraph.usecase;

import java.util.ArrayList;
import java.util.HashMap;

import com.microsoft.z3.Context;
import com.microsoft.z3.DatatypeExpr;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

import it.polito.verigraph.mcnet.components.*;
import it.polito.verigraph.mcnet.netobjs.PacketModel;
import it.polito.verigraph.mcnet.netobjs.PolitoEndHost;
import it.polito.verigraph.mcnet.netobjs.Rule_AclFirewall;
import it.polito.verigraph.mcnet.netobjs.Rule_Antispam;
import it.polito.verigraph.mcnet.netobjs.Rule_Classifier;
import it.polito.verigraph.mcnet.netobjs.Rule_Ids;
import it.polito.verigraph.mcnet.netobjs.Rule_MailServer;
import it.polito.verigraph.mcnet.netobjs.Rule_WebCache;
import it.polito.verigraph.mcnet.netobjs.Rule_WebServer;

/**
 * <p/>
 * Network Graph node: |host_a| --> |classifier| --> |ids| --> |firewall| --> |host_b|
 * 							        |classifier| --> |firewall2| --> |webCache| --> |WebServer|
 * 									|classifier| --> |antispam| --> |firewall3| --> |MailServer|
 * 
 */
public class Test_Graph_C {

	public Checker check;
	public Context ctx;

	public PolitoEndHost a, b;
	public Rule_Classifier cf;
	public Rule_Ids ids,ids2;
	public Rule_AclFirewall fw,fw2,fw3;
	public Rule_WebCache c;
	public Rule_WebServer ws;
	public Rule_Antispam as;
	public Rule_MailServer ms;

	public Test_Graph_C() {
		ctx = new Context();

		NetContext nctx = new NetContext(ctx, new String[] { "a", "cf", "ids", "fw", "b","fw2","ids2","c","ws","as","fw3","ms" },
				new String[] { "ip_a", "ip_cf", "ip_ids", "ip_fw", "ip_b","ip_fw2","ip_ids2","ip_c","ip-c","ip_ws","ip_as","ip_fw3","ip_ms" });
		Network net = new Network(ctx, new Object[] { nctx });
		
		a = new PolitoEndHost(ctx, new Object[] { nctx.nm.get("a"), net, nctx });
		cf = new Rule_Classifier(ctx, new Object[] { nctx.nm.get("cf"), net, nctx });
		ids = new Rule_Ids(ctx, new Object[] { nctx.nm.get("ids"), net, nctx });
		fw = new Rule_AclFirewall(ctx, new Object[] { nctx.nm.get("fw"), net, nctx });
		b = new PolitoEndHost(ctx, new Object[] { nctx.nm.get("b"), net, nctx });
		fw2 = new Rule_AclFirewall(ctx, new Object[] { nctx.nm.get("fw2"), net, nctx });
		ids2 = new Rule_Ids(ctx, new Object[] { nctx.nm.get("ids2"), net, nctx });
		c = new Rule_WebCache(ctx, new Object[] { nctx.nm.get("c"), net, nctx });
		ws = new Rule_WebServer(ctx, new Object[] { nctx.nm.get("ws"), net, nctx });
		as = new Rule_Antispam(ctx, new Object[] { nctx.nm.get("as"), net, nctx });
		fw3 = new Rule_AclFirewall(ctx, new Object[] { nctx.nm.get("fw3"), net, nctx });
		ms = new Rule_MailServer(ctx, new Object[] { nctx.nm.get("ms"), net, nctx });
		

		ArrayList<Tuple<NetworkObject, ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject, ArrayList<DatatypeExpr>>>();
		ArrayList<DatatypeExpr> al1 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al2 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al3 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al4 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al5 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al6 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al7 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al8 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al9 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al10 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al11 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al12 = new ArrayList<DatatypeExpr>();
		
		al1.add(nctx.am.get("ip_a"));
		al2.add(nctx.am.get("ip_cf"));
		al3.add(nctx.am.get("ip_ids"));
		al4.add(nctx.am.get("ip_fw"));
		al5.add(nctx.am.get("ip_b"));
		al6.add(nctx.am.get("ip_fw2"));
		al7.add(nctx.am.get("ip_ids2"));
		al8.add(nctx.am.get("ip_c"));
		al9.add(nctx.am.get("ip_ws"));
		al10.add(nctx.am.get("ip_as"));
		al11.add(nctx.am.get("ip_fw3"));
		al12.add(nctx.am.get("ip_ms"));
		

		adm.add(new Tuple<>(a, al1));
		adm.add(new Tuple<>(cf, al2));
		adm.add(new Tuple<>(ids, al3));
		adm.add(new Tuple<>(fw, al4));
		adm.add(new Tuple<>(b, al5));
		adm.add(new Tuple<>(fw2, al6));
		adm.add(new Tuple<>(ids2, al7));
		adm.add(new Tuple<>(c, al8));
		adm.add(new Tuple<>(ws, al9));
		adm.add(new Tuple<>(as, al10));
		adm.add(new Tuple<>(fw3, al11));
		adm.add(new Tuple<>(ms, al12));

		net.setAddressMappings(adm);

		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rta = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rta.add(new Tuple<>(nctx.am.get("ip_cf"), cf));
		rta.add(new Tuple<>(nctx.am.get("ip_ids"), cf));
		rta.add(new Tuple<>(nctx.am.get("ip_fw"), cf));
		rta.add(new Tuple<>(nctx.am.get("ip_b"), cf));
		rta.add(new Tuple<>(nctx.am.get("ip_fw2"), cf));
		rta.add(new Tuple<>(nctx.am.get("ip_ids2"), cf));
		rta.add(new Tuple<>(nctx.am.get("ip_c"), cf));
		rta.add(new Tuple<>(nctx.am.get("ip_ws"), cf));
		rta.add(new Tuple<>(nctx.am.get("ip_as"), cf));
		rta.add(new Tuple<>(nctx.am.get("ip_fw3"), cf));
		rta.add(new Tuple<>(nctx.am.get("ip_ms"), cf));
		net.routingTable(a, rta);
		
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rtcf = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rtcf.add(new Tuple<>(nctx.am.get("ip_a"), a));
		rtcf.add(new Tuple<>(nctx.am.get("ip_ids"), ids));
		rtcf.add(new Tuple<>(nctx.am.get("ip_fw"), ids));
		rtcf.add(new Tuple<>(nctx.am.get("ip_b"), ids));
		rtcf.add(new Tuple<>(nctx.am.get("ip_fw2"), fw2));
		rtcf.add(new Tuple<>(nctx.am.get("ip_ids2"), fw2));
		rtcf.add(new Tuple<>(nctx.am.get("ip_c"), fw2));
		rtcf.add(new Tuple<>(nctx.am.get("ip_ws"), fw2));
		rtcf.add(new Tuple<>(nctx.am.get("ip_as"), as));
		rtcf.add(new Tuple<>(nctx.am.get("ip_fw3"), as));
		rtcf.add(new Tuple<>(nctx.am.get("ip_ms"), as));
		net.routingTable(cf, rtcf);
		
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rtids = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rtids.add(new Tuple<>(nctx.am.get("ip_fw"), fw));
		rtids.add(new Tuple<>(nctx.am.get("ip_b"), fw));
		rtids.add(new Tuple<>(nctx.am.get("ip_a"), cf));
		rtids.add(new Tuple<>(nctx.am.get("ip_cf"), cf));
		rtids.add(new Tuple<>(nctx.am.get("ip_fw2"), cf));
		rtids.add(new Tuple<>(nctx.am.get("ip_ids2"), cf));
		rtids.add(new Tuple<>(nctx.am.get("ip_c"), cf));
		rtids.add(new Tuple<>(nctx.am.get("ip_ws"), cf));
		rtids.add(new Tuple<>(nctx.am.get("ip_as"), cf));
		rtids.add(new Tuple<>(nctx.am.get("ip_fw3"), cf));
		rtids.add(new Tuple<>(nctx.am.get("ip_ms"), cf));
		net.routingTable(ids, rtids);
		
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rtfw = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rtfw.add(new Tuple<>(nctx.am.get("ip_b"), b));
		rtfw.add(new Tuple<>(nctx.am.get("ip_ids"), ids));
		rtfw.add(new Tuple<>(nctx.am.get("ip_cf"), ids));
		rtfw.add(new Tuple<>(nctx.am.get("ip_a"), ids));
		rtfw.add(new Tuple<>(nctx.am.get("ip_fw2"), ids));
		rtfw.add(new Tuple<>(nctx.am.get("ip_ids2"), ids));
		rtfw.add(new Tuple<>(nctx.am.get("ip_c"), ids));
		rtfw.add(new Tuple<>(nctx.am.get("ip_ws"), ids));
		rtfw.add(new Tuple<>(nctx.am.get("ip_as"), ids));
		rtfw.add(new Tuple<>(nctx.am.get("ip_fw3"), ids));
		rtfw.add(new Tuple<>(nctx.am.get("ip_ms"), ids));
		net.routingTable(fw, rtfw);
	
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rtb = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rtb.add(new Tuple<>(nctx.am.get("ip_fw"), fw));
		rtb.add(new Tuple<>(nctx.am.get("ip_ids"), fw));
		rtb.add(new Tuple<>(nctx.am.get("ip_cf"), fw));
		rtb.add(new Tuple<>(nctx.am.get("ip_a"), fw));
		rtb.add(new Tuple<>(nctx.am.get("ip_fw2"), fw));
		rtb.add(new Tuple<>(nctx.am.get("ip_ids2"), fw));
		rtb.add(new Tuple<>(nctx.am.get("ip_c"), fw));
		rtb.add(new Tuple<>(nctx.am.get("ip_ws"), fw));
		rtb.add(new Tuple<>(nctx.am.get("ip_as"), fw));
		rtb.add(new Tuple<>(nctx.am.get("ip_fw3"), fw));
		rtb.add(new Tuple<>(nctx.am.get("ip_ms"), fw));
		net.routingTable(b, rtb);
		
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rtfw2 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rtfw2.add(new Tuple<>(nctx.am.get("ip_a"), cf));
		rtfw2.add(new Tuple<>(nctx.am.get("ip_cf"), cf));
		rtfw2.add(new Tuple<>(nctx.am.get("ip_ids"), cf));
		rtfw2.add(new Tuple<>(nctx.am.get("ip_fw"), cf));
		rtfw2.add(new Tuple<>(nctx.am.get("ip_b"), cf));
		rtfw2.add(new Tuple<>(nctx.am.get("ip_ids2"), ids2));
		rtfw2.add(new Tuple<>(nctx.am.get("ip_c"), ids2));
		rtfw2.add(new Tuple<>(nctx.am.get("ip_ws"), ids2));
		rtfw2.add(new Tuple<>(nctx.am.get("ip_as"), cf));
		rtfw2.add(new Tuple<>(nctx.am.get("ip_fw3"), cf));
		rtfw2.add(new Tuple<>(nctx.am.get("ip_ms"), cf));
		net.routingTable(fw2, rtfw2);
		
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rtids2 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rtids2.add(new Tuple<>(nctx.am.get("ip_a"), fw2));
		rtids2.add(new Tuple<>(nctx.am.get("ip_cf"), fw2));
		rtids2.add(new Tuple<>(nctx.am.get("ip_ids"), fw2));
		rtids2.add(new Tuple<>(nctx.am.get("ip_fw"), fw2));
		rtids2.add(new Tuple<>(nctx.am.get("ip_b"), fw2));
		rtids2.add(new Tuple<>(nctx.am.get("ip_fw2"), fw2));
		rtids2.add(new Tuple<>(nctx.am.get("ip_c"), c));
		rtids2.add(new Tuple<>(nctx.am.get("ip_ws"), c));
		rtids2.add(new Tuple<>(nctx.am.get("ip_as"), fw2));
		rtids2.add(new Tuple<>(nctx.am.get("ip_fw3"), fw2));
		rtids2.add(new Tuple<>(nctx.am.get("ip_ms"), fw2));
		net.routingTable(ids2, rtids2);
		
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rtc = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rtc.add(new Tuple<>(nctx.am.get("ip_a"), ids2));
		rtc.add(new Tuple<>(nctx.am.get("ip_cf"), ids2));
		rtc.add(new Tuple<>(nctx.am.get("ip_ids"), ids2));
		rtc.add(new Tuple<>(nctx.am.get("ip_fw"), ids2));
		rtc.add(new Tuple<>(nctx.am.get("ip_b"), ids2));
		rtc.add(new Tuple<>(nctx.am.get("ip_fw2"), ids2));
		rtc.add(new Tuple<>(nctx.am.get("ip_ids2"), ids2));
		rtc.add(new Tuple<>(nctx.am.get("ip_ws"), ws));
		rtc.add(new Tuple<>(nctx.am.get("ip_as"), ids2));
		rtc.add(new Tuple<>(nctx.am.get("ip_fw3"), ids2));
		rtc.add(new Tuple<>(nctx.am.get("ip_ms"), ids2));
		net.routingTable(c, rtc);
		
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rtws = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rtws.add(new Tuple<>(nctx.am.get("ip_a"), c));
		rtws.add(new Tuple<>(nctx.am.get("ip_cf"), c));
		rtws.add(new Tuple<>(nctx.am.get("ip_ids"), c));
		rtws.add(new Tuple<>(nctx.am.get("ip_fw"), c));
		rtws.add(new Tuple<>(nctx.am.get("ip_b"), c));
		rtws.add(new Tuple<>(nctx.am.get("ip_fw2"), c));
		rtws.add(new Tuple<>(nctx.am.get("ip_ids2"), c));
		rtws.add(new Tuple<>(nctx.am.get("ip_c"), c));
		rtws.add(new Tuple<>(nctx.am.get("ip_as"), c));
		rtws.add(new Tuple<>(nctx.am.get("ip_fw3"), c));
		rtws.add(new Tuple<>(nctx.am.get("ip_ms"), c));
		net.routingTable(ws, rtws);
		
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rtas = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rtas.add(new Tuple<>(nctx.am.get("ip_a"), cf));
		rtas.add(new Tuple<>(nctx.am.get("ip_cf"), cf));
		rtas.add(new Tuple<>(nctx.am.get("ip_ids"), cf));
		rtas.add(new Tuple<>(nctx.am.get("ip_fw"), cf));
		rtas.add(new Tuple<>(nctx.am.get("ip_b"), cf));
		rtas.add(new Tuple<>(nctx.am.get("ip_fw2"), cf));
		rtas.add(new Tuple<>(nctx.am.get("ip_ids2"), cf));
		rtas.add(new Tuple<>(nctx.am.get("ip_c"), cf));
		rtas.add(new Tuple<>(nctx.am.get("ip_ws"), cf));
		rtas.add(new Tuple<>(nctx.am.get("ip_fw3"), fw3));
		rtas.add(new Tuple<>(nctx.am.get("ip_ms"), fw3));
		net.routingTable(as, rtas);
		
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rtfw3 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rtfw3.add(new Tuple<>(nctx.am.get("ip_a"), as));
		rtfw3.add(new Tuple<>(nctx.am.get("ip_cf"), as));
		rtfw3.add(new Tuple<>(nctx.am.get("ip_ids"), as));
		rtfw3.add(new Tuple<>(nctx.am.get("ip_fw"), as));
		rtfw3.add(new Tuple<>(nctx.am.get("ip_b"), as));
		rtfw3.add(new Tuple<>(nctx.am.get("ip_fw2"), as));
		rtfw3.add(new Tuple<>(nctx.am.get("ip_ids2"), as));
		rtfw3.add(new Tuple<>(nctx.am.get("ip_c"), as));
		rtfw3.add(new Tuple<>(nctx.am.get("ip_ws"), as));
		rtfw3.add(new Tuple<>(nctx.am.get("ip_as"), as));
		rtfw3.add(new Tuple<>(nctx.am.get("ip_ms"), ms));
		net.routingTable(fw3, rtfw3);
		
		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rtms = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rtms.add(new Tuple<>(nctx.am.get("ip_a"), fw3));
		rtms.add(new Tuple<>(nctx.am.get("ip_cf"), fw3));
		rtms.add(new Tuple<>(nctx.am.get("ip_ids"), fw3));
		rtms.add(new Tuple<>(nctx.am.get("ip_fw"), fw3));
		rtms.add(new Tuple<>(nctx.am.get("ip_b"), fw3));
		rtms.add(new Tuple<>(nctx.am.get("ip_fw2"), fw3));
		rtms.add(new Tuple<>(nctx.am.get("ip_ids2"), fw3));
		rtms.add(new Tuple<>(nctx.am.get("ip_c"), fw3));
		rtms.add(new Tuple<>(nctx.am.get("ip_ws"), fw3));
		rtms.add(new Tuple<>(nctx.am.get("ip_fw3"), fw3));
		rtms.add(new Tuple<>(nctx.am.get("ip_as"), fw3));
		net.routingTable(ms, rtms);

		net.attach(a,cf,ids,fw,b,fw2,ids2,c,ws,as,fw3,ms);

		
		cf.addEntry(ctx.mkInt(3)); // POP3_REQUEST
		cf.addEntry(ctx.mkInt(1)); // HTTP_REQUEST //if not add expeted_result=UNSAT
		cf.addEntry(ctx.mkInt(5)); // Null
		cf.installClassifier();

		ids.addEntry(ctx.mkInt(6));
		ids.addEntry(ctx.mkInt(66));
		ids.addEntry(ctx.mkInt(666));
		ids.addEntry(ctx.mkInt(16));
		ids.addEntry(ctx.mkInt(61));
		ids.addEntry(ctx.mkInt(601));
		ids.installIds();
		
		ids2.addEntry(ctx.mkInt(26));
		ids2.addEntry(ctx.mkInt(266));
		ids2.addEntry(ctx.mkInt(2666));
		ids2.addEntry(ctx.mkInt(216));
		ids2.addEntry(ctx.mkInt(261));
		ids2.addEntry(ctx.mkInt(2601));
		ids2.installIds();

		fw.addEntry(nctx.am.get("ip_a"), nctx.am.get("ip_fw"));
		fw.addEntry(nctx.am.get("ip_b"), nctx.am.get("ip_a"));
		fw.installAclFirewall();
		
		fw2.addEntry(nctx.am.get("ip_a"), nctx.am.get("ip_fw"));
		fw2.addEntry(nctx.am.get("ip_a"), nctx.am.get("ip_b"));
		fw2.installAclFirewall();
		
		fw3.addEntry(nctx.am.get("ip_a"), nctx.am.get("ip_fw"));
		fw3.addEntry(nctx.am.get("ip_a"), nctx.am.get("ip_b"));
		fw3.addEntry(nctx.am.get("ip_a"), nctx.am.get("ip_ws"));
		fw3.installAclFirewall();
		
		ArrayList<DatatypeExpr> ia = new ArrayList<DatatypeExpr>();
		ia.add(nctx.am.get("ip_a"));
		c.setInternalAddress(ia);
		c.installWebCache();
		
		ws.installWebServer();
		
	    as.addEntry(ctx.mkInt(3));
	    as.addEntry(ctx.mkInt(300));
	    as.addEntry(ctx.mkInt(42));
	    as.addEntry(ctx.mkInt(333));
	    as.addEntry(ctx.mkInt(33));
	    as.installAntispam();
	    
	    ms.installMailServer(ctx.mkInt(1)); //1=RESPONSE

		PacketModel packet = new PacketModel();
		packet.setProto(5); // HTTP_REQUEST
		packet.setEmailFrom(100);
		packet.setUrl(100);
		packet.setIp_dest(nctx.am.get("ip_b"));

		a.installEndHost(packet);
		b.installEndHost(packet);
		
		check = new Checker(ctx, nctx, net);
	}

	public static void main(String[] args) {
		Test_Graph_C model = new Test_Graph_C();
		model.resetZ3();

		IsolationResult ret = model.check.checkIsolationProperty(model.a, model.b);
		if (ret.result == Status.UNSATISFIABLE) {
			System.out.println("UNSAT");
		} else {
			System.out.println("**************************************************");
			System.out.println("SAT ");
			System.out.print("\n\t_Model: ");
			System.out.println(ret.model); // Find Send to check
		}

	}

	public void resetZ3() throws Z3Exception {
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");
		ctx = new Context(cfg);
	}

}
