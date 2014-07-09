package au.com.addstar.rcon.view;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import au.com.addstar.rcon.ChatColor;
import au.com.addstar.rcon.util.Message;

public class MessageProcessor
{
	private ArrayList<Process> mProcessors;
	
	public MessageProcessor()
	{
		mProcessors = new ArrayList<Process>();
	}
	
	public MessageProcessor drop(String pattern) throws IllegalArgumentException
	{
		mProcessors.add(new DropProcess(pattern));
		return this;
	}
	
	public MessageProcessor replace(String pattern, String replace) throws IllegalArgumentException
	{
		mProcessors.add(new ReplaceProcess(pattern, replace));
		return this;
	}
	
	public MessageProcessor color(String pattern, Level level, String color) throws IllegalArgumentException
	{
		ChatColor c = ChatColor.getByName(color);
		if(c == null)
			throw new IllegalArgumentException("Unknown color " + color);
		
		mProcessors.add(new ColorProcess(pattern, level, c.toString()));
		return this;
	}
	
	public Message process(Message message)
	{
		for(Process process : mProcessors)
		{
			message = process.process(message);
			if(message == null)
				return null;
		}
		
		return message;
	}
	
	private static Pattern mMetaPattern = Pattern.compile("/(.*)/([imsxduU]*)");
	private static Pattern compilePattern(String pattern) throws IllegalArgumentException
	{
		Matcher matcher = mMetaPattern.matcher(pattern);
		
		if(!matcher.matches())
			throw new IllegalArgumentException("Illegal regex: " + pattern);
		
		int flags = 0;
		if(matcher.group(2) != null)
		{
			String flagString = matcher.group(2);
			for(int i = 0; i < flagString.length(); ++i)
			{
				char c = flagString.charAt(i);
				switch(c)
				{
				case 'i':
					flags |= Pattern.CASE_INSENSITIVE;
					break;
				case 'm':
					flags |= Pattern.MULTILINE;
					break;
				case 's':
					flags |= Pattern.DOTALL;
					break;
				case 'x':
					flags |= Pattern.COMMENTS;
					break;
				case 'd':
					flags |= Pattern.UNIX_LINES;
					break;
				case 'u':
					flags |= Pattern.UNICODE_CASE;
					break;
				case 'U':
					flags |= Pattern.UNICODE_CHARACTER_CLASS;
					break;
				}
			}
		}
		
		try
		{
			return Pattern.compile(matcher.group(1), flags);
		}
		catch(PatternSyntaxException e)
		{
			throw new IllegalArgumentException("Illegal regex: " + pattern);
		}
	}
	
	private interface Process
	{
		public Message process(Message message);
	}
	
	private static class DropProcess implements Process
	{
		private Pattern mPattern;
		
		public DropProcess(String pattern) throws IllegalArgumentException
		{
			mPattern = compilePattern(pattern);
		}
		
		@Override
		public Message process( Message message )
		{
			Matcher matcher = mPattern.matcher(message.getMessage());
			if(matcher.find())
				return null;
			else
				return message;
		}
	}
	
	private static class ReplaceProcess implements Process
	{
		private Pattern mPattern;
		private String mReplacement;
		
		public ReplaceProcess(String pattern, String replacement) throws IllegalArgumentException
		{
			mPattern = compilePattern(pattern);
			mReplacement = replacement;
		}
		
		@Override
		public Message process( Message message )
		{
			Matcher matcher = mPattern.matcher(message.getMessage());
			if(matcher.find())
				return message.copyAs(matcher.replaceAll(mReplacement));
			else
				return message;
		}
	}
	
	private static class ColorProcess implements Process
	{
		private Pattern mPattern;
		private Level mLevel;
		private String mColor;
		
		public ColorProcess(String pattern, Level level, String color)
		{
			if(!pattern.isEmpty())
				mPattern = compilePattern(pattern);
			
			mLevel = level;
			mColor = color;
		}
		
		@Override
		public Message process( Message message )
		{
			if(mLevel != null && message.getLevel() != mLevel)
				return message;

			if(mPattern != null)
			{
				Matcher matcher = mPattern.matcher(message.getMessage());
				if(matcher.find())
					return message.copyAs(mColor + message.getMessage());
			}
			
			return message;
		}
	}
}
