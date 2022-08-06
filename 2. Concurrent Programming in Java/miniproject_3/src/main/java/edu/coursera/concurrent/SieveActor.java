package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import static edu.rice.pcdp.PCDP.*;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determin the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        final SieveActorActor sieveActor = new SieveActorActor(2);
        finish(() -> {
            for(int i = 3; i <= limit; i+=2) {
                sieveActor.send(i);
            }

            sieveActor.send(0);
        });

        int nPrimes = 0;
        SieveActorActor loopActor = sieveActor;
        while(loopActor != null) {
            nPrimes += loopActor.getNumLocalPrimes();
            loopActor = loopActor.getNextActor();
        }

        return nPrimes;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        /**
         * Process a single message sent to this actor.
         *
         * TODO complete this method.
         *
         * @param msg Received message
         */

        private static final int MAX_LOCAL_PRIMES = 1_200;
        private final int[] localPrimes;
        private int numLocalPrimes;
        private SieveActorActor nextActor;

        public SieveActorActor(final int localPrime) {
            this.localPrimes = new int[MAX_LOCAL_PRIMES];
            this.localPrimes[0] = localPrime;
            this.numLocalPrimes = 1;
            this.nextActor = null;
        }

        public SieveActorActor getNextActor() {
            return nextActor;
        }

        public int getNumLocalPrimes() {
            return numLocalPrimes;
        }

        @Override
        public void process(final Object msg) {
            final int candidate = (Integer) msg;

            if(isLocallyPrime(candidate)) {
                if (numLocalPrimes < MAX_LOCAL_PRIMES) {
                    localPrimes[numLocalPrimes++] = candidate;
                    return;
                }

                if (nextActor == null) {
                    nextActor = new SieveActorActor(candidate);
                } else {
                    nextActor.send(candidate);
                }
            }
        }

        private boolean isLocallyPrime(final int candidate) {
            for(int i = 0; i < numLocalPrimes; i++) {
                if(candidate % localPrimes[i] == 0) {
                    return false;
                }
            }

            return true;
        }
    }
}
