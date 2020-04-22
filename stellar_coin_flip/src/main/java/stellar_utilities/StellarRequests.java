package stellar_utilities;

import org.stellar.sdk.*;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.Page;
import org.stellar.sdk.responses.SubmitTransactionResponse;
import org.stellar.sdk.responses.operations.OperationResponse;
import org.stellar.sdk.responses.operations.PaymentOperationResponse;

import java.io.IOException;

public class StellarRequests implements RequestInterface {

    @Override
    public void sendMoney(KeyPair fromAccount, String toAccount, String amount) throws IOException, AccountRequiresMemoException {
        Server server = new Server("https://horizon-testnet.stellar.org");
        KeyPair source = fromAccount;
        KeyPair destination = KeyPair.fromAccountId(toAccount);
        server.accounts().account(destination.getAccountId());

        AccountResponse sourceAccount = server.accounts().account(source.getAccountId());

        //verify account balances.
        for (AccountResponse.Balance balance : sourceAccount.getBalances()) {
            if (balance.getAsset() instanceof AssetTypeNative) {
                if (Double.parseDouble(balance.getBalance()) < Double.parseDouble(amount)) {
                    throw new IOException("Not enough money");
                }
            }
        }

        Transaction transaction = new Transaction.Builder(sourceAccount, Network.TESTNET)
                .addOperation(new PaymentOperation.Builder(destination.getAccountId(), new AssetTypeNative(), amount).build())
                .setTimeout(180)
                .setOperationFee(100)
                .build();
        transaction.sign(source);

        SubmitTransactionResponse response = server.submitTransaction(transaction);
        if (!response.isSuccess()) {
            throw new IOException("Something bad happened");
        }
    }

    @Override
    public void history(KeyPair accountToCheck) throws IOException {
        Server server = new Server("https://horizon-testnet.stellar.org");

        Page<OperationResponse> history = server.operations().forAccount(accountToCheck.getAccountId()).execute();
        for (OperationResponse record : history.getRecords()) {
            if (record instanceof PaymentOperationResponse) {
                PaymentOperationResponse o = (PaymentOperationResponse) record;
                System.out.println(String.format(
                        "Type: %s, From: %s, To: %s, Created: %s, Amount: %s",
                        o.getType(),
                        o.getFrom(),
                        o.getTo(),
                        o.getCreatedAt(),
                        o.getAmount()));
            } else {
                System.out.println(String.format(
                        "Type: %s, Source: %s, Created: %s",
                        record.getType(),
                        record.getSourceAccount(),
                        record.getCreatedAt()));
            }
        }
    }

    @Override
    public Double balance(KeyPair accountToCheck) throws IOException {
        Server server = new Server("https://horizon-testnet.stellar.org");
        AccountResponse account = server.accounts().account(accountToCheck.getAccountId());
        System.out.println("Balances for account " + accountToCheck.getAccountId());
        for (AccountResponse.Balance balance : account.getBalances()) {
            if (balance.getAsset() instanceof AssetTypeNative) {
                return Double.parseDouble(balance.getBalance());
            }
        }
        throw new IOException("Unable to get balance");
    }

    @Override
    public AccountResponse login(String accountId) throws IOException {
        Server server = new Server("https://horizon-testnet.stellar.org");
        AccountResponse accountResponse;
        try {
            accountResponse = server.accounts().account(accountId);
        } catch (Exception e) {
            throw new IOException("That account doesn't exist or another error occurred");
        }
        return accountResponse;
    }
}
