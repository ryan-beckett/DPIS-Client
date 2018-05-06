
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;

public class OfficerClient
{
	public static final String DEFAULT_REGISTRY_HOST = "localhost";
	public static final int DEFAULT_REGISTRY_PORT = 1100;
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
	private FileLogger logger;
	private Scanner kbd;
	private String host;
	private int port;
	private String badgeId;
	private String station;
	private StationServer server;

	public OfficerClient(String badgeId)
	{
		this(badgeId, DEFAULT_REGISTRY_HOST, DEFAULT_REGISTRY_PORT);
	}

	public OfficerClient(String badgeId, String host)
	{
		this(badgeId, host, DEFAULT_REGISTRY_PORT);
	}

	public OfficerClient(String badgeId, int port)
	{
		this(badgeId, DEFAULT_REGISTRY_HOST, port);
	}

	public OfficerClient(String badgeId, String host, int port)
	{
		this.badgeId = badgeId;
		this.host = host;
		this.port = port;
		try {
			logger = new FileLogger(this, badgeId + ".log");
		} catch (IOException | SecurityException ex) {
			System.err.println("Couldn't create log file.");
		}
		kbd = new Scanner(System.in);
		int end;
		for (end = 0; end < badgeId.length(); end++)
			if (Character.isDigit(badgeId.charAt(end)))
				break;
		station = badgeId.substring(0, end);
	}

	public void run()
	{
		try {
			Registry registry = LocateRegistry.getRegistry(host, port);
			server = (StationServer) registry.lookup(station + "StationServer");
			String msg = "Retrieved "+station+"StationServer object successfully.";
			logger.log(Level.INFO, msg, (String[])null);
			System.out.println(msg);
		} catch (RemoteException | NotBoundException ex) {
			String msg = "Could not find "+station+"StationServer object. Exiting.";
			logger.log(Level.SEVERE, msg, (String[]) null);
			System.err.println(msg);
			System.exit(1);
		}
		uiLoop();
		kbd.close();
		String msg = "Client terminated by user.";
		logger.log(Level.INFO, msg, (String[]) null);
		System.out.println(msg);
	}

	private void uiLoop()
	{
		try {
			int choice;
			boolean exit = false;
			System.out.println("\nWelcome officer " + badgeId + "!");
			while (!exit) {
				showMenu();
				try {
					choice = Integer.parseInt(kbd.nextLine());
					System.out.println();
					switch (choice) {
						case 0:
							uiCreateCRecord();
							break;
						case 1:
							uiCreateMRecord();
							break;
						case 2:
							uiGetRecordCounts();
							break;
						case 3:
							uiEditCRecord();
							break;
						case 4:
							exit = true;
							break;
						default:
							System.out.println("Invalid choice.");
					}
				} catch (NumberFormatException ex) {
					System.out.println("Please enter an integer.");
				}
			}
		} catch (RemoteException ex) {
			String msg = station + "StationServer error: " + ex.getMessage() + ". Exiting.";
			logger.log(Level.SEVERE, msg, (String[]) null);
			System.err.println(msg);
			System.exit(1);
		}
	}

	private void uiCreateCRecord() throws RemoteException
	{
		String firstName = promptUserString("Enter first name: ");
		if (firstName == null)
			return;
		String lastName = promptUserString("Enter last name: ");
		if (lastName == null)
			return;
		String description = promptUserString("Enter crime desciption: ");
		if (description == null)
			return;
		Character status = promptUserCharacter("Enter status ('C' = Captured / 'R' = On the run): ");
		if (status == null)
			return;
		boolean captured;
		if (status == 'c')
			captured = true;
		else if (status == 'r')
			captured = false;
		else {
			System.out.println("Please choose 'C' or 'R'.");
			return;
		}
		String newRecordId = server.createCRecord(firstName, lastName, description, captured);
		String[] args = {
			firstName, lastName, description, captured + "", (newRecordId != null) ? "success" : "fail"
		};
		logger.log(Level.INFO, "Request = createCRecord({0}, {1}, {2}, {3}), Response = {4}.", args);
		if (newRecordId != null)
			System.out.println("New criminal record " + newRecordId + " was created.");
		else
			System.out.println("New criminal record could not be created.");
	}

