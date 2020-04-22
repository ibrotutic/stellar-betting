package game.model;

import org.apache.commons.codec.digest.DigestUtils;
import org.stellar.sdk.AssetTypeNative;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.responses.AccountResponse;
import stellar_utilities.StellarRequests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CoinFlipGame {
    HashMap<String, User> currentUsers = new HashMap<>();
    ArrayList<String> userIds = new ArrayList<>();
    private User banker;
    private GameState state;
    private boolean didCheat;
    private User winner;
    private StellarRequests requests = new StellarRequests();

    public CoinFlipGame() {
        initGame();
    }

    public List<String> currentUsers() {
        return currentUsers.values().stream().map(User::getAccountId).collect(Collectors.toList());
    }

    public GameState getGameState() {
        return state;
    }

    public void addUser(String accountId) throws IOException {
        AccountResponse userToAdd = null;
        userToAdd = requests.login(accountId);
        KeyPair keyPair = KeyPair.fromAccountId(accountId);

        if (!isGameFull()) {
            if (!currentUsers.containsKey(userToAdd.getAccountId())) {
                currentUsers.put(userToAdd.getAccountId(), new User(userToAdd, keyPair));
                userIds.add(userToAdd.getAccountId());
            } else {
                throw new IOException("User is already registered");
            }
        } else if (isGameFull() && state == GameState.AWAIT_HASH_AND_BET){
            throw new IOException("Game is full and is awaiting hashes and bets");
        } else if (isGameFull() && state == GameState.AWAIT_REAL_VALUE ||  state == GameState.AWAITING_WINNER) {
            throw new IOException("Game is full and in progress");
        }
        if (isGameFull()) {
            checkGameState();
        }
    }

    private void checkGameState() {
        if (state == GameState.AWAITING_USERS) {
            boolean shouldAwaitHashesAndBets = true;
            for(User user : currentUsers.values()) {
                if (!(user.getState() == UserState.AWAIT_HASH_AND_BET)) {
                    shouldAwaitHashesAndBets = false;
                    break;
                }
            }
            if (shouldAwaitHashesAndBets) {
                state = GameState.AWAIT_HASH_AND_BET;
            }
        } else if (state == GameState.AWAIT_HASH_AND_BET){
            boolean shouldAwaitRealValues = true;
            for(User user : currentUsers.values()) {
                if (!(user.getState() == UserState.AWAIT_REAL_VALUE)) {
                    shouldAwaitRealValues = false;
                    break;
                }
            }
            if (shouldAwaitRealValues) {
                state = GameState.AWAIT_REAL_VALUE;
            }
        } else if (state == GameState.AWAIT_REAL_VALUE) {
            boolean shouldChooseWinner = true;
            for(User user : currentUsers.values()) {
                if (!(user.getState() == UserState.AWAITING_WINNER)) {
                    shouldChooseWinner = false;
                    break;
                }
            }
            if (shouldChooseWinner) {
                state = GameState.AWAITING_WINNER;
                chooseWinner();
            }
        }
    }

    private void chooseWinner(){
        int total = 0;
        for (User user : currentUsers.values()) {
            total += user.getGuess();
            if (user.getHash().equals(DigestUtils.sha256Hex(Integer.toString(user.getGuess()))) && !didCheat) {
                didCheat = false;
            } else {
                didCheat = true;
            }
        }

        int result = total % userIds.size();
        for (User user : currentUsers.values()) {
            if (user.getAccountId().equals(userIds.get(result))) {
                winner = user;
                return;
            }
        }
    }

    public void updateUserGuess(int guess, String userId) throws IOException{
        checkIfAccountExists(userId);

        User user = currentUsers.get(userId);
        UserState userState = user.getState();

        if (userState == UserState.AWAITING_WINNER) {
            throw new IOException("Already sent in your number, current state is: " + userState.toString());
        }

        if (userState != UserState.AWAIT_REAL_VALUE || getGameState() == GameState.AWAITING_USERS) {
            throw new IOException("Not ready for your number, current state is: " + GameState.AWAITING_USERS);
        }

        currentUsers.get(userId).setGuess(guess);

        if (isGameFull()) {
            checkGameState();
        }
    }

    private void checkIfAccountExists(String userId) throws IOException {
        if (!currentUsers.containsKey(userId)) {
            throw new IOException("That user doesn't exist");
        }
    }

    public void updateUserBetAndHash(String userId, double bet, String hash) throws IOException {
        checkIfAccountExists(userId);

        Double balance = getUserBalance(userId);
        if (getGameState() != GameState.AWAIT_HASH_AND_BET) {
            throw new IOException("Not at that state yet, currently: " + GameState.AWAIT_HASH_AND_BET);
        }

        if (balance < bet) {
            throw new IOException("That userId does not have enough money. Current Balance: " + balance.toString());
        }

        if (currentUsers.get(userId).getState() == UserState.AWAITING_WINNER) {
            throw new IOException("You have already placed your bet. Now you are: " + UserState.AWAITING_WINNER.toString());
        }

        double otherBet = getOtherBet(userId);
        if (otherBet > 0) {
            if (otherBet != bet) {
                throw new IOException("That bet is not valid. You must bet the same as your opponent. Current bet: " + otherBet);
            }
        }

        currentUsers.get(userId).hashAndBetReceived(hash, bet);

        if (isGameFull()) {
            checkGameState();
        }
    }

    private double getOtherBet(String userId) {
        for (User user : currentUsers.values()) {
            if (user.getState() == UserState.AWAIT_REAL_VALUE && !user.getAccountId().equals(userId)) {
                return user.getBet();
            }
        }
        return 0;
    }

    public boolean isGameFull() {
        return currentUsers.size() > 1;
    }

    private void initGame() {
        currentUsers.clear();
        banker = new User(new AccountResponse("GCDI364JXXVW65KIEOQSKH6OMN7QEQRZAL23WPVEYB263PMXL2DVOA3K", (long) 10), KeyPair.fromAccountId("GCDI364JXXVW65KIEOQSKH6OMN7QEQRZAL23WPVEYB263PMXL2DVOA3K"));
        state = GameState.AWAITING_USERS;
    }

    public Double getUserBalance(String userId) throws IOException {
        if (currentUsers.containsKey(userId)) {
            AccountResponse accountResponse = currentUsers.get(userId).getAccountResponse();
            for (AccountResponse.Balance balance : accountResponse.getBalances()) {
                if (balance.getAsset() instanceof AssetTypeNative) {
                    return Double.parseDouble(balance.getBalance());
                }
            }
        }
        throw new IOException("User does not exist");
    }

    public String getWinner()  throws IOException {
        if (winner == null) {
            throw new IOException("No current winner");
        }
        if (didCheat) {
            return "Somebody cheated";
        }

        return "Winner was: " + winner.getAccountId() + "with a value of " + winner.getGuess() + "with a bet of " + winner.getBet() + " lumens";
    }

    public String userHistory() {
        StringBuilder result = new StringBuilder();
        for (User user : currentUsers.values()) {
            result.append("User ID").append(user.getAccountId()).append(" with a value of ").append(user.getGuess()).append(" with a bet of ").append(user.getBet()).append(" lumens\n");
        }
        try {
            result.append(getWinner());
        } catch (IOException e) {
            return result.toString();
        }
        return result.toString();
    }
}
