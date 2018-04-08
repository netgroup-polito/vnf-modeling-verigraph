package it.polito.nfdev.vCDN;

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
import it.polito.nfdev.verification.Verifier;

public class LocalDnsServer extends NetworkFunction {

	private final Integer TIMEOUT;

	private String ip_GlobalBalancer;
	private Table dnsTable;
	private PortPool portPool;
	private Interface internalFace;
	private Interface externalFace;

	public LocalDnsServer(List<Interface> interfaces, Integer timeout, String ip_GlobalBalancer) {
		super(interfaces);

		internalFace = null;
		externalFace = null;
		for (Interface i : interfaces) {
			if (i.getAttributes().contains(Interface.INTERNAL_ATTR))
				internalFace = i;
			if (i.getAttributes().contains(Interface.EXTERNAL_ATTR))
				externalFace = i;
		}
		this.portPool = new PortPool(10000, 1024);
		this.TIMEOUT = timeout;
		this.ip_GlobalBalancer = ip_GlobalBalancer;
		dnsTable = new Table(2, 0);
		this.dnsTable.setTypes(Table.TableTypes.URL);
		this.dnsTable.setDataDriven();
		
	}

	@Override
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) {
		Packet p = null;
		try {
			p = packet.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		if (iface.isInternal()) 
		{
			if (packet.equalsField(PacketField.PROTO, Packet.HTTP_REQUEST))
			{
				TableEntry entry = dnsTable.matchEntry(packet.getField(PacketField.URL), Verifier.ANY_VALUE);
				if (entry != null) {
					p.setField(PacketField.OLD_DST, packet.getField(PacketField.IP_DST));
					p.setField(PacketField.IP_DST, (String) entry.getValue(1));
				
		
					return new RoutingResult(Action.FORWARD, p, externalInterface);
				} else {
				
					Integer new_port = portPool.getAvailablePort();
					if (new_port == null)
						return new RoutingResult(Action.DROP, null, null);

					p.setField(PacketField.OLD_DST, packet.getField(PacketField.IP_DST));
					p.setField(PacketField.IP_DST, ip_GlobalBalancer);
					p.setField(PacketField.PROTO, Packet.DNS_REQUEST);
					return new RoutingResult(Action.FORWARD, p, externalInterface);
				}
		   }
			return new RoutingResult(Action.DROP, null, null);
		} 
		else //! iface.isInternal()
		{
			if (packet.equalsField(PacketField.PROTO, Packet.DNS_RESPONSE)) 
			{
				TableEntry entry = new TableEntry(2);
				entry.setValue(0, packet.getField(PacketField.URL));
				entry.setValue(1, packet.getField(PacketField.OLD_SRC));  // IP_CACHE
				dnsTable.storeEntry(entry);
				
				p.setField(PacketField.IP_DST, packet.getField(PacketField.OLD_SRC));
				p.setField(PacketField.PROTO, Packet.HTTP_REQUEST);
				
				return new RoutingResult(Action.FORWARD, p, externalInterface);
			}else if(packet.equalsField(PacketField.PROTO, Packet.HTTP_RESPONSE))
			{
				
				return new RoutingResult(Action.FORWARD, packet, internalInterface);
			}
			
		}
		return new RoutingResult(Action.DROP, null, null);
	}


	public void clearCdnCacheTable()
	{
		dnsTable.clear();
	}

}