	private void uiCreateMRecord() throws RemoteException
	{
		String firstName = promptUserString("Enter first name: ");
		if (firstName == null)
			return;
		String lastName = promptUserString("Enter last name: ");
		if (lastName == null)
			return;
		String lastAddress = promptUserString("Enter last seen address: ");
		if (lastAddress == null)
			return;
		String lastDate = promptUserString("Enter last seen date (e.g. 09/20/11): ");
		if (lastDate == null)
			return;
		Date dt;
		try {
			dt = dateFormat.parse(lastDate);
			for(String tok : lastDate.split("/"))
				if(tok.length() > 2)
					throw new ParseException(null, 0);
		} catch (ParseException ex) {
			System.out.println("Enter a valid date.");
			return;
		}
		String lastLocation = promptUserString("Enter last seen location: ");
		if (lastLocation == null)
			return;
		String newRecordId = server.createMRecord(
			firstName, lastName, lastAddress, lastDate, lastLocation, false);
		String[] args = {
			firstName, lastName, lastAddress, dt.toString(), lastLocation, "false",
			(newRecordId != null) ? "success" : "fail"
		};
		logger.log(Level.INFO, "Request = createMRecord({0}, {1}, {2}, {3}, {4}, {5}), Response = {6}.", args);
		if (newRecordId != null)
			System.out.println("New missing record " + newRecordId + " was created.");
		else
			System.out.println("New missing record could not be created.");
	}

	private void uiGetRecordCounts() throws RemoteException
	{
		String resp = server.getRecordCounts();
		String[] args = {resp};
		logger.log(Level.INFO, "Request = getRecordCounts, Response = {0}", args);
		System.out.println(resp);
	}

	private void uiEditCRecord() throws RemoteException
	{
		String recordId = promptUserString("Enter record id: ");
		if (recordId == null)
			return;
		Character status = promptUserCharacter("Enter status ('C' = Captured / 'R' = On the run): ");
		if (status == null)
			return;
		boolean captured;
		if (status == 'c')
			captured = true;
		else if (status == 'r')
			captured = false;
		else {
			System.out.println("Please choose 'C' or 'R'.");
			return;
		}
		boolean success = server.editCRecord(recordId, captured);
		String[] args = {
			recordId, captured + "", (success) ? "success" : "fail"
		};
		logger.log(Level.INFO, "Request = editCRecord({0}, {1}), Response = {2}.", args);
		if (success)
			System.out.println("Record " + recordId + "'s status is updated");
		else
			System.out.println("Record " + recordId + " could not be updated.");
	}

	private Integer promptUserInteger(String prompt)
	{
		System.out.print(prompt);
		try {
			return Integer.parseInt(kbd.nextLine());
		} catch (NumberFormatException ex) {
			System.out.println("Please enter an integer.");
		}
		return null;
	}

	private String promptUserString(String prompt)
	{
		System.out.print(prompt);
		String input = kbd.nextLine();
		if (input == null || input.trim().length() == 0) {
			System.out.println("Please enter some input.");
			return null;
		}
		return input.trim();
	}

	private Character promptUserCharacter(String prompt)
	{
		System.out.print(prompt);
		String input = kbd.nextLine();
		if (input == null || input.trim().length() == 0) {
			System.out.println("Please enter some input.");
			return null;
		} else if (input.trim().length() > 1) {
			System.out.println("Please enter a single character.");
			return null;
		}
		return Character.toLowerCase(input.charAt(0));
	}

	private void showMenu()
	{
		System.out.println("\n0: Create a criminal record");
		System.out.println("1: Create a missing person record");
		System.out.println("2: Get station-wide record count");
		System.out.println("3: Edit criminal record status");
		System.out.println("4: Exit");
		System.out.print("Enter action: ");
	}

	public static void main(String[] args)
	{
		String host = DEFAULT_REGISTRY_HOST;
		int port = DEFAULT_REGISTRY_PORT;
		String badgeId = null;
		if (args.length == 0)
			printUsage();
		if (args.length >= 1) {
			if (args[0].equals("-help"))
				printUsage();
			badgeId = args[0];
		}
		if (args.length >= 2)
			host = args[1];
		if (args.length >= 3)
			try {
				port = Integer.parseInt(args[2]);
			} catch (NumberFormatException ex) {
				System.err.println("Error: Illegal port number. Exiting.");
				System.exit(1);
			}
		new OfficerClient(badgeId, host, port).run();
	}

	private static void printUsage()
	{
		System.err.println("Usage: java OfficerClient [-help] <BadgeID> [<host>] [<port>]");
		System.err.println("\t-- <BadgeID> - The officer's badge identifier. The identifier is");
		System.err.println("\t\t\tthe station acronym followed by a 4 digit number.");
		System.err.println("\t-- <host> - The hostname where the registry server runs.");
		System.err.println("\t-- <port> - The port where the registry server runs on.");
		System.err.println("If the -help flag should be used alone.");
		System.err.println("If a port is specified a hostname must be as well.");
		System.err.println("If no hostname is specified, it defaults to localhost.");
		System.err.println("If no port is specified, it defaults to the default registry port (1100).");
		System.exit(0);
	}
}
