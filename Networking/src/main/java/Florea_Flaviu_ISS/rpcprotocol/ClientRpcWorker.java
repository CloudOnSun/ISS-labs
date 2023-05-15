package Florea_Flaviu_ISS.rpcprotocol;

import Florea_Flaviu_ISS.domain.*;
import Florea_Flaviu_ISS.service.IService;
import Florea_Flaviu_ISS.service.IServiceObserver;
import Florea_Flaviu_ISS.service.TeatruException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientRpcWorker implements Runnable, IServiceObserver {


    private IService server;
    private Socket connection;

    private ObjectInputStream input;
    private ObjectOutputStream output;

    private volatile boolean connected;

    public ClientRpcWorker(IService server, Socket connection) {
        this.server = server;
        this.connection = connection;
        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            connected = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (connected) {
            try {
                Object request = input.readObject();
                Response response = handleRequest((Request) request);
                if (response != null) {
                    sendResponse(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            input.close();
            output.close();
            connection.close();
        } catch (IOException e) {
            System.out.println("Error ---- " + e);
        }

    }

    private static Response okResponse = new Response.Builder().type(ResponseType.OK).build();

    private void sendResponse(Response response) throws IOException {
        synchronized (output) {
            output.writeObject(response);
            output.flush();
        }
    }

    @Override
    public void updateLocuri() throws TeatruException {

    }

    @Override
    public void updateSpectacole() throws TeatruException {
        Response resp = new Response.Builder().type(ResponseType.SPEC_ADDED).build();
        try {
            sendResponse(resp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Response handleRequest(Request request) {

        if (request.type() == RequestType.LOG_IN) {
            ManagerLogInDTO managerDTO = (ManagerLogInDTO) request.data();
            try {
                Manager admin = server.logIn(managerDTO, this);
                return new Response.Builder().type(ResponseType.LOGED_IN).data(admin).build();
            } catch (TeatruException e) {
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }
        if (request.type() == RequestType.LOG_OUT) {
            Manager manager = (Manager) request.data();
            try {
                server.logOut(manager, this);
                connected = false;
                return okResponse;
            } catch (TeatruException e) {
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }
        if (request.type() == RequestType.FIND_ALL_SPEC) {
            try {
                List<Spectacol> spectacols = server.findAllSpectacole(this);
                return new Response.Builder().type(ResponseType.GOT_ALL_SPEC).data(spectacols).build();
            } catch (TeatruException e) {
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }
        if (request.type() == RequestType.ADD_SPEC) {
            SpectacolDTO specDTO = (SpectacolDTO) request.data();
            try{
                server.addSpectacol(specDTO, this);
                return okResponse;
            } catch (TeatruException e){
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }
        if (request.type() == RequestType.FILTRE_SPEC) {
            Genuri gen = (Genuri) request.data();
            try {
                List<Spectacol> spectacols = server.filtruDupaGen(gen, this);
                return new Response.Builder().type(ResponseType.GOT_FILTRE_SPEC).data(spectacols).build();
            }
            catch (TeatruException e){
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }
        return null;
    }

}
