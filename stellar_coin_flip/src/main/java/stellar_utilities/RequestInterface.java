package stellar_utilities;

import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.responses.AccountResponse;

import java.io.IOException;

public interface RequestInterface {
    void sendMoney(KeyPair fromAccount, String toAccount, String amount) throws IOException, AccountRequiresMemoException;
    void history(KeyPair accountToCheck) throws IOException;
    Double balance(KeyPair accountToCheck) throws IOException;
    AccountResponse login(String secretId) throws IOException;
}
