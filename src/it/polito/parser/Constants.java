package it.polito.parser;

public class Constants {
	
	//Methods
	public static final String MAIN_NF_METHOD = "onReceivedPacket";
	public static final String DEFINE_SENDING_PACKET_METHOD = "defineSendingPacket";
	public static final String ENTRY_SETTER = "setValue";
	public static final String ENTRY_GETTER = "getValue";
	public static final String SET_FIELD_METHOD = "setField";
	public static final String GET_FIELD_METHOD = "getField";
	public static final String ADD_INTERNAL_ADDRESS_METHOD = "addInternalAddress";
	public static final String SATISFY_METHOD_NAME = "satisfy";
	public static final String MATCH_ENTRY_METHOD_NAME = "matchEntry";
	public static final String SEND_METHOD_NAME = "sendPacket";
	public static final String STORE_ENTRY_METHOD_NAME = "storeEntry";
	public static final String DATA_DRIVEN = "setDataDriven";
	public static final String RECORD_PreviousPacket = "setRecordPreviousPacket";
	public static final String INDIRECT_NF = "setIndirectNF";
	public static final String IS_INTERNAL_METHOD = "isInternal";
	public static final String EQUALS_FIELD_METHOD = "equalsField";
	public static final String SET_TYPES = "setTypes";
	
	//Function' parameters
	public static final String INTERNAL_INTERFACE = "internalInterface";
	public static final String EXTERNAL_INTERFACE = "externalInterface";
	public static final String INITIAL_FORWARDING_INTERFACE = "initialForwardingInterface";
	public static final String INTERFACE_PARAMETER = "iface";
	public static final String PACKET_PARAMETER = "packet";
	public static final String DNS_RESPONSE = "DNS_RESPONSE";   //used at line573 in RuleContext.java
	public static final String HTTP_RESPONSE = "HTTP_RESPONSE";
	public static final String NULL = "null";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String NOTNULL = "notNull";
	
	//Types
	public static final String STRING_TYPE = "String";
	public static final String TABLE_ENTRY_TYPE = "TableEntry";
	public static final String PACKET_TYPE = "Packet";
	public static final String INTERFACE_TYPE = "Interface";
	public static final String TABLE_TYPE = "Table";
	public static final String ROUTING_RESULT_CLASS = "RoutingResult";
	
	//Packet fields
	public static final String IP_SOURCE = "IP_SRC";
	public static final String IP_DESTINATION = "IP_DST";
	public static final String PORT_SOURCE = "PORT_SRC";
	public static final String PORT_DESTINATION = "PORT_DST";
	public static final String PROTO = "PROTO";
	
	public static final String ORIGIN = "ORIGIN";
	public static final String ORIG_BODY = "ORIG_BODY";
	public static final String BODY = "BODY";	//= application data
	public static final String SEQUENCE = "SEQUENCE";
	public static final String EMAIL_FROM = "EMAIL_FROM";
	public static final String URL = "URL";
	public static final String OPTIONS = "OPTIONS";	
	public static final String INNER_SRC = "INNER_SRC";
	public static final String INNER_DEST = "INNER_DEST";
	public static final String ENCRYPTED = "ENCRYPTED";
	
	//Z3 packet fields

	public static final String Z3_IP_SOURCE = "src";
	public static final String Z3_IP_DESTINATION = "dest";
	public static final String Z3_PORT_SOURCE = "src_port";
	public static final String Z3_PORT_DESTINATION = "dst_port";
	public static final String Z3_PROTO = "proto";
	
	public static final String Z3_ORIGIN = "origin";
	public static final String Z3_ORIG_BODY = "orig_body";
	public static final String Z3_BODY = "body";
	public static final String Z3_SEQUENCE = "seq";
	public static final String Z3_EMAIL_FROM = "emailFrom";
	public static final String Z3_URL = "url";
	public static final String Z3_OPTIONS = "options";
	public static final String Z3_INNER_SRC = "inner_src";
	public static final String Z3_INNER_DEST = "inner_dest";
	public static final String Z3_ENCRYPTED = "encrypted";
	
	//Placeholder
	public static final String ANY_VALUE = "ANY_VALUE";
	public static final String PLACE_HOLDER = "placeholder";
	public static final String NONE = "None";
	public static final String INTERNAL = "internal";
	public static final String EXTERNAL = "external";
	
	//Field type enums
	
	public static final String ENUM_IP = "Ip";
	public static final String ENUM_PORT = "Port";
	public static final String ENUM_PROTO = "Proto";
	public static final String ENUM_BODY_DATA = "BodyData";
	public static final String ENUM_GENERIC = "Generic";
	public static final String ENUM_URL = "URL";
	
}
