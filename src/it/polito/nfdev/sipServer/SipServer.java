package it.polito.nfdev.sipServer;

import java.util.List;


import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult.Action;
import it.polito.nfdev.lib.Table;
import it.polito.nfdev.lib.TableEntry;
import it.polito.nfdev.verification.Verifier;
import it.polito.parser.Constants;
import it.polito.nfdev.lib.RoutingResult;

public class SipServer extends NetworkFunction {
	
	public static final String  SIP_INVITE = "Sip_Invite";
	public static final String  SIP_INVITE_OK = "Sip_Invite_OK";
	public static final String  SIP_REGISTE_OK = "Sip_Registe_OK";
	public static final String  SIP_REGISTE = "Sip_Registe";
	public static final String  SIP_END = "Sip_End";
	
	private Table sipTable;
	private String ip_sipServer;
	private String domain;
	private String ip_dns;

	public SipServer(List<Interface> interfaces, String ip_sipServer, String domain, String ip_dns) {
		super(interfaces);
		this.ip_sipServer = ip_sipServer;
		sipTable = new Table(2,0);
		sipTable.setTypes(Table.TableTypes.BodyData, Table.TableTypes.Ip); //BodyData stores the account number of callee.
		sipTable.setDataDriven();
		this.domain = domain;
		this.ip_dns = ip_dns;
	}

	@Override
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) {
		  
	
		Packet p = null;
		try {
			p = packet.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		if(packet.equalsField(PacketField.PROTO, SIP_REGISTE) && packet.equalsField(PacketField.IP_DST, ip_sipServer) && packet.equalsField(PacketField.URL, domain))
		{
			
			TableEntry entry = new TableEntry(2);
			entry.setValue(0, packet.getField(PacketField.BODY));
			entry.setValue(1, packet.getField(PacketField.IP_SRC));
			sipTable.storeEntry(entry);
			
			p.setField(PacketField.IP_SRC,packet.getField(PacketField.IP_DST));
			p.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC));
		
			p.setField(PacketField.PROTO, SIP_REGISTE_OK);
			
			return new RoutingResult(Action.FORWARD,p,iface);
		
			
		}
		
		if(packet.equalsField(PacketField.PROTO, SIP_INVITE) && packet.equalsField(PacketField.IP_DST, ip_sipServer) && packet.equalsField(PacketField.URL, domain))
		{
			
			if(packet.equalsField(PacketField.URL, domain)){
				TableEntry entry = sipTable.matchEntry(packet.getField(PacketField.BODY), Verifier.ANY_VALUE);
				if(entry != null)
				{						
					p.setField(PacketField.IP_DST, (String)entry.getValue(1));  // second place is the Callee IP
					p.setField(PacketField.URL, domain);
					return new RoutingResult(Action.FORWARD,p,iface);
		
				}
			}
			else{
				p.setField(PacketField.IP_DST, searchIP(packet.getField(PacketField.URL),ip_sipServer, ip_dns));  // second place is the Callee IP
				p.setField(PacketField.URL,packet.getField(PacketField.URL));
				p.notEqualsField(PacketField.URL, domain);
				return new RoutingResult(Action.FORWARD,p,iface);
	
			}
		}
		
	/*	
		if(packet.equalsField(PacketField.PROTO, SIP_INVITE_OK))
		{
			p.setField(PacketField.PROTO,SIP_INVITE_OK);
			return new RoutingResult(Action.FORWARD,p,iface);
		}
		if(packet.equalsField(PacketField.PROTO, SIP_END))
		{
			p.setField(PacketField.PROTO,SIP_END);
			return new RoutingResult(Action.FORWARD,p,iface);
		}
	*/	
		return new RoutingResult(Action.DROP,null,null);

	}

	private String searchIP(String url,String ip_sipServer, String ip_dns) {
		/*Packet p = new Packet();
		p.setField(PacketField.IP_SRC, ip_sipServer);
		p.setField(PacketField.IP_DST, ip_dns);
		p.setField(PacketField.URL, url);
		p.setField(PacketField.PROTO, Constants.DNS_REQUEST);
		*/
		return new String("ip_"+url+"_sendFrom_"+ip_sipServer+"_to_"+ip_dns);
	}
}
