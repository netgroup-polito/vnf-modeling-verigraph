package it.polito.nfdev.webserver;

import java.util.ArrayList;

import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.RoutingResult;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult.Action;
import it.polito.nfdev.lib.Table;
import it.polito.nfdev.lib.TableEntry;

public class WebServer extends NetworkFunction {
	
	public static final String URL = "http://www.RightUrl.com/";
	public static final String WRONG_URL = "http://www.WrongUrl.com/";
	
	private Table urlList;
	
	public WebServer(){
		super(new ArrayList<Interface>());
		
		this.urlList = new Table(1, 0);
		this.urlList.setTypes(Table.TableTypes.Proto);
		TableEntry entry = new TableEntry(1);
		entry.setValue(0, URL);
		
		urlList.storeEntry(entry);
	}

	@Override
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) {
		
		Packet p;
		try {
			p = packet.clone();
		} catch (CloneNotSupportedException e) {	
			e.printStackTrace();
			return new RoutingResult(Action.DROP,null,null);
		}
		
		if(packet.equalsField(PacketField.PROTO, Packet.HTTP_REQUEST)){
		
			TableEntry entry = urlList.matchEntry(packet.getField(PacketField.URL));
			if(entry!=null){
				
				p.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
				p.setField(PacketField.PORT_SRC, packet.getField(PacketField.PORT_DST));
				p.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC));
				p.setField(PacketField.PORT_DST, packet.getField(PacketField.PORT_SRC));
				p.setField(PacketField.PROTO, Packet.HTTP_RESPONSE);
			
				
				return new RoutingResult(Action.FORWARD,p,iface);
			}
		
		}
		
		return new RoutingResult(Action.DROP,null,null);
	}

}
