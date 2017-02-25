<h1><b>topKQueries</b></h1>

Implemented Top-K queries over a relation Table of arity N + 1, using the scoring function :
F(A1, . . . , AN) = vi ∗ Ai for all i ranging from 1 to N

Implemented a Naive solution and Threshold Solution for the same.

Instructions to run the program:
1. Program arguments:
"k n"
where k: number of top k records to be retrieved
      n: number of columns in CSV file (except for primary key)

2. On console:
  1. init file_path   
    to create hash index and necessary sort
  2. run1 v1 v2 ... vn
    to run threshhold algorithm
  3. run2 v1 v2 ... vn
    to run naive algorithm


