package client;

import org.stellar.sdk.*;
import org.stellar.sdk.requests.EventListener;
import org.stellar.sdk.requests.PaymentsRequestBuilder;
import org.stellar.sdk.responses.operations.OperationResponse;
import org.stellar.sdk.responses.operations.PaymentOperationResponse;
import shadow.com.google.common.base.Optional;

public class PaymentWatcher {
    String token;

    public PaymentWatcher(KeyPair accountKeyPair) {
        Server server = new Server("https://horizon-testnet.stellar.org");

        PaymentsRequestBuilder paymentsRequest = server.payments().forAccount(accountKeyPair.getAccountId());

        @SuppressWarnings("ConstantConditions") String lastToken = loadLastPagingToken();
        //noinspection ConstantConditions
        if (lastToken != null) {
            paymentsRequest.cursor(lastToken);
        }

        paymentsRequest.stream(new EventListener<OperationResponse>() {
            @Override
            public void onEvent(OperationResponse payment) {
                savePagingToken(payment.getPagingToken());
                if (payment instanceof PaymentOperationResponse) {
                    if (!((PaymentOperationResponse) payment).getTo().equals(accountKeyPair.getAccountId())) {
                        return;
                    }

                    String amount = ((PaymentOperationResponse) payment).getAmount();

                    Asset asset = ((PaymentOperationResponse) payment).getAsset();
                    String assetName;
                    if (asset.equals(new AssetTypeNative())) {
                        assetName = "lumens";
                    } else {
                        assetName = ((AssetTypeCreditAlphaNum) asset).getCode() +
                                ":" +
                                ((AssetTypeCreditAlphaNum) asset).getIssuer();
                    }

                    String output = amount +
                            " " +
                            assetName +
                            " from " +
                            ((PaymentOperationResponse) payment).getFrom();
                    System.out.println(output);
                }

            }

            @Override
            public void onFailure(Optional<Throwable> optional, Optional<Integer> optional1) {
                System.out.println("A failure occurred when trying to watch payments");
            }
        });
    }

    private void savePagingToken(String pagingToken) {
        token = pagingToken;
    }

    private String loadLastPagingToken() {
        return token;
    }
}
