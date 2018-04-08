package it.polito.nfdev.vAAA;
import java.util.List;

import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.RoutingResult;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult.Action;
import it.polito.nfdev.nat.PortPool;

public class AAAClient extends NetworkFunction {
	
	private String ip_aaa;
	private String namePw;  //username+password
	private PortPool portPool;
	protected Interface initialForwardingInterface;
	
	public AAAClient(List<Interface> interfaces,String ip_aaa, String namePw) {
		super(interfaces);
		
		this.ip_aaa = ip_aaa;
		this.namePw = namePw;
	    this.portPool = new PortPool(10000, 1024);
	    initialForwardingInterface = interfaces.get(0);
	}
	
	public RoutingResult defineSendingPacket() {
		Packet p = new Packet();
		
		Integer new_port = portPool.getAvailablePort();
		if(new_port == null)
			return new RoutingResult(Action.DROP, null, null);
		
		p.setField(PacketField.IP_DST, ip_aaa);
		p.setField(PacketField.PORT_DST, String.valueOf(new_port));
		p.setField(PacketField.BODY, namePw);
		return new RoutingResult(Action.FORWARD,p,initialForwardingInterface);
	}

	@Override
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) {
		// TODO Auto-generated method stub
		return null;
	}
	
}