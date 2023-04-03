# Milestone



Initially, our goal was to create a multi-threaded chess AI. We found great resources online to help create a Java-based chess implementation with an AI. We planned to start by developing the chess game while researching parallel implementations, and then multithread the AI once the engine was built out.

We realized that this would be a large undertaking, and in the case that the chess engine didn't allow for parallelization, we would be left with no deliverable. So for now, we shifted our focus to parallelizing the core algorithm that the AI uses, which is Alpha-Beta-Pruning.

Alpha-Beta-Pruning is a commonly used algorithm in chess engines, and we decided to make it the focus of our paper. However, if time permits, we still hope to implement our parallel alpha-beta algorithm into the chess engine.

Initially, we made significant progress building the chess engine from scratch. But for the purpose of the project and due to time constraints, instead of focusing our efforts on finishing building out the game itself, we found finished code that we can use as our base and then attempt to modify its existing AI code to the parallelized alpha-beta implementation that we developed.

One of the major roadblocks we are currently encountering is that an efficient sequential implementation has proven to outperform the parallel one despite our best efforts using a variety of multi-threading techniques.

Another blocker we encountered is that it is difficult to test trees with large depths due to limited heap space. My computer starts to crash around depth ~27. This is a blocker because parallel implementations tend to work much better for larger depths, but if we are unable to test deeper than 27, we may not know the true effectiveness of our algorithm.

Our main goals currently are to improve and optimize our existing parallel alpha-beta algorithm and try to implement it into the chess engine.

Once we are content with our implementation, we will create quantitative data to compare it to the sequential approach.

If we can successfully implement it into the chess AI, we can also gather data regarding lookup times compared to other existing chess AIs.

See README.md for instructions on running the code.
