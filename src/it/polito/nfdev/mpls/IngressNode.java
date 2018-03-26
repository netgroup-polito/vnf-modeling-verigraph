package it.polito.nfdev.mpls;

import java.util.List;

import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.RoutingResult;
import it.polito.nfdev.lib.Table;
import it.polito.nfdev.lib.TableEntry;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult.Action;
import it.polito.nfdev.nat.PortPool;

public class IngressNode extends NetworkFunction{

	private Table inTable;
	private Integer label;
	private PortPool portPool;
	
	public IngressNode(List<Interface> interfaces) {
		super(interfaces);
		 this.portPool = new PortPool(1, 1024);
		 this.inTable = new Table(1,0);
		 inTable.setTypes(Table.TableTypes.Ip);
	}

	@Override
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) {
		Packet p = null;
		try {
			p = packet.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		Integer label = portPool.getAvailablePort();
		if(label == null)
			return new RoutingResult(Action.DROP, null, null);
		
		TableEntry entry = inTable.matchEntry(packet.getField(PacketField.IP_DST));
		if(entry!=null){
			
			p.setField(PacketField.OPTIONS, String.valueOf(label));
			return new RoutingResult(Action.FORWARD,packet,iface);
		}
		
		return new RoutingResult(Action.DROP, null, null);
		
	}

}
