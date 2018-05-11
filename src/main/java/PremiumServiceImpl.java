import bank_service.PremiumService;
import org.apache.thrift.TException;

public class PremiumServiceImpl implements PremiumService.Iface {
    @Override
    public void takeLoan(String guid) throws TException {

    }

    @Override
    public double balance(String guid) throws TException {
        return 0;
    }
}
