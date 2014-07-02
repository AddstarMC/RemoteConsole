package au.com.addstar.rcon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import au.com.addstar.rcon.network.packets.main.PacketOutMessage.MessageType;
import au.com.addstar.rcon.view.ConfigConsoleView;

public class ConfigLoader
{
	public static void loadConfig(File file) throws IOException
	{
		try
		{
			// Load the schema for validation
			InputStream schemaSource = ConfigLoader.class.getResourceAsStream("/config.xsd");
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			
			Schema schema = schemaFactory.newSchema(new StreamSource(schemaSource));
			
			// Load the config
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setSchema(schema);
			Document doc = factory.newDocumentBuilder().parse(file);
			parseAll(doc);
		}
		catch (SAXException e)
		{
			throw new IOException(e);
		}
		catch (ParserConfigurationException e)
		{
			throw new IOException(e);
		}
	}
	
	private static void parseAll(Document doc) throws SAXException
	{
		NodeList servers = doc.getDocumentElement().getElementsByTagName("servers");
		if(servers.getLength() > 0)
			parseServerList((Element)servers.item(0));
		
		NodeList views = doc.getDocumentElement().getElementsByTagName("views");
		if(views.getLength() > 0)
			parseViewsList((Element)views.item(0));
	}
	
	private static void parseServerList(Element root) throws SAXException
	{
		NodeList servers = root.getElementsByTagName("server");
		for(int i = 0; i < servers.getLength(); ++i)
		{
			Element server = (Element)servers.item(i);
			
			boolean reconnect = boolFromString(server.getAttribute("reconnect"));
			String fullHost = server.getTextContent();
			
			String host;
			int port = 22050;
			
			if(fullHost.contains(":"))
			{
				String[] split = fullHost.split(":");
				host = split[0];
				
				try
				{
					port = Integer.parseInt(split[1]);
					if(port <= 0 || port > 65535)
						throw new SAXException("Port number in " + fullHost + " is out of range");
				}
				catch(NumberFormatException e)
				{
					throw new SAXException("Port number in " + fullHost + " is not valid");
				}
			}
			else
				host = fullHost;
			
			ClientMain.getConnectionManager().addConnection(host, port, reconnect);
		}
	}
	
	private static void parseViewsList(Element root) throws SAXException
	{
		NodeList def = root.getElementsByTagName("default");
		NodeList views = root.getElementsByTagName("view");
		
		for(int i = 0; i < views.getLength(); ++i)
		{
			Element el = (Element)views.item(i);
			String name = el.getAttribute("name");
			ConfigConsoleView view = new ConfigConsoleView();
			
			NodeList filters = el.getElementsByTagName("filters");
			NodeList servers = el.getElementsByTagName("servers");
			
			if(filters.getLength() != 0)
				view.setFilter(parseFilters((Element)filters.item(0)));
			
			if(servers.getLength() != 0)
				parseViewServers((Element)servers.item(0), view);
			
			ClientMain.getViewManager().addView(name, view);
		}
		
		if(def.getLength() != 0)
		{
			String defaultView = def.item(0).getTextContent();
			try
			{
				ClientMain.getViewManager().setActive(defaultView);
			}
			catch(IllegalArgumentException e)
			{
				throw new SAXException("Unknown view " + defaultView);
			}
		}
	}
	
	private static EnumSet<MessageType> parseFilters(Element root)
	{
		EnumSet<MessageType> filters = EnumSet.noneOf(MessageType.class);
		
		NodeList nodes = root.getChildNodes();
		
		for(int i = 0; i < nodes.getLength(); ++i)
		{
			String name = nodes.item(i).getNodeName();
			
			for(MessageType type : MessageType.values())
			{
				if(type.name().equalsIgnoreCase(name))
				{
					filters.add(type);
					break;
				}
			}
		}
		
		return filters;
	}
	
	private static void parseViewServers(Element root, ConfigConsoleView view)
	{
		NodeList includes = root.getElementsByTagName("includes");
		NodeList excludes = root.getElementsByTagName("excludes");
		
		if(includes.getLength() != 0)
		{
			NodeList servers = ((Element)includes.item(0)).getElementsByTagName("server");
			for(int i = 0; i < servers.getLength(); ++i)
				view.includes(servers.item(i).getTextContent());
		}
		
		if(excludes.getLength() != 0)
		{
			NodeList servers = ((Element)excludes.item(0)).getElementsByTagName("server");
			for(int i = 0; i < servers.getLength(); ++i)
				view.excludes(servers.item(i).getTextContent());
		}
	}
	
	private static boolean boolFromString(String value)
	{
		return (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1"));
	}
}
