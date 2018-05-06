
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class FileLogger extends Logger
{
	private static final DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");

	public FileLogger(Object client, String filename) throws IOException, SecurityException
	{
		super(client.getClass().getName(), null);
		setUseParentHandlers(false);
		FileHandler handler = new FileHandler(filename, true);
		addHandler(handler);
		handler.setFormatter(new SimpleFormatter());
	}

	public void log(Level lvl, String formatStr, String... formatArgs)
	{
		String dt = dateFormat.format(new Date());
		super.log(lvl, dt + ": " + formatStr, formatArgs);
	}
}
