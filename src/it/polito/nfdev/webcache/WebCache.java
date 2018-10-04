package it.polito.nfdev.webcache;

import java.net.URL;
import java.util.List;

import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult.Action;
import it.polito.nfdev.lib.TableEntry;
import it.polito.nfdev.verification.Verifier;
import it.polito.nfdev.verification.Table;
import it.polito.nfdev.lib.RoutingResult;

public class WebCache extends NetworkFunction {
	
	private Interface internalFace;
	private Interface externalFace;

	
	@Table( fields = {"URL", "CONTENT"} )
	private CacheTable cacheTable;
	
	public WebCache(List<Interface> interfaces) {
		super(interfaces);
		assert interfaces.size() == 2;
		internalFace = null;
		externalFace = null;
		for(Interface i : interfaces)
		{
			if(i.getAttributes().contains(Interface.INTERNAL_ATTR))
				internalFace = i;
			if(i.getAttributes().contains(Interface.EXTERNAL_ATTR))
				externalFace = i;
		}
		assert internalFace != null;
		assert externalFace != null;
		assert internalFace.getId() != externalFace.getId();
		cacheTable = new CacheTable(2,0);
		cacheTable.setDataDriven();
	}

	@Override
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) {
		if(iface.isInternal())
		{
			if(packet.equalsField(PacketField.PROTO,Packet.HTTP_REQUEST)){
				TableEntry entry = cacheTable.matchEntry(packet.getField(PacketField.URL), Verifier.ANY_VALUE);
				if(entry != null)
				{
					Packet p = null;
					try {
						p = packet.clone();
						p.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC));
						p.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
						p.setField(PacketField.PORT_DST, packet.getField(PacketField.PORT_SRC));
						p.setField(PacketField.PORT_SRC, packet.getField(PacketField.PORT_DST));
						p.setField(PacketField.PROTO, Packet.HTTP_RESPONSE);
						p.setField(PacketField.URL, (String)entry.getValue(0));
						return new RoutingResult(Action.FORWARD, p, internalInterface);
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
						return new RoutingResult(Action.DROP, null, null);
					}
				}else
					return new RoutingResult(Action.FORWARD, packet, externalInterface);
			}
			return new RoutingResult(Action.DROP, null, null);
			
		}
		else
		{
			if(packet.equalsField(PacketField.PROTO,Packet.HTTP_RESPONSE)){
				try {
					Content content = new Content(new URL(packet.getField(PacketField.URL)));
					CacheTableEntry cacheEntry = new CacheTableEntry(2);
					cacheEntry.setValue(0, packet.getField(PacketField.URL));
					cacheEntry.setValue(1, content);
					cacheTable.storeEntry(cacheEntry);
					return new RoutingResult(Action.FORWARD, packet, internalInterface);
				} catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
			return new RoutingResult(Action.FORWARD, packet, internalInterface);
			
		}
		
	}

}