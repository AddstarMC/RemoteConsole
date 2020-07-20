package au.com.addstar.rcon.objects;

import au.com.addstar.rcon.ConsoleScreen;
import jline.console.ConsoleReader;
import org.fusesource.jansi.Ansi;

import java.io.IOException;

/**
 * Created for AddstarMC.
 * Created by Narimm on 5/02/2018.
 */
public class TestConsole extends ConsoleScreen {

    private boolean mDebug = true;
    public  TestConsole(){
        super();
        setDaemon(false);
        setPromptText("Test");
    }


    @Override
    public void printString(String string) {
        try
        {
            StringBuffer buffer = new StringBuffer();
            int lines = getCursorLines();
            while (lines > 1)
            {
                buffer.append(Ansi.ansi().eraseLine(Ansi.Erase.ALL));
                buffer.append(ConsoleReader.RESET_LINE);
                buffer.append(Ansi.ansi().cursorUp(1));
                --lines;
            }

            buffer.append(Ansi.ansi().eraseLine(Ansi.Erase.ALL));
            buffer.append(ConsoleReader.RESET_LINE);

            buffer.append(string);
            buffer.append(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.DEFAULT));

            if (mDebug)
            {
                for (int i = 0; i < buffer.length(); ++i)
                {
                    if (!Character.isAlphabetic(buffer.charAt(i)) && !Character.isWhitespace(buffer.charAt(i)) && !Character.isDigit(buffer.charAt(i)))
                    {
                        String insert = String.format("\\%x", (int)buffer.charAt(i));
                        buffer.replace(i, i, insert);
                        i += insert.length();
                    }
                }
            }

            System.out.println(buffer.toString());

            mConsole.redrawLine();
            mConsole.flush();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void printErrString(String string) {
        try
        {
            StringBuffer buffer = new StringBuffer();
            int lines = getCursorLines();
            while (lines > 1)
            {
                buffer.append(Ansi.ansi().eraseLine(Ansi.Erase.ALL));
                buffer.append(ConsoleReader.RESET_LINE);
                buffer.append(Ansi.ansi().cursorUp(1));
                --lines;
            }

            buffer.append(Ansi.ansi().eraseLine(Ansi.Erase.ALL));
            buffer.append(ConsoleReader.RESET_LINE);

            buffer.append(string);
            buffer.append(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.DEFAULT));

            if (mDebug)
            {
                for (int i = 0; i < buffer.length(); ++i)
                {
                    if (Character.isISOControl(buffer.charAt(i)))
                    {
                        String insert = String.format("\\%x", (int)buffer.charAt(i));
                        buffer.replace(i, i, insert);
                        i += insert.length();
                    }
                }
            }

            System.err.println(buffer.toString());

            mConsole.redrawLine();
            mConsole.flush();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
