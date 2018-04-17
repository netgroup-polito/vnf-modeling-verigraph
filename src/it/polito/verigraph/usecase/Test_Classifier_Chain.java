/**
 * 
 */
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
import it.polito.verigraph.mcnet.netobjs.Rule_Classifier;

/**
 * @author s211483
 *         <p/>
 *         Test_Case
 *         <p/>
 *         | A | <------> | CF1 | <------> | CF2 | <------> | B |
 *
 */
public class Test_Classifier_Chain {

	public Checker check;
	public Context ctx;

	public PolitoEndHost a;
	public PolitoEndHost b;
	public Rule_Classifier cf1, cf2;

	public Test_Classifier_Chain() {
		ctx = new Context();

		NetContext nctx = new NetContext(ctx, new String[] { "a", "b", "cf1", "cf2" },
				new String[] { "ip_a", "ip_b", "ip_cf1", "ip_cf2" });
		Network net = new Network(ctx, new Object[] { nctx });

		a = new PolitoEndHost(ctx, new Object[] { nctx.nm.get("a"), net, nctx });
		b = new PolitoEndHost(ctx, new Object[] { nctx.nm.get("b"), net, nctx });
		cf1 = new Rule_Classifier(ctx, new Object[] { nctx.nm.get("cf1"), net, nctx });
		cf2 = new Rule_Classifier(ctx, new Object[] { nctx.nm.get("cf2"), net, nctx });

		ArrayList<Tuple<NetworkObject, ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject, ArrayList<DatatypeExpr>>>();
		ArrayList<DatatypeExpr> al1 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al2 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al3 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al4 = new ArrayList<DatatypeExpr>();
		al1.add(nctx.am.get("ip_a"));
		al2.add(nctx.am.get("ip_b"));
		al3.add(nctx.am.get("ip_cf1"));
		al4.add(nctx.am.get("ip_cf2"));
		adm.add(new Tuple<>(a, al1));
		adm.add(new Tuple<>(b, al2));
		adm.add(new Tuple<>(cf1, al3));
		adm.add(new Tuple<>(cf2, al4));
		net.setAddressMappings(adm);

		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt1 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt1.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_b"), cf1));
		rt1.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf1"), cf1));
		rt1.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf2"), cf1));

		net.routingTable(a, rt1);

		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt2 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt2.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_a"), a));
		rt2.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_b"), cf2));
		rt2.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf2"), cf2));

		net.routingTable(cf1, rt2);

		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt3 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt3.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_a"), cf1));
		rt3.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_b"), b));
		rt3.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf1"), cf1));

		net.routingTable(cf2, rt3);

		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt4 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_a"), cf2));
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf1"), cf2));
		rt4.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf2"), cf2));

		net.routingTable(b, rt4);

		net.attach(a, b, cf1, cf2);

		// Configuration:
		// Test_01: send(a,b,p.proto=1);

		cf1.addEntry(ctx.mkInt(3)); // POP3_REQUEST
		cf1.addEntry(ctx.mkInt(1)); // HTTP_REQUEST
		cf2.addEntry(ctx.mkInt(1)); // expeted_result=SAT if add else UNSAT
		cf2.addEntry(ctx.mkInt(2)); // HTTP_RESPONSE

		PacketModel packet = new PacketModel();

		packet.setProto(1);
		packet.setIp_dest(nctx.am.get("ip_b"));

		cf1.installClassifier();
		cf2.installClassifier();
		a.installAsWebClient(nctx.am.get("ip_b"), packet);
		b.installAsWebServer(packet);

		check = new Checker(ctx, nctx, net);
	}

	public void resetZ3() throws Z3Exception {
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");
		ctx = new Context(cfg);
	}

	public static void main(String[] args) {
		Test_Classifier_Chain model = new Test_Classifier_Chain();
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
