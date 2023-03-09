# ChessAI


Milestone

Initially, it was our goal to create a multi-threaded chess AI. We found great resources online to help create a Java-based chess implementation with an AI. We planned to start by developing the chess game, while researching parallel implementations, and then multithread the AI once the engine was built out. 

We realized that this would be a large undertaking, and in case the chess engine didn’t allow for parallelization, we would be left with no deliverable. So for now, we shifted our focus to parallelizing the core algorithm that the AI uses, which is Alpha-Beta-Pruning. 

Alpha-Beta-pruning is a very commonly used algorithm in chess engines, and we realized that it would make more sense for it to be the focus of our paper. However, if time permits, we still hope to implement our parallel alpha-beta algorithm into the chess engine. 

Initially, we did make significant progress building the chess engine from scratch. But for the purpose of the project, and due to time constraints, it didn’t make sense to make this our main focus. So, we found existing finished code that we will use as our base model for the chess game, and then attempt to modify its existing AI code to the parallelized alpha-beta implementation that we developed. 

One of the large roadblocks we are currently encountering is that an efficient sequential implementation has proven to outperform the parallel one despite our best efforts, using a variety of multi- threading techniques. 

Another blocker we found is that it is difficult to test trees with large depths due to limited heap space. My computer starts to crash around depth ~ 27. This is a blocker because parallel implementations tend to work much better for larger depths, but if we are unable to test deeper than 27 we may not know the true effectiveness of our algorithm. 

Our main goals currently are to improve and optimize our exisitng parallel alpha beta, and try to implement it into the chess engine. Once we are content with our implementation, we will create quantitative data to compare it to the sequential approach, and also to display in the results section of our paper. 

If we are able to successfully implement it into the chess AI, we can also gather data regarding lookup times compared to other existing chess AIs. 

GitHub repo link: https://github.com/cirillojon/ChessAI




Our Alpha-Beta code is contained in the: Parallel-Alpha-Beta folder. 

In this folder we have a parallel and a sequential implementation. The sequential implementation is used for testing, and both are currently seeded to create the same tree every time no matter the depth for testing purposes. We have helper functions to create a tree of any depth, and print out the tree as well as the resulting value and time taken. 

To run the code:

compile with: javac filename.java 
run: java filename.java

Notes: 

The unicode used in the print function to output the tree does not show up correctly on all computers, and may appear as ‘?’s instead of ‘—‘. 

When testing with large depths, it is important to comment out all print statements. 





