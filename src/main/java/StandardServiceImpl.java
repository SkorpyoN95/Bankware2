import bank_service.StandardService;
import bank_service.User;
import org.apache.thrift.TException;

import java.util.Map;

public class StandardServiceImpl implements StandardService.Iface {
    private Map<String, User> clients;

    public StandardServiceImpl(Map<String, User> clients) {
        this.clients = clients;
    }

    @Override
    public double balance(String guid) throws TException {
        System.out.println(clients.get(guid).personal_info + " demanded his account balance.");
        return clients.get(guid).salary;
    }
}
