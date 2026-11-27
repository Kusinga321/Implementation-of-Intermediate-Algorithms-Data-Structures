# Nerwowiec

Nerwowiec is a real-time card game in which bots race to empty their hands. Each bot can play a card onto any pile whose top card is one rank higher or lower, with ace adjacent to both king and two.

`ParallelEngine.java` runs one thread per bot. It locks the target pile and checks the move against its current top card, so two bots can play on different piles concurrently. At the end of the game it returns the accepted moves in timestamp order.

Build and run the supplied two-player example with JDK 21 from this directory.

```sh
javac -d out src/main/java/cp2026/nerwowiec/*.java
java -cp out cp2026.nerwowiec.Main
```

The example uses the course-provided `SingleThreadedEngine`. To run the same game concurrently, change the engine in `Main.java` to `new ParallelEngine()`.
