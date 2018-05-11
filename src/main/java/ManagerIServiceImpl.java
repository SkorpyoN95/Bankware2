import bank_service.ManagerService;
import bank_service.User;
import org.apache.thrift.TException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ManagerIServiceImpl implements ManagerService.Iface {
    private Map<String, User> clients;

    ManagerIServiceImpl(Map<String, User> clients) {
        this.clients = clients;
    }

    @Override
    public String createAccount(User user) throws TException {
        String guid = user.pesel + LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        clients.put(guid, user);
        System.out.println(user.personal_info + " registered.");
        return guid;
    }
}
