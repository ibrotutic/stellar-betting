package client;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.stellar.sdk.AccountRequiresMemoException;
import org.stellar.sdk.KeyPair;

import java.io.IOException;
import java.util.Scanner;

public class ClientInputHandler {
    Scanner input;
    String userId;
    KeyPair accountKeyPair;
    ServerRequestHandler serverRequestHandler;
    RequestHandler stellarRequestHandler;
    WebSocketRequestHandler webSocketRequestHandler;
    double bet;

    public ClientInputHandler() {
        input = new Scanner(System.in);
        System.out.println("Usage: \n login to login\n sendBet to send bet with number \n" +
                "sendNumber to send number \n winner to check winner \n accountHistory to check account history \n history to check game history " +
                "\n checkState to check current state \n balance to get balance \n exit to exit.");
        serverRequestHandler = new ServerRequestHandler();
        stellarRequestHandler = new RequestHandler();
        startInput();
    }

    private void startInput() {
        Scanner in = new Scanner(System.in);
        String userInput = "";
        do {
            userInput = in.nextLine();
            handleUserInput(userInput);
        } while (!userInput.equalsIgnoreCase("exit") && !userInput.equalsIgnoreCase("e"));
        System.out.println("Exiting...");
        in.close();
    }

    private void handleUserInput(String userInput) {
        String[] args = userInput.split(" ");

        if (validateInitialArgs(args)) {
            if (checkLogin(args[0])) {
                try {
                    if (accountKeyPair == null) {
                        loginUser();
                    } else {
                        System.out.println("You are already logged in");
                    }
                } catch (Exception e) {
                    System.out.println("Account creation failed" + e.getLocalizedMessage());
                }
            } else if (checkSendMoney(args[0])) {
                try {
                    stellarRequestHandler.sendMoney(accountKeyPair, args[1], args[2]);
                } catch (IOException e) {
                    System.out.println("There was an IO error: " + e.getLocalizedMessage());
                } catch (AccountRequiresMemoException e) {
                    System.out.println("There was a Memo Exception: " + e.getLocalizedMessage());
                }
            } else if (checkAccountHistory(args[0])) {
                try {
                    stellarRequestHandler.history(accountKeyPair);
                } catch (IOException e) {
                    System.out.println("Error retrieving history: " + e.getLocalizedMessage());
                }
            } else if (checkBalance(args[0])) {
                try {
                    stellarRequestHandler.balance(accountKeyPair);
                } catch (IOException e) {
                    System.out.println("An error occurred" + e.getLocalizedMessage());
                }
            } else if (checkSendBet(args[0])) {
                getUserHash();
            } else if (checkSendNumber(args[0])) {
                getRealValue();
            } else if (checkWinner(args[0])) {
                getWinner();
            } else if (checkHistory(args[0])) {
                //getGameHistory();
            } else if (checkState(args[0])) {
                getGameState();
            }
        } else {
            System.out.println("Bad input!");
        }
    }

    private boolean checkSendBet(String arg) {
        return arg.equalsIgnoreCase("sendBet");
    }

    private void createPaymentWatcher(KeyPair accountKeyPair) {
        new PaymentWatcher(accountKeyPair);
    }

    private boolean validateInitialArgs(String[] args) {
        boolean valid = false;
        if (checkLogin(args[0])) {
            valid = true;
        } else if (checkSendMoney(args[0])) {
            valid = true;
        } else if (checkHistory(args[0])) {
            valid = true;
        } else if (checkSendNumber(args[0])) {
            valid = userId != null;
        } else if (checkBalance(args[0])) {
            valid = userId != null;
        } else if (checkWinner(args[0])) {
            valid = true;
        } else if (checkState(args[0])) {
            valid = true;
        } else if (checkAccountHistory(args[0])) {
            valid = userId != null;
        } else if (checkSendBet(args[0])) {
            valid = userId != null;
        }
        return valid;
    }

