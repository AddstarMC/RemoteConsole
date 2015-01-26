package au.com.addstar.rcon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.logging.Level;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

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
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			ConfigErrorHandler handler = new ConfigErrorHandler();
			builder.setErrorHandler(handler);
			
			Document doc = builder.parse(file);
			
			if(handler.errored)
				throw new IOException("Errors occured during parsing");
			
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
			String name = server.getAttribute("name").trim();
			String fullHost = server.getTextContent();
			
			if (name.isEmpty())
				name = null;
			
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
			
			if (name != null)
				ClientMain.getConnectionManager().setAlias(host, port, name);
			ClientMain.getConnectionManager().addConnection(host, port, reconnect, name);
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
			NodeList layout = el.getElementsByTagName("layout");
			NodeList format = el.getElementsByTagName("format");
			
			if(filters.getLength() != 0)
				view.setFilter(parseFilters((Element)filters.item(0)));
			
			if(servers.getLength() != 0)
				parseViewServers((Element)servers.item(0), view);
			
			if(layout.getLength() != 0)
				view.getBuffer().setOverrideFormat(((Element)layout.item(0)).getAttribute("format"));
			
			if(format.getLength() != 0)
				parseViewFormatter((Element)format.item(0), view);
			
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
	
	private static void parseViewFormatter(Element root, ConfigConsoleView view)
	{
		NodeList children = root.getChildNodes();
		
		for(int i = 0; i < children.getLength(); ++i)
		{
			if(!(children.item(i) instanceof Element))
				continue;
			
			Element element = (Element)children.item(i);
			if(element.getTagName().equals("drop"))
				view.getProcessor().drop(element.getAttribute("pattern"));
			else if(element.getTagName().equals("replace"))
				view.getProcessor().replace(element.getAttribute("pattern"), element.getTextContent());
			else if(element.getTagName().equals("color"))
			{
				Level level = (element.hasAttribute("level") ? Level.parse(element.getAttribute("level")) : null);
				view.getProcessor().color(element.getAttribute("pattern"), level, element.getTextContent());
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
	
	private static class ConfigErrorHandler implements ErrorHandler
	{
		public boolean errored = false;
		
		@Override
		public void warning( SAXParseException exception ) throws SAXException
		{
			System.err.println("WARN: " + exception.getMessage());
		}
		
		@Override
		public void fatalError( SAXParseException exception ) throws SAXException
		{
			errored = true;
			System.err.println("FATAL: " + exception.getMessage());
		}
		
		@Override
		public void error( SAXParseException exception ) throws SAXException
		{
			errored = true;
			System.err.println("ERROR: " + exception.getMessage());
		}
	}
}
