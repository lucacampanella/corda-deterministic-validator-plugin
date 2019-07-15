package com.github.lucacampanella;

import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import java.util.Random;

/**
 * A very simple contract that shows that you cannot use non-deterministic calls / objects when you build
 * against the deterministic jdk
 */
public class ExampleContract implements Contract {
    public static final String ID = "com.github.lucacampanella.ExampleContract";

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    @Override
    public void verify(LedgerTransaction tx) {

        //this will fail at compile time
        Random r = new Random();

    }
}
