package Florea_Flaviu_ISS.service;

import Florea_Flaviu_ISS.domain.*;

import java.util.List;

public interface IService {

    public Manager logIn(ManagerLogInDTO managerDTO, IServiceObserver client) throws TeatruException;

    public List<Spectacol> filtruDupaGen(Genuri gen, IServiceObserver client) throws TeatruException;

    public List<Spectacol> findAllSpectacole(IServiceObserver client) throws TeatruException;

    public void addSpectacol(SpectacolDTO spectacolDTO, IServiceObserver client) throws TeatruException;

    public void logOut(Manager manager, IServiceObserver client) throws TeatruException;
}
