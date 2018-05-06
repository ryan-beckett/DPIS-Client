
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface StationServer extends Remote
{
	String createCRecord(String firstName, String lastName,
		String description, boolean captured) throws RemoteException;

	String createMRecord(String firstName, String lastName,
		String lastSeenAdress, String lastSeenDate, String lastSeenLocation,
		boolean found) throws RemoteException;

	boolean editCRecord(String recordID, boolean captured) throws RemoteException;

	String getRecordCounts() throws RemoteException;
}
