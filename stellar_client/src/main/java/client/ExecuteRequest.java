package client;

import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.KeyPair;

import java.io.IOException;

public interface ExecuteRequest {
    KeyPair createAccount() throws Exception;
    void sendMoney(KeyPair fromAccount, String toAccount, String amount) throws IOException, AccountRequiresMemoException;
    void history(KeyPair accountToCheck) throws IOException;
    void balance(KeyPair accountToCheck) throws IOException;
}