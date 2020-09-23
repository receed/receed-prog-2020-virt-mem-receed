# Page Replacement Algorithms

```
java -jar virt-mem.jar FILE...
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