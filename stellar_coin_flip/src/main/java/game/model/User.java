package game.model;

import org.stellar.sdk.KeyPair;
import org.stellar.sdk.responses.AccountResponse;

public class User {
    private AccountResponse accountResponse;
    private double balance;
    private String hash;
    private int guess;
    private double bet = 0;
    private UserState state = UserState.AWAIT_HASH_AND_BET;
    private KeyPair keyPair;

    private String accountId;

    public User(AccountResponse accountResponse, KeyPair keyPair) {
        this.accountResponse = accountResponse;
        accountId = accountResponse.getAccountId();
        this.keyPair = keyPair;
    }

    public UserState getState() {
        return state;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public AccountResponse getAccountResponse() {
        return accountResponse;
    }

    public void setAccountResponse(AccountResponse accountResponse) {
        this.accountResponse = accountResponse;
    }

    public void hashAndBetReceived(String hash, double bet){
        setHash(hash);
        setBet(bet);
        state = UserState.AWAIT_REAL_VALUE;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getGuess() {
        return guess;
    }

    public void setGuess(int guess) {
        this.guess = guess;
        state = UserState.AWAITING_WINNER;
    }

    public double getBet() {
        return bet;
    }

    public void setBet(double bet) {
        this.bet = bet;
    }
}
