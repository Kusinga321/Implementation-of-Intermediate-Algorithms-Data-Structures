package cp2026.nerwowiec;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.ReentrantLock;

public class ParallelEngine implements Engine {
    @Override
    public List<RecordedMove> playGame(List<Player> players, List<Iterator<Integer>> stacks, Duration timeout) throws InterruptedException {
        assert (players.size() == stacks.size());
        long deadlineNanos = System.nanoTime() + Math.max(0L, TimeUnit.NANOSECONDS.convert(timeout));
        int numPlayers = players.size();
        List<List<Integer>> hands = new ArrayList<>(numPlayers);
        AtomicIntegerArray topCards = new AtomicIntegerArray(numPlayers);
        ConcurrentLinkedQueue<RecordedMove> transcript = new ConcurrentLinkedQueue<>();
        AtomicInteger playersLeft = new AtomicInteger(numPlayers);
        List<ReentrantLock> stackLocks = new ArrayList<>(numPlayers);

        for (int i = 0; i < numPlayers; i++) {
            Iterator<Integer> stack = stacks.get(i);
            setupHand(stack, hands);
            topCards.set(i, stack.next());
            stackLocks.add(new ReentrantLock(true));
        }

        long startNanos = Clock.nanoTime();
        CountDownLatch gameOver = new CountDownLatch(1);
        CyclicBarrier barrier = new CyclicBarrier(numPlayers);
        Thread[] threads = new Thread[numPlayers];
        int started = 0;
        boolean interrupted = false;
        try {
            // Wszyscy gracze zaczynają wybierać ruch po przejściu bariery.
            while (started < numPlayers && deadlineNanos - System.nanoTime() > 0) {
                if (Thread.interrupted()) throw new InterruptedException();
                threads[started] = new Thread(new BotThread(
                        started, startNanos, deadlineNanos, playersLeft, transcript,
                        stackLocks, stacks, hands, topCards, players, gameOver, barrier));
                threads[started].start();
                started++;
            }
            gameOver.await(deadlineNanos - System.nanoTime(), TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            interrupted = true;
        } finally {
            gameOver.countDown();
            for (int i = 0; i < started; i++) {
                threads[i].interrupt();
            }
            for (int i = 0; i < started; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    // Ponawiam join tego samego wątku.
                    --i;
                    interrupted = true;
                }
            }
        }

        if (interrupted) throw new InterruptedException();

        List<RecordedMove> result = new ArrayList<>(transcript);
        result.sort(Comparator.comparingLong(RecordedMove::nanos));
        return result;
    }

    private void setupHand(Iterator<Integer> stack, List<List<Integer>> hands) {
        List<Integer> hand = new ArrayList<>();
        for (int i = 0; i < GameRules.HAND_SIZE; i++) {
            hand.add(stack.next());
        }
        hands.add(hand);
    }

    private static class BotThread implements Runnable {
        private final int id;
        private final long startNanos;
        private final long deadlineNanos;
        private final AtomicInteger playersLeft;
        private final ConcurrentLinkedQueue<RecordedMove> transcript;
        private final List<ReentrantLock> stackLocks;
        private final List<Iterator<Integer>> reserveStacks;
        private final List<List<Integer>> hands;
        private final AtomicIntegerArray topCards;
        private final List<Player> players;
        private final CountDownLatch gameOver;
        private final CyclicBarrier barrier;

        private BotThread(int id, long startNanos, long deadlineNanos,
                          AtomicInteger playersLeft, ConcurrentLinkedQueue<RecordedMove> transcript,
                          List<ReentrantLock> stackLocks, List<Iterator<Integer>> reserveStacks,
                          List<List<Integer>> hands, AtomicIntegerArray topCards, List<Player> players,
                          CountDownLatch gameOver, CyclicBarrier barrier) {
            this.id = id;
            this.startNanos = startNanos;
            this.deadlineNanos = deadlineNanos;
            this.playersLeft = playersLeft;
            this.transcript = transcript;
            this.stackLocks = stackLocks;
            this.reserveStacks = reserveStacks;
            this.hands = hands;
            this.topCards = topCards;
            this.players = players;
            this.gameOver = gameOver;
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                barrier.await();
                while (deadlineNanos - System.nanoTime() > 0
                        && gameOver.getCount() != 0
                        && !hands.get(id).isEmpty()
                        && !Thread.currentThread().isInterrupted()) {
                    int[] topCardsCopy = new int[topCards.length()];
                    for (int i = 0; i < topCardsCopy.length; i++) {
                        topCardsCopy[i] = topCards.get(i);
                    }
                    int[] hand = hands.get(id).stream().mapToInt(Integer::intValue).toArray();
                    Player.Move move = players.get(id).chooseMove(hand, topCardsCopy);
                    if (move instanceof Player.Move.PlayCard play
                            && checkIndices(play.cardIndex(), hand.length, play.stackIndex(), topCardsCopy.length)) {
                        playIfLegal(play);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (BrokenBarrierException e) {
                // Inny wątek przerwano przed rozpoczęciem gry.
            }
        }

        private boolean checkIndices(int cardId, int handSize, int stackId, int numberOfStacks) {
            return cardId >= 0 && cardId < handSize && stackId >= 0 && stackId < numberOfStacks;
        }

        private void playIfLegal(Player.Move.PlayCard move) throws InterruptedException {
            List<Integer> hand = hands.get(id);
            int card = hand.get(move.cardIndex());
            int stack = move.stackIndex();
            ReentrantLock lock = stackLocks.get(stack);
            lock.lockInterruptibly();
            try {
                if (!GameRules.isInSequence(card, topCards.get(stack))) return;
                long now = Clock.nanoTime() - startNanos;
                if (deadlineNanos - System.nanoTime() <= 0 || gameOver.getCount() == 0) return;
                topCards.set(stack, card);
                transcript.add(new RecordedMove(now, id, stack, card));

                // Ręka maleje poniżej czterech kart dopiero po wyczerpaniu stosu zakrytego.
                if (hand.size() == 1 && playersLeft.decrementAndGet() == 1) {
                    gameOver.countDown();
                }
            } finally {
                lock.unlock();
            }

            hand.remove(move.cardIndex());
            Iterator<Integer> reserveStack = reserveStacks.get(id);
            if (!hand.isEmpty() && reserveStack.hasNext()) {
                hand.add(reserveStack.next());
            }
        }
    }
}
