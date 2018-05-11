import bank_service.ManagerService;
import bank_service.PremiumService;
import bank_service.StandardService;
import bank_service.User;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class Client {
    private String name_surname;
    private String pesel;
    private double salary;
    private String guid;
    private boolean logged_in = false;

    public void register(String name_surname, String pesel, double salary){
        this.name_surname = name_surname;
        this.pesel = pesel;
        this.salary = salary;
    }

    public void logIn(String guid){
        this.guid = guid;
        this.logged_in = true;
    }

    public void logOut(){
        this.logged_in = false;
        this.guid = null;
    }

    public static void main(String argv[]){
        String host = "localhost";

        TSocket socket = new TSocket(host, 9091);
        TTransport transport = socket;
        TProtocol protocol = new TBinaryProtocol(transport, true, true);

        ManagerService.Client manager = new ManagerService.Client(new TMultiplexedProtocol(protocol, "ManagerService"));
        StandardService.Client standard = new StandardService.Client(new TMultiplexedProtocol(protocol, "StandardService"));
        PremiumService.Client premium = new PremiumService.Client(new TMultiplexedProtocol(protocol, "PremiumService"));

        String line = null;
        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
        Client cli = new Client();
        System.out.println("Welcome in Bankware!");
        do{
            try {
                if (!cli.logged_in) {
                    System.out.println("register - to register new user");
                    System.out.println("login - to log in your account");
                    System.out.println("end - to quit");

                    line = in.readLine();
                    if(line.equals("register")){
                        System.out.println("We need some yours data.");
                        System.out.println("Put your full name, please:");
                        String full_name = in.readLine();
                        System.out.println("Enter your pesel now:");
                        String pesel = in.readLine();
                        System.out.println("At last we need your monthly income:");
                        double salary = Double.parseDouble(in.readLine());
                        User user = new User();
                        user.personal_info = full_name;
                        user.pesel = pesel;
                        user.salary = salary;
                        transport.open();
                        String guid = manager.createAccount(user);
                        transport.close();
                        System.out.println("This is your GUID: " + guid + ".");
                        System.out.println("You can use it to log in.");
                    }
                    if(line.equals("login")){
                        System.out.println("Put your GUID to log in: ");
                        String guid = in.readLine();
                        cli.logIn(guid);
                    }
                }
                else{
                    System.out.println("balance - to get your account balance");
                    System.out.println("loan - unavailaible for standard account");
                    System.out.println("end - to logout");

                    line = in.readLine();
                    if(line.equals("balance")){
                        transport.open();
                        double balance = standard.balance(cli.guid);
                        transport.close();
                        System.out.println("You have " + balance + " on account!");
                    }
                    if(line.equals("end")){
                        cli.logOut();
                        line = "";
                    }
                }
            }
            catch(Exception e){
                System.err.println(e);
            }
        }while(!line.equals("end"));
    }
}