    private boolean checkAccountHistory(String arg) {
        return arg.equalsIgnoreCase("accountHistory");
    }

    private boolean checkState(String arg) {
        return arg.equalsIgnoreCase("checkState");
    }

    private boolean checkWinner(String arg) {
        return arg.equalsIgnoreCase("checkWinner");
    }

    private boolean checkSendNumber(String arg) {
        return arg.equalsIgnoreCase("sendNumber");
    }

    private boolean checkHistory(String arg) {
        return arg.equalsIgnoreCase("history");
    }

    private boolean checkLogin(String arg) {
        return arg.equalsIgnoreCase("login");
    }

    private boolean checkSendMoney(String arg) {
        return arg.equalsIgnoreCase("sendMoney");
    }

    private boolean checkCreateAccount(String arg) {
        return arg.equalsIgnoreCase("c") || arg.equalsIgnoreCase("createAccount");
    }

    private boolean checkBalance(String arg) {
        return arg.equalsIgnoreCase("b") || arg.equalsIgnoreCase("balance");
    }

    private void loginUser() {
        System.out.println("Enter your Stellar User ID");
        String userInput = input.nextLine();
        try {
            accountKeyPair = stellarRequestHandler.login(userInput);
            createPaymentWatcher(accountKeyPair);
            getUserId(userInput);
        } catch (IOException e) {
            System.out.println("Are you sure the account id is correct?");
        }
    }

    private void getGameState() {
        serverRequestHandler.getGameState(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage();
                System.out.println("Call failed: " + mMessage);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String mMessage = response.body().string();
                if (response.isSuccessful()) {
                    System.out.println("Current game state: " + mMessage);
                }
            }
        });
    }

    private void getWinner() {
        serverRequestHandler.getWinner(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage();
                System.out.println("Call failed: " + mMessage);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String mMessage = response.body().string();
                if (response.isSuccessful()) {
                    System.out.println("Winner information: " + mMessage);
                } else {
                    System.out.println("Game is in progress, no winner yet!");
                }
            }
        });
    }

    private void getRealValue() {
        System.out.println("Enter the value you chose again, don't cheat!");
        String userInput = input.nextLine();
        sendRealValue(userInput);
    }

    private void sendRealValue(String value) {
        serverRequestHandler.sendRealValue(userId, value, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage();
                System.out.println("Call failed: " + mMessage);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String mMessage = response.body().string();
                if (response.isSuccessful()) {
                    System.out.println("Value received, awaiting other player");
                    getGameState();
                } else {
                    System.out.println("Call failed: " + mMessage);
                }
            }
        });
    }

    private void getUserId(String nextLine) {
        serverRequestHandler.registerUser(nextLine, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage();
                System.out.println("Call failed: " + mMessage);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String mMessage = response.body().string();
                if (response.isSuccessful()) {
                    System.out.println("Success!");
                    userId = nextLine;
                    getGameState();
                } else {
                    System.out.println("Call failed: " + mMessage);
                }
            }
        });
    }

    private void getUserHash() {
        System.out.println("Input number and bet, separated by a space.");
        String userInput = input.nextLine();
        String[] parsed = userInput.split(" ");
        if (parsed.length < 2) {
            System.out.println("Try again");
        } else {
            bet = Double.parseDouble(parsed[1]);
            String hash = getHashForNumber(parsed[0]);
            serverRequestHandler.sendHashAndBets(hash, parsed[1], userId, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    String mMessage = e.getMessage();
                    System.out.println("Call failed: " + mMessage);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String mMessage = response.body().string();
                    if (response.isSuccessful()) {
                        System.out.println("Hash and bet received");
                        getGameState();
                    } else {
                        System.out.println("Call failed: " + mMessage);
                    }
                }
            });
        }
    }

    private String getHashForNumber(String s) {
        return DigestUtils.sha256Hex(s);
    }
}
