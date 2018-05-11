const fixer = require("fixer-api");
const grpc = require("grpc");
const proto = grpc.load("src/main/proto/currency.proto");
const server = new grpc.Server();

//currencies' "value" based on KRW rate (South Korea won)
let currencies = {
    PLN: 0.0,
    EUR: 0.0,
    USD: 0.0,
    GBP: 0.0,
    CHF: 0.0,
    RUB: 0.0,
    CNY: 0.0,
    JPY: 0.0
};

let currencies_banks = {
    PLN: [],
    EUR: [],
    USD: [],
    GBP: [],
    CHF: [],
    RUB: [],
    CNY: [],
    JPY: []
};

function currencyChange(){
    let keys = Object.keys(currencies);
    let key = keys[parseInt(Math.random() * keys.length)];
    let percent = Math.random() * 0.1 - 0.05;
    currencies[key] += percent * currencies[key];
    currencies_banks[key].forEach(el => {
        if(key == el.request.main){
            for(let curr in currencies){
                if(key != curr){
                    let rate = {
                        base: key,
                        target: curr,
                        rate: currencies[curr] / currencies[key]
                    };
                    el.write(rate);
                }
            }
        }
        else {
            let rate = {
                base: el.request.main,
                target: key,
                rate: key / el.request.main
            };
            el.write(rate);
        }
    });
    console.log(key + " => "  + (percent > 0 ? "↑" + (percent * 100).toFixed(2) + "%" : "↓" + (percent * -100).toFixed(2) + "%"));
}

function registerBank(call){
    currencies_banks[call.request.main].push(call);
    call.request.other.forEach(el => currencies_banks[el].push(call));
    let key = call.request.main;
    for(let curr in currencies){
        if(key != curr){
            let rate = {
                base: key,
                target: curr,
                rate: currencies[curr] / currencies[key]
            };
            call.write(rate);
        }
    }
    console.log(call.request.name + " registered!");
}

server.addService(proto.currency.CurrencyProvider.service, {
    RegisterBank: registerBank
});

server.bind('0.0.0.0:50051', grpc.ServerCredentials.createInsecure());

//Start the server
fixer.latest({base: 'KRW'})
    .then(data => {
        for(let key in currencies){
            currencies[key] = 1 / data.rates[key];
        }
    })
    .then(() => {
        server.start();
        setInterval(currencyChange, 3000);
        console.log('grpc server running on port:', '0.0.0.0:50051');
    });