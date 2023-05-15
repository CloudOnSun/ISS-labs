package Florea_Flaviu_ISS.server;

import Florea_Flaviu_ISS.domain.*;
import Florea_Flaviu_ISS.repository.IDateTimeRepository;
import Florea_Flaviu_ISS.repository.IManagerRepository;
import Florea_Flaviu_ISS.repository.ISpectacolRepository;
import Florea_Flaviu_ISS.service.IService;
import Florea_Flaviu_ISS.service.IServiceObserver;
import Florea_Flaviu_ISS.service.TeatruException;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class TeatruServiceImpl implements IService {

    IManagerRepository managerRepository;
    IDateTimeRepository dateTimeRepository;
    ISpectacolRepository spectacolRepository;

    private Set<IServiceObserver> loggedClients;

    public TeatruServiceImpl(IManagerRepository managerRepository, IDateTimeRepository dateTimeRepository, ISpectacolRepository spectacolRepository) {
        this.managerRepository = managerRepository;
        this.dateTimeRepository = dateTimeRepository;
        this.spectacolRepository = spectacolRepository;
        this.loggedClients = new HashSet<>();
    }

    @Override
    public synchronized Manager logIn(ManagerLogInDTO managerDTO, IServiceObserver client) throws TeatruException {
        String email = managerDTO.getEmail();
        String password = managerDTO.getPassword();
        var manager = managerRepository.logIn(email, password);
        if (manager != null) {
            loggedClients.add(client);
//            if (loggedClients.get(manager.getId()) != null)
//                throw new TeatruException("User already logged in!");
//            loggedClients.put(manager.getId(), client);
            return manager;
        } else {
            throw new TeatruException("Authentication failed!");
        }
    }

    @Override
    public synchronized List<Spectacol> filtruDupaGen(Genuri gen, IServiceObserver client) throws TeatruException {
        return spectacolRepository.filtrareDupaGen(gen);
    }

    @Override
    public synchronized List<Spectacol> findAllSpectacole(IServiceObserver client) throws TeatruException {
        loggedClients.add(client);
        return spectacolRepository.findAll();
    }

    @Override
    public synchronized void addSpectacol(SpectacolDTO spectacolDTO, IServiceObserver client) throws TeatruException {
        int idDate = 0;
        DateTime date = null;
        boolean done = false;
        while (!done) {
            idDate = ThreadLocalRandom.current().nextInt(0, 500000 + 1);
            date = new DateTime(spectacolDTO.getYear(), spectacolDTO.getMonth(), spectacolDTO.getDay(),
                    spectacolDTO.getHour(), spectacolDTO.getMinute());
            date.setId(idDate);
            try {
                dateTimeRepository.add(date);
                done = true;
            } catch (Exception ex) {

            }
        }

        int idSpec = 0;
        Spectacol spectacol = null;
        done = false;
        while (!done) {
            idSpec = ThreadLocalRandom.current().nextInt(0, 500000 + 1);
            spectacol = new Spectacol(spectacolDTO.getTitlu(), date, spectacolDTO.getRegizor(),
                    spectacolDTO.getGen(), spectacolDTO.getManager());
            spectacol.setId(idSpec);
            spectacol.setTeatru(spectacolDTO.getTeatru());
            try {
                spectacolRepository.add(spectacol);
                done = true;
            } catch (Exception ex) {

            }
        }
        spectacolDTO.getManager().addToSpectacole(spectacol);

        ExecutorService executor = Executors.newFixedThreadPool(5);
        for(var receiver : loggedClients) {
            executor.execute(() -> {
                try {
                    receiver.updateSpectacole();
                }
                catch (TeatruException e) {
                    System.out.println("ERROR NOTIFYING ADMINS");
                }
            });
        }
//        for (Integer key : loggedClients.keySet()) {
//            IServiceObserver receiver = loggedClients.get(key);
//            executor.execute(() -> {
//                try {
//                    receiver.updateSpectacole();
//                }
//                catch (TeatruException e) {
//                    System.out.println("ERROR NOTIFYING ADMINS");
//                }
//            });
//        }
    }

    @Override
    public synchronized void logOut(Manager manager, IServiceObserver client) throws TeatruException {
        //IServiceObserver localClient = loggedClients.remove(manager.getId());
        loggedClients.remove(client);
//        if (localClient == null) {
//            throw new TeatruException("User is not logged in!");
//        }
    }
}
