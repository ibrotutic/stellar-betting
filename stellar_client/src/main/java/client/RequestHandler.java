package client;

import org.stellar.sdk.*;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.Page;
import org.stellar.sdk.responses.SubmitTransactionResponse;
import org.stellar.sdk.responses.operations.OperationResponse;
import org.stellar.sdk.responses.operations.PaymentOperationResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class RequestHandler implements ExecuteRequest {
    @Override
    public KeyPair createAccount() throws Exception {
        KeyPair keyPair = KeyPair.random();
        System.out.println("Creating account");
        String friendbotUrl = String.format(
                "https://friendbot.stellar.org/?addr=%s",
                keyPair.getAccountId());
        InputStream response = new URL(friendbotUrl).openStream();
        System.out.println("SUCCESS! You have a new account :)\n" + "Account ID: " + keyPair.getAccountId());
        return keyPair;
    }

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
        if (response.isSuccess()) {
            System.out.println("Success!");
        } else {
            System.out.println("Something happened, transaction was not successful");
        }
    }

    @Override
    public void history(KeyPair accountKeyPair) throws IOException {
        Server server = new Server("https://horizon-testnet.stellar.org");

        Page<OperationResponse> history = server.operations().forAccount(accountKeyPair.getAccountId()).execute();
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
    public void balance(KeyPair accountToCheck) throws IOException {
        Server server = new Server("https://horizon-testnet.stellar.org");
        AccountResponse account = server.accounts().account(accountToCheck.getAccountId());
        System.out.println("Balances for account " + accountToCheck.getAccountId());
        for (AccountResponse.Balance balance : account.getBalances()) {
            System.out.println(String.format(
                    "Type: %s, Code: %s, Balance: %s",
                    balance.getAssetType(),
                    balance.getAssetCode(),
                    balance.getBalance()));
        }
    }

    public KeyPair login(String accountId) throws IOException {
        Server server = new Server("https://horizon-testnet.stellar.org");
        KeyPair accountId1 = KeyPair.fromAccountId(accountId);
        server.accounts().account(accountId1.getAccountId());
        return accountId1;
    }
}
