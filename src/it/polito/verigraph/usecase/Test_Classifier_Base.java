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
 *         | A | <------> | CF | <------> | B |
 *
 */
public class Test_Classifier_Base {

	public Checker check;
	public Context ctx;

	public PolitoEndHost a;
	public PolitoEndHost b;
	public Rule_Classifier cf;

	/**
	 * 
	 */
	public Test_Classifier_Base() {

		ctx = new Context();

		NetContext nctx = new NetContext(ctx, new String[] { "a", "b", "cf" },
				new String[] { "ip_a", "ip_b", "ip_cf" });
		Network net = new Network(ctx, new Object[] { nctx });

		a = new PolitoEndHost(ctx, new Object[] { nctx.nm.get("a"), net, nctx });
		b = new PolitoEndHost(ctx, new Object[] { nctx.nm.get("b"), net, nctx });
		cf = new Rule_Classifier(ctx, new Object[] { nctx.nm.get("cf"), net, nctx });

		ArrayList<Tuple<NetworkObject, ArrayList<DatatypeExpr>>> adm = new ArrayList<Tuple<NetworkObject, ArrayList<DatatypeExpr>>>();
		ArrayList<DatatypeExpr> al1 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al2 = new ArrayList<DatatypeExpr>();
		ArrayList<DatatypeExpr> al3 = new ArrayList<DatatypeExpr>();
		al1.add(nctx.am.get("ip_a"));
		al2.add(nctx.am.get("ip_b"));
		al3.add(nctx.am.get("ip_cf"));
		adm.add(new Tuple<>(a, al1));
		adm.add(new Tuple<>(b, al2));
		adm.add(new Tuple<>(cf, al3));
		net.setAddressMappings(adm);

		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt1 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt1.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_b"), cf));
		rt1.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf"), cf));

		net.routingTable(a, rt1);

		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt2 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt2.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_a"), a));
		rt2.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_b"), b));

		net.routingTable(cf, rt2);

		ArrayList<Tuple<DatatypeExpr, NetworkObject>> rt3 = new ArrayList<Tuple<DatatypeExpr, NetworkObject>>();
		rt3.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_a"), cf));
		rt3.add(new Tuple<DatatypeExpr, NetworkObject>(nctx.am.get("ip_cf"), cf));

		net.routingTable(b, rt3);

		net.attach(a, b, cf);

		// Configuration:

		cf.addEntry(ctx.mkInt(3)); //POP3_REQUEST
		cf.addEntry(ctx.mkInt(1)); //HTTP_REQUEST
		cf.addEntry(ctx.mkInt(2)); //HTTP_RESPONSE

		PacketModel packet = new PacketModel();
		//Test_01: send(a,b,p.proto=1); expeted_result=SAT
		//Modify_checkIsolationProperty(model.a, model.b)
		packet.setProto(1);
		packet.setIp_dest(nctx.am.get("ip_b"));

		cf.installClassifier();
		a.installAsWebClient(nctx.am.get("ip_b"), packet);
		b.installAsWebServer(packet);
		
//		//Test_02: send(b,a,p.proto=3); expeted_Result=SAT
//		//Modify_checkIsolationProperty(model.b, model.a)
//		packet.setProto(3);
//		packet.setIp_dest(nctx.am.get("ip_a"));
//		cf.installClassifier();
//		b.installAsPOP3MailClient((nctx.am.get("ip_a")), packet);
//		a.installAsPOP3MailServer(packet);
		
//		//Test_03: send(a,b,p.proto=4); expeted_Result=UNSAT
//		//Modify_checkIsolationProperty(model.a, model.b)
//		packet.setProto(4);
//		packet.setIp_dest(nctx.am.get("ip_b"));
//		cf.installClassifier();
//		b.installAsPOP3MailClient((nctx.am.get("ip_a")), packet);
//		a.installAsPOP3MailServer(packet);

		check = new Checker(ctx, nctx, net);
	}

	public void resetZ3() throws Z3Exception {
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");
		ctx = new Context(cfg);
	}

	public void printVector(Object[] array) {
		int i = 0;
		System.out.println("*** Printing vector ***");
		for (Object a : array) {
			i += 1;
			System.out.println("#" + i);
			System.out.println(a);
			System.out.println("*** " + i + " elements printed! ***");
		}
	}
	
	public static void main(String[] args) {
		Test_Classifier_Base model = new Test_Classifier_Base();
		model.resetZ3();

		IsolationResult ret = model.check.checkIsolationProperty(model.a, model.b);
		// model.printVector(ret.assertions);
		if (ret.result == Status.UNSATISFIABLE) {
			System.out.println("UNSAT"); // Nodes a and b are isolated
		} else {
			System.out.println("**************************************************");
			System.out.println("SAT ");
			System.out.print( "\n\t_Model: ");System.out.println(ret.model);; //Find Send to check 
		}
	}

}
