package com.codexsoft.sas.connections.iom;

import com.codexsoft.sas.connections.ConnectionProperties;
import com.sas.meta.SASOMI.IOMI;
import com.sas.meta.SASOMI.ISecurity;
import com.sas.meta.SASOMI.ISecurityAdmin;
import com.sas.metadata.remote.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.rmi.RemoteException;


@Component
@Scope("prototype")
@Slf4j
public class IOMConnection implements AutoCloseable {

    private final ConnectionProperties connection;

    public IOMConnection(ConnectionProperties connection) {
        this.connection = connection;
    }

    private MdFactory mdFactory;

    public MdFactory getMdFactory() throws Exception {
        if (mdFactory == null) {
            try {
                mdFactory = new MdFactoryImpl(true);
                MdOMRConnection omrConnection = mdFactory.getConnection();
                omrConnection.makeOMRConnection(
                        connection.getHost(),
                        Integer.toString(connection.getPort()),
                        connection.getUserName(),
                        connection.getPassword()
                );
            } catch (MdException e) {
                log.error("Error occurred while connecting to SAS: {}", e.getMessage(), e);

                Throwable t = e.getCause();
                if (t == null) {
                    String ErrorType = e.getSASMessageSeverity();
                    String ErrorMsg = e.getSASMessage();
                    String causeMsg = "";
                    if (t instanceof org.omg.CORBA.COMM_FAILURE || t instanceof org.omg.CORBA.NO_PERMISSION) {
                        causeMsg = e.getLocalizedMessage();
                    }
                    throw new Exception(ErrorType + ": " + ErrorMsg + "; " + causeMsg);
                } else {
                    throw new Exception(e.getLocalizedMessage());
                }
            }
        }
        return mdFactory;
    }

    public void close() throws RemoteException {
        mdFactory.closeOMRConnection();
    }

    public IOMI getIOMIConnection() throws Exception {
        MdFactory mdFactory = getMdFactory();
        return mdFactory.getConnection().getCMRHandle();
    }

    public MdOMIUtil getOMIUtil() throws Exception {
        MdFactory mdFactory = getMdFactory();
        return mdFactory.getOMIUtil();
    }

    public ISecurityAdmin getISecurityAdmin() throws Exception {
        MdOMRConnection omrConnection = getMdFactory().getConnection();
        IOMI iOMI = getIOMIConnection();
        return omrConnection.MakeISecurityAdminConnection(iOMI);
    }

    public ISecurity getISecurity() throws Exception {
        MdOMRConnection omrConnection = getMdFactory().getConnection();
        IOMI iOMI = getIOMIConnection();
        return omrConnection.MakeISecurityConnection();
    }
}
