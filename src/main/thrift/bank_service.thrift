namespace java bank_service

typedef i32 int

struct User{
    1: string personal_info,
    2: string pesel,
    3: double salary,
}

exception BankServiceException{
    1: string why
}

service ManagerService{
    string createAccount(1: User user) throws (1: BankServiceException e);
}

service StandardService{
    double balance(1: string guid) throws (1: BankServiceException e)
}

service PremiumService extends StandardService{
    void takeLoan(1: string guid) throws (1: BankServiceException e)
}