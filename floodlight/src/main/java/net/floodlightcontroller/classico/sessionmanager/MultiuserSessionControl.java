package net.floodlightcontroller.classico.sessionmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;

import net.floodlightcontroller.classico.pathscontrol.CandidatePath;
import net.floodlightcontroller.classico.pathscontrol.Monitor;
import net.floodlightcontroller.classico.pathscontrol.MultipathSession;
import net.floodlightcontroller.routing.IRoutingService.PATH_METRIC;

public class MultiuserSessionControl {
	
	private int id;
	private ServerSession serverSession;
	private List<Session> listSessions;
	private Date timeInit; 
	private Date timeUpdate;
	
	private int countUsers = 1;
	
	List<MultipathSession> multipathSessions = new ArrayList<>();
	private Monitor monitor;
	
	public MultiuserSessionControl(ServerSession serverSession){
		this.timeInit = new Date();
		this.listSessions = new ArrayList<>();
		this.serverSession = serverSession;
	}
	
	
	/**
	 * MULTIUSER CLOUD SESSION CONTROL ALGORITHM
	 * @param srcIp Client Ip
	 * @param srcPort Source process port
	 * @param dstIp IP of the registered server (useful for validation)
	 * @param dstPort Port of the registered server (useful for validation)
	 * @param macAddress 
	 * @param service Required service (message body)
	 * @param datapathId Edge switch that generated packet-in
	 */
	public boolean addClientRequest(IPv4Address srcIp, TransportPort srcPort, IPv4Address dstIp, TransportPort dstPort,
			MacAddress macAddress, String service, DatapathId datapathId, OFPort switchInPort) {
		
		/*Treats the body of the service message for a type that will define the session*/
		ISessionCondition sessionCond = new SessionCondition(service);
		
		/*Creates a session for the User (not yet available in the table)*/
		UserSession userSession = new UserSession(srcIp, srcPort, dstIp, dstPort, macAddress, datapathId, switchInPort);
		
		/*Scroll through list of active table sessions*/
		for(Session sm : listSessions){
			
			/*Checks the session condition, that is, if the requested service is part of an active session*/
			if(sm.getSessionCondition().verify(sessionCond)){
				
				/*Checks whether the user's host already exists in the session*/				
				if(sm.userSessionExists(userSession)){
					//TODO Remove (using for testing)
//					System.out.println("User exists in session!");
					
					return true;
					
				}else{
					/*Insert ID in User Session*/
					userSession.setIdUser(countUsers++);
					
					/*Add User Session in Session MultiUser*/
					sm.addUser(userSession);
					
					List<CandidatePath> paths = monitor.calculatePaths(userSession, serverSession.getDatapathId(), datapathId, PATH_METRIC.UTILIZATION);
					multipathSessions.add(new MultipathSession(paths, userSession, serverSession, sm));
					monitor.alertUpdate();
					//show();
					System.out.println("[MultiuserSessionControl] User Session Created e added to Session "+
							+sm.getId()+": "+
							+userSession.getIdUser()+", "+userSession.getIp()+":"+userSession.getPort());
					return false;
				}
			}
		}
		
		/*This will only run if the service that creates the session does not yet exist. In this case, a new session will 
		 * be created for the service, and the User Session will be added to the session.*/
		Session smu = new Session(listSessions.size(), listSessions.size()+" "+service, sessionCond);
		userSession.setIdUser(countUsers++);
		smu.addUser(userSession);
		listSessions.add(smu);
		System.out.println("[MultiuserSessionControl] Session Created: "
				+smu.getId()+", "+smu.getDescription());
		
		List<CandidatePath> paths = monitor.calculatePaths(userSession, serverSession.getDatapathId(), datapathId,  null);
		multipathSessions.add(new MultipathSession(paths, userSession, serverSession, smu));
//		show();
		monitor.alertUpdate();
		System.out.println("[MultiuserSessionControl] User Session Created: "
				+userSession.getIdUser()+", "+userSession.getIp()+":"+userSession.getPort());
		return true;
	} 
	
	
	
//	public void initMonitor(IRoutingService routingService, IOFSwitchService switchService, ILinkDiscoveryService linkDiscoveryService,
//			IStatisticsService statisticsService) {
//		this.monitor = new Monitor(this, routingService, switchService, linkDiscoveryService, statisticsService);
//		monitor.start();
//		
//	}
	
	public void show(){
		System.out.println("------------ Table Sessions ----------");
		for (Session smu : getListSessions()) {
			System.out.println(smu.toString());
		}
		System.out.println("------------ Candidate Paths Table ----------");
		for (MultipathSession mps : getMultipathSessions()) {
			System.out.println(mps.toString());
			
			for (CandidatePath cp : mps.getPaths()) {
				String id = cp.getId().getSrc().toString();
				System.out.print("	"+"Candidate Path: "+id.substring(id.length()-2, id.length())+" -> ");
				for (int i = 1; i <  cp.getPath().size()-1; i+=2) {
					id = cp.getPath().get(i).getNodeId().toString();
					System.out.print(id.substring(id.length()-2, id.length())+" -> ");
				}
				id = cp.getId().getDst().toString();
				System.out.println(id.substring(id.length()-2, id.length()));
				System.out.println("		Bandwidth Consumption: "+cp.getBandwidthConsumption()+"bps");
				System.out.println("		Latency: "+cp.getLatency().getValue());
				System.out.println("		Hop Count: "+cp.getHopCount());
			}
			
		}
		System.out.println("----------------------------------------------");
	}
	
	@Override
	public String toString() {
		String texto = "";
		for(Session sm : listSessions){
			texto += sm.getId() + " - ";
			texto += sm.getDescription()+ " - ";
			texto += sm.getListUser().toString() + "\n";
		}
		return texto;
	}
	
	public List<MultipathSession> getMultipathSessions() {
		return multipathSessions;
	}

	public void setMultipathSessions(List<MultipathSession> multipathSessions) {
		this.multipathSessions = multipathSessions;
	}

//	public Monitor getMonitor() {
//		return monitor;
//	}
//
//	public void setMonitor(Monitor monitor) {
//		this.monitor = monitor;
//	}

	public int getId() {
		return id;
	}
	
	public void addSession(Session session){
		listSessions.add(session);
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<Session> getListSessions() {
		return listSessions;
	}

	public void setListSessions(List<Session> listSessions) {
		this.listSessions = listSessions;
	}

	public Date getTimeInit() {
		return timeInit;
	}

	public void setTimeInit(Date timeInit) {
		this.timeInit = timeInit;
	}

	public Date getTimeUpdate() {
		return timeUpdate;
	}

	public void setTimeUpdate(Date timeUpdate) {
		this.timeUpdate = timeUpdate;
	}
	
	

	public ServerSession getServerSession() {
		return serverSession;
	}

	public void setServerSession(ServerSession serverSession) {
		this.serverSession = serverSession;
	}


	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
		
	}
	
	
}
