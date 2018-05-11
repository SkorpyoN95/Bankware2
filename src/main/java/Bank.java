import currency.*;
import bank_service.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import javafx.util.Pair;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Bank {
    private final ManagedChannel channel;
    private final CurrencyProviderGrpc.CurrencyProviderBlockingStub blockingStub;
    private final CurrencyProviderGrpc.CurrencyProviderStub asyncStub;

    private Map<Pair<Currency, Currency>, Double> currency_table = new HashMap<>();
    private Map<String, User> clients = new HashMap<>();

    public Bank(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
    }

    public Bank(ManagedChannelBuilder<?> o) {
        channel = o.build();
        blockingStub = CurrencyProviderGrpc.newBlockingStub(channel);
        asyncStub = CurrencyProviderGrpc.newStub(channel);
        new Thread(() -> {
            try {
                int port = 10000;
                TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
                TMultiplexedProcessor processor = new TMultiplexedProcessor();
                TServerTransport t = new TServerSocket(port);

                processor.registerProcessor("ManagerService", new ManagerService.Processor<>(new ManagerIServiceImpl(clients)));
                processor.registerProcessor("StandardService", new StandardService.Processor<>(new StandardServiceImpl(clients)));
                processor.registerProcessor("PremiumService", new PremiumService.Processor<>(new PremiumServiceImpl()));

                TServer server = new TSimpleServer(new TServer.Args(t).protocolFactory(protocolFactory).processor(processor));

                System.out.println("starting server port:" + port);
                server.serve();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    public void RegisterBank(String name, Currency main, Currency[] other){
        currency.Bank request = currency.Bank.newBuilder()
                                            .setName(name)
                                            .setMain(main)
                                            .addAllOther(Arrays.asList(other))
                                            .build();
        StreamObserver<CurrencyRate> responseStream = new StreamObserver<CurrencyRate>() {
            @Override
            public void onNext(CurrencyRate currencyRate) {
                currency_table.put(new Pair<>(currencyRate.getBase(), currencyRate.getTarget()), currencyRate.getRate());
                System.out.println(currencyRate.getBase() + "/" + currencyRate.getTarget() + " course updated!");
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("RPC ERROR");
            }

            @Override
            public void onCompleted() {
                System.out.println("That's unexpected...");
            }
        };
        asyncStub.registerBank(request, responseStream);
    }

    public static void main(String[] args){
        Bank bank = new Bank("0.0.0.0", 50051);
        bank.RegisterBank("PKO BP", Currency.PLN, new Currency[]{
                                                        Currency.CHF,
                                                        Currency.EUR,
                                                        Currency.USD,
                                                        Currency.GBP});
        while(true);
    }
}
