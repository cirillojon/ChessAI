# Instructions to run parallel-alpha-beta code

Our Alpha-Beta code is contained in the Parallel-Alpha-Beta folder.

In this folder, we have both parallel and sequential implementations. The sequential implementation is used for testing, and both are currently seeded to create the same tree every time no matter the depth for testing purposes. We have helper functions to create a tree of any depth and print out the tree as well as the resulting value and time taken.

To run the code:

compile with: javac filename.java
run: java filename.java

Notes:

The Unicode used in the print function to output the tree may not display correctly on all computers and may appear as '?'s instead of '—'.

When testing with large depths, it is important to comment out all print statements.
