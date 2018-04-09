package it.polito.nfdev.vCDN;

import java.util.List;
import java.net.MalformedURLException;
import java.net.URL;
import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult;
import it.polito.nfdev.lib.RoutingResult.Action;
import it.polito.nfdev.lib.TableEntry;
import it.polito.nfdev.verification.Verifier;
import it.polito.nfdev.webcache.*;
import it.polito.nfdev.verification.Table;
import it.polito.nfdev.nat.PortPool;

public class CDNcache extends NetworkFunction {

	private String webServer_Ip;

	private Interface internalFace;
	private Interface externalFace;
	
	@Table( fields = {"URL", "CONTENT"} )
	private CacheTable cdnCacheTable;
	
	public CDNcache(List<Interface> interfaces, String webServer_Ip) {
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
		
		this.cdnCacheTable = new CacheTable(2,0);  // URL, CONTENT
		this.cdnCacheTable.setDataDriven();
		this.webServer_Ip = webServer_Ip;
		
	}
	
	@Override
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) {		
		Packet p = null;
		try {
			p = packet.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return new RoutingResult(Action.DROP, null, null);
		}
	
			if(packet.equalsField(PacketField.PROTO,Packet.HTTP_REQUEST))
			{
				TableEntry entry = cdnCacheTable.matchEntry(packet.getField(PacketField.URL));				
				
				if(entry != null)   
				{
					p.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC));
					p.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
					p.setField(PacketField.PROTO, Packet.HTTP_RESPONSE);
				
					return new RoutingResult(Action.FORWARD, p, iface); 
				
				}
				else 
				{
					p.setField(PacketField.IP_DST, webServer_Ip);
			//		p.setField(PacketField.OLD_DST, packet.getField(PacketField.IP_DST));
				
					return new RoutingResult(Action.FORWARD, p, iface); 
				}
			}
				
			if(packet.equalsField(PacketField.PROTO,Packet.HTTP_RESPONSE)){
				
					Content content;
					try {
						content = new Content(new URL(packet.getField(PacketField.URL)));
					
					CacheTableEntry newEntry = new CacheTableEntry(2);
					newEntry.setValue(0, packet.getField(PacketField.URL));
					newEntry.setValue(1, content);
					cdnCacheTable.storeEntry(newEntry);
					} catch (Exception e) {
						e.printStackTrace();
					}
				
					return new RoutingResult(Action.FORWARD, p, internalInterface); 
					
				
			}
			return new RoutingResult(Action.DROP, null, null);
			
		}
		
	
	
	public void clearCdnCacheTable(){
		cdnCacheTable.clear();
	}

}
