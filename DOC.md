# Page Replacement Algorithms

```
gradle run --args="FILE..."
```

Each of the input files can contain several tests. Each test
should be described by a pair of lines: the first one should
contain the number of pages, and the number of available frames;
the second one should contain numbers of pages in the order they
are accessed, separated by spaces. Frames and pages are numbered
from 1.

Output file for the file "file" is "file.out". For each test case
and for each algorithm it contains number of page replacements.
Then, for each access, frame to which the accessed page is loaded
or 0 if it is already in memory.

```
gradle run --args="-p NUM_PAGES NUM_FRAMES NUM_ACCESSES"
```
Draws plot showing how many times a page will be loaded to memory
by each algorithm if there are NUM_PAGES pages, NUM_FRAMES frames
in memory and 1 to NUM_ACCESSES accesses to pages. Accesses are
generated randomly.