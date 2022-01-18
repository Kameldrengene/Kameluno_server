import chat.LobbyManager;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.SpaceRepository;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        final String PORT = "9001";
        System.out.println("Server starting at " + PORT);
        SpaceRepository kamelUnoServerRepository = new SpaceRepository();
        SequentialSpace requestSpace = new SequentialSpace();
        SequentialSpace serverToLobbyManager = new SequentialSpace();
        kamelUnoServerRepository.add("requestSpace",requestSpace);
        kamelUnoServerRepository.addGate("tcp://server:" + PORT + "/?keep");
        List<String> players = new ArrayList<>();
        List<String> lobbies = new ArrayList<>();

        new Thread(new LobbyManager(serverToLobbyManager,kamelUnoServerRepository,players)).start();

        while (true){
            try {
                Object[] request = requestSpace.get(new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));
                String requestType = (String) request[0];
                String requestVerb = (String) request[1];
                String requestArgument = (String) request[2];
                System.out.println("Serving request " + requestType + " : " + requestVerb + " -> " + requestArgument);

                //TODO: Sender vi requesten kun til lobbymanager så skal denne switch case fjernes.

                switch (requestType){
                    case "lobby":
                        if(requestVerb.equals("getLobbies")){
                            System.out.println("Sending getlobbies to lobbymanager");
                            serverToLobbyManager.put(requestVerb, requestArgument);
                            Object[] response = serverToLobbyManager.get(new ActualField("getLobbies"),new FormalField(String[].class));
                            requestSpace.put(response[0],response[1]);
                            System.out.println("done");
                        }else {
                            serverToLobbyManager.put(requestVerb, requestArgument);
                            Object[] response = serverToLobbyManager.get(new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));
                            String the_if = (String) response[2];
                            if (the_if.equals("if")) {
                                Object[] response2client = serverToLobbyManager.get(new FormalField(String.class), new FormalField(String.class), new ActualField("oklobby"));
                                requestSpace.put(response2client[2]);
                            }else{
                                Object[] response2client = serverToLobbyManager.get(new FormalField(String.class), new FormalField(String.class), new ActualField("koybbol"));
                                requestSpace.put(response2client[2]);
                            }
                        }
                        break;
                    default:
                        System.out.println("request type not found");
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        //Server.getInstance().addChatRoom();
        //Server.getInstance().addChatRoom();
        //System.out.println("Hello from bottom")


    }
